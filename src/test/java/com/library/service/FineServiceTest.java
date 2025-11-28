package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.Loan;
import com.library.domain.OverdueReport;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.domain.FineStrategyFactory;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.repository.UserRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.repository.memory.InMemoryMediaRepository;
import com.library.repository.memory.InMemoryUserRepository;
import com.library.support.FakeDateProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FineServiceTest {

  private User user;
  private FineService fineService;
  private LoanRepository loanRepository;
  private FakeDateProvider dateProvider;

  @BeforeEach
  void setUp() {
    UserRepository userRepository = new InMemoryUserRepository();
    MediaRepository mediaRepository = new InMemoryMediaRepository();
    loanRepository = new InMemoryLoanRepository();
    dateProvider = new FakeDateProvider(LocalDate.of(2025, 2, 1));
    fineService =
        new FineService(
            userRepository,
            loanRepository,
            mediaRepository,
            dateProvider,
            new FineStrategyFactory());

    user = new User("user1", "bob", "Bob", UserRole.MEMBER, "pw");
    userRepository.save(user);

    Book book = new Book("book1", "Clean Code", "Martin", "111");
    CD cd = new CD("cd1", "Blue Train", "Coltrane");
    mediaRepository.save(book);
    mediaRepository.save(cd);

    Loan bookLoan =
        new Loan(
            "loan1",
            user.getId(),
            book.getId(),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 20));
    Loan cdLoan =
        new Loan(
            "loan2",
            user.getId(),
            cd.getId(),
            LocalDate.of(2025, 1, 15),
            LocalDate.of(2025, 1, 25));
    loanRepository.save(bookLoan);
    loanRepository.save(cdLoan);
  }

  @Test
  void overdueReportAggregatesMixedMedia() {
    OverdueReport report = fineService.generateOverdueReport(user.getId());
    assertEquals(2, report.getItems().size());
    assertEquals(
        BigDecimal.valueOf(10 * 12L + 20 * 7L), report.getTotalFine()); // 12 & 7 days overdue
  }

  @Test
  void payFineSupportsPartialPayments() {
    user.addFine(BigDecimal.valueOf(50));
    BigDecimal balance = fineService.payFine(user.getId(), BigDecimal.valueOf(20));
    assertEquals(BigDecimal.valueOf(30), balance);
  }
}


