package com.library.service;

import com.library.domain.Loan;
import com.library.domain.Media;
import com.library.domain.OverdueReport;
import com.library.domain.User;
import com.library.domain.FineStrategyFactory;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.repository.UserRepository;
import com.library.support.DateProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles fine payments and reporting.
 */
public class FineService {
  private final UserRepository userRepository;
  private final LoanRepository loanRepository;
  private final MediaRepository mediaRepository;
  private final DateProvider dateProvider;
  private final FineStrategyFactory fineStrategyFactory;

  public FineService(
      UserRepository userRepository,
      LoanRepository loanRepository,
      MediaRepository mediaRepository,
      DateProvider dateProvider,
      FineStrategyFactory fineStrategyFactory) {
    this.userRepository = userRepository;
    this.loanRepository = loanRepository;
    this.mediaRepository = mediaRepository;
    this.dateProvider = dateProvider;
    this.fineStrategyFactory = fineStrategyFactory;
  }

  public BigDecimal payFine(String userId, BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new LibraryException("Payment must be positive");
    }
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new LibraryException("User not found: " + userId));
    user.payFine(amount);
    return user.getFineBalance();
  }

  public OverdueReport generateOverdueReport(String userId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new LibraryException("User not found: " + userId));
    List<Loan> loans = loanRepository.findActiveByUser(userId);
    LocalDate today = dateProvider.today();
    List<OverdueReport.Item> items = new ArrayList<>();
    for (Loan loan : loans) {
      if (!loan.isOverdue(today)) {
        continue;
      }
      Media media =
          mediaRepository
              .findById(loan.getMediaId())
              .orElseThrow(() -> new LibraryException("Media missing for loan " + loan.getId()));
      long overdueDays = loan.daysOverdue(today);
      BigDecimal fine =
          fineStrategyFactory.forType(media.getType()).calculateFine(overdueDays);
      items.add(new OverdueReport.Item(media.getTitle(), media.getType(), overdueDays, fine));
    }
    return new OverdueReport(userId, items);
  }
}


