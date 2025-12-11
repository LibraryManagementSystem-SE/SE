package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.Loan;
import com.library.domain.Media;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.domain.FineStrategyFactory;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.repository.UserRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.repository.memory.InMemoryMediaRepository;
import com.library.repository.memory.InMemoryUserRepository;
import com.library.support.DateProvider;
import com.library.support.FakeDateProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BorrowServiceTest {

  private MediaRepository mediaRepository;
  private UserRepository userRepository;
  private LoanRepository loanRepository;
  private DateProvider dateProvider;
  private BorrowService borrowService;
  private User user;
  private Book book;
  private CD cd;

  @BeforeEach
  void setUp() {
    mediaRepository = new InMemoryMediaRepository();
    userRepository = new InMemoryUserRepository();
    loanRepository = new InMemoryLoanRepository();
    dateProvider = new FakeDateProvider(LocalDate.of(2025, 1, 1));
    borrowService =
        new BorrowService(
            loanRepository,
            mediaRepository,
            userRepository,
            dateProvider,
            new FineStrategyFactory());
    user = new User("u1", "alice", "Alice", UserRole.MEMBER, "pw");
    userRepository.save(user);
    book = new Book("b1", "Domain-Driven Design", "Evans", "123");
    cd = new CD("c1", "Kind of Blue", "Miles Davis");
    mediaRepository.save(book);
    mediaRepository.save(cd);
  }
  private void removeMediaFromRepository(String id) {
      try {
          var field = InMemoryMediaRepository.class.getDeclaredField("mediaStore");
          field.setAccessible(true);
          @SuppressWarnings("unchecked")
          Map<String, Media> store = (Map<String, Media>) field.get(mediaRepository);
          store.remove(id);
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
  }

  @Test
  void borrowBookUses28DayLoanPeriod() {
    Loan loan = borrowService.borrow(user.getId(), book.getId());
    assertEquals(LocalDate.of(2025, 1, 29), loan.getDueDate());
    assertFalse(book.isAvailable());
  }

  @Test
  void borrowCdUsesSevenDayLoanPeriod() {
    Loan loan = borrowService.borrow(user.getId(), cd.getId());
    assertEquals(LocalDate.of(2025, 1, 8), loan.getDueDate());
  }

  @Test
  void borrowBlockedWhenUserHasOutstandingFine() {
    user.addFine(BigDecimal.TEN);
    assertThrows(LibraryException.class, () -> borrowService.borrow(user.getId(), book.getId()));
  }

  @Test
  void borrowBlockedWhenUserHasOverdueLoan() {
    Loan existing =
        new Loan("l1", user.getId(), book.getId(), LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 29));
    loanRepository.save(existing);
    assertThrows(LibraryException.class, () -> borrowService.borrow(user.getId(), cd.getId()));
  }

  @Test
  void returningOverdueMediaAddsFine() {
    Loan loan = borrowService.borrow(user.getId(), book.getId());
    ((FakeDateProvider) dateProvider).advanceDays(30);
    BigDecimal fine = borrowService.returnMedia(loan.getId());
    assertEquals(BigDecimal.valueOf(20), fine); // 2 days overdue * 10
    assertTrue(book.isAvailable());
  }
  @Test
  void borrowFailsWhenMediaAlreadyLoanedOut() {
      // borrow once
      borrowService.borrow(user.getId(), book.getId());

      // try borrowing again while unavailable
      assertThrows(LibraryException.class,
          () -> borrowService.borrow(user.getId(), book.getId()));
  }

  @Test
  void borrowFailsWhenUserDoesNotExist() {
      assertThrows(LibraryException.class,
          () -> borrowService.borrow("unknown-user", book.getId()));
  }

  @Test
  void borrowFailsWhenMediaDoesNotExist() {
      assertThrows(LibraryException.class,
          () -> borrowService.borrow(user.getId(), "missing-media"));
  }

  @Test
  void returnMediaReturnsZeroIfAlreadyReturned() {
      Loan loan = borrowService.borrow(user.getId(), book.getId());
      borrowService.returnMedia(loan.getId()); // first return

      BigDecimal secondTime = borrowService.returnMedia(loan.getId());
      assertEquals(BigDecimal.ZERO, secondTime);
  }

  @Test
  void returnMediaFailsWhenLoanDoesNotExist() {
      assertThrows(LibraryException.class,
          () -> borrowService.returnMedia("missing-loan"));
  }

  @Test
  void returnMediaFailsWhenMediaDoesNotExist() {
      Loan loan = borrowService.borrow(user.getId(), book.getId());

      // use the helper method
      removeMediaFromRepository(book.getId());

      assertThrows(LibraryException.class,
          () -> borrowService.returnMedia(loan.getId()));
  }

  @Test
  void returnMediaFailsWhenUserDoesNotExist() {
      Loan loan = borrowService.borrow(user.getId(), book.getId());

      // delete user to simulate data corruption
      userRepository.delete(user.getId());

      assertThrows(LibraryException.class,
          () -> borrowService.returnMedia(loan.getId()));
  }

}


