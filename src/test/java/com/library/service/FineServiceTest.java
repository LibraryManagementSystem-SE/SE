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
  private MediaRepository mediaRepository;
  private UserRepository userRepository;


  @BeforeEach
	  void setUp() {
	    // Assign to class fields — not new local variables
	    this.userRepository = new InMemoryUserRepository();
	    this.mediaRepository = new InMemoryMediaRepository();
	    this.loanRepository = new InMemoryLoanRepository();
	    this.dateProvider = new FakeDateProvider(LocalDate.of(2025, 2, 1));

	    fineService =
	        new FineService(
	            this.userRepository,
	            this.loanRepository,
	            this.mediaRepository,
	            this.dateProvider,
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
  @Test
  void payFineRejectsZeroOrNegativeAmount() {
      user.addFine(BigDecimal.valueOf(10));

      assertThrows(LibraryException.class,
          () -> fineService.payFine(user.getId(), BigDecimal.ZERO));

      assertThrows(LibraryException.class,
          () -> fineService.payFine(user.getId(), BigDecimal.valueOf(-5)));
  }

  @Test
  void payFineThrowsWhenUserNotFound() {
      assertThrows(LibraryException.class,
          () -> fineService.payFine("missing", BigDecimal.TEN));
  }

  @Test
  void payFineThrowsWhenUserHasNoOutstandingFines() {
      assertThrows(LibraryException.class,
          () -> fineService.payFine(user.getId(), BigDecimal.ONE));
  }

  @Test
  void payFineThrowsWhenPaymentExceedsFine() {
      user.addFine(BigDecimal.valueOf(30));

      assertThrows(LibraryException.class,
          () -> fineService.payFine(user.getId(), BigDecimal.valueOf(40)));
  }

  @Test
  void payFineAllowsFullPayment() {
      user.addFine(BigDecimal.valueOf(25));

      BigDecimal balance = fineService.payFine(user.getId(), BigDecimal.valueOf(25));

      assertEquals(BigDecimal.ZERO, balance);
  }
  @Test
  void generateOverdueReportThrowsWhenUserMissing() {
      assertThrows(LibraryException.class,
          () -> fineService.generateOverdueReport("missing"));
  }

  @Test
  void generateOverdueReportReturnsEmptyWhenNoOverdues() {
      // Loans are already overdue in @BeforeEach, so fix their due dates:
      loanRepository.findAll().forEach(loan -> loan.markReturned(dateProvider.today()));

      var report = fineService.generateOverdueReport(user.getId());

      assertEquals(0, report.getItems().size());
      assertEquals(BigDecimal.ZERO, report.getTotalFine());
  }

  @Test
  void generateOverdueReportSkipsNonOverdueLoans() {
      // Change both loans to not overdue
      loanRepository.findAll().forEach(
          loan -> {
              loan.markReturned(dateProvider.today());
          }
      );

      var report = fineService.generateOverdueReport(user.getId());
      assertTrue(report.getItems().isEmpty());
  }

  @Test
  void generateOverdueReportThrowsWhenMediaMissing() {
      // Remove media that is referenced by an existing loan
      ((InMemoryMediaRepository) mediaRepository).delete("book1");

      assertThrows(LibraryException.class,
              () -> fineService.generateOverdueReport(user.getId()));
  }



}


