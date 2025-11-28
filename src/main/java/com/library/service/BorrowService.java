package com.library.service;

import com.library.domain.Loan;
import com.library.domain.Media;
import com.library.domain.MediaType;
import com.library.domain.User;
import com.library.domain.FineStrategyFactory;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.repository.UserRepository;
import com.library.support.DateProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Handles the lifecycle of borrowing and returning media.
 */
public class BorrowService {
  private static final int BOOK_LOAN_DAYS = 28;
  private static final int CD_LOAN_DAYS = 7;

  private final LoanRepository loanRepository;
  private final MediaRepository mediaRepository;
  private final UserRepository userRepository;
  private final DateProvider dateProvider;
  private final FineStrategyFactory fineStrategyFactory;

  public BorrowService(
      LoanRepository loanRepository,
      MediaRepository mediaRepository,
      UserRepository userRepository,
      DateProvider dateProvider,
      FineStrategyFactory fineStrategyFactory) {
    this.loanRepository = loanRepository;
    this.mediaRepository = mediaRepository;
    this.userRepository = userRepository;
    this.dateProvider = dateProvider;
    this.fineStrategyFactory = fineStrategyFactory;
  }

  public Loan borrow(String userId, String mediaId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new LibraryException("User not found: " + userId));
    Media media =
        mediaRepository
            .findById(mediaId)
            .orElseThrow(() -> new LibraryException("Media not found: " + mediaId));

    ensureBorrowAllowed(user);
    if (!media.isAvailable()) {
      throw new LibraryException("Media already loaned out");
    }

    LocalDate checkoutDate = dateProvider.today();
    int duration = media.getType() == MediaType.BOOK ? BOOK_LOAN_DAYS : CD_LOAN_DAYS;
    Loan loan =
        new Loan(
            UUID.randomUUID().toString(), user.getId(), media.getId(), checkoutDate,
            checkoutDate.plusDays(duration));

    loanRepository.save(loan);
    media.markUnavailable();
    user.addLoan(loan.getId());
    return loan;
  }

  public BigDecimal returnMedia(String loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new LibraryException("Loan not found: " + loanId));
    if (loan.isReturned()) {
      return BigDecimal.ZERO;
    }
    LocalDate today = dateProvider.today();
    loan.markReturned(today);

    Media media =
        mediaRepository
            .findById(loan.getMediaId())
            .orElseThrow(() -> new LibraryException("Media not found: " + loan.getMediaId()));
    media.markAvailable();

    User user =
        userRepository
            .findById(loan.getUserId())
            .orElseThrow(() -> new LibraryException("User not found: " + loan.getUserId()));
    user.closeLoan(loan.getId());

    long overdueDays = loan.daysOverdue(today);
    BigDecimal fine =
        fineStrategyFactory.forType(media.getType()).calculateFine(overdueDays);
    user.addFine(fine);
    return fine;
  }

  private void ensureBorrowAllowed(User user) {
    if (user.hasOutstandingFines()) {
      throw new LibraryException("Outstanding fines must be paid first");
    }
    List<Loan> activeLoans = loanRepository.findActiveByUser(user.getId());
    LocalDate today = dateProvider.today();
    boolean hasOverdue =
        activeLoans.stream().anyMatch(loan -> loan.isOverdue(today));
    if (hasOverdue) {
      throw new LibraryException("User has overdue loans");
    }
  }
}


