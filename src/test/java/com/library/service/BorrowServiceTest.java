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
  void borrowAndReturnAdjustQuantityAndPersist() {
    // Arrange: ensure single copy
    book.setQuantity(1);
    mediaRepository.save(book);

    // Act: borrow once
    borrowService.borrow(user.getId(), book.getId());

    // Assert: quantity decremented, unavailable
    Media afterBorrow =
        mediaRepository.findById(book.getId()).orElseThrow();
    assertEquals(0, afterBorrow.getQuantity());
    assertFalse(afterBorrow.isAvailable());

    // Act: return it
    Loan loan = loanRepository.findActiveByUser(user.getId()).get(0);
    ((FakeDateProvider) dateProvider).advanceDays(1); // not overdue, just to move time
    borrowService.returnMedia(loan.getId());

    // Assert: quantity restored, available
    Media afterReturn =
        mediaRepository.findById(book.getId()).orElseThrow();
    assertEquals(1, afterReturn.getQuantity());
    assertTrue(afterReturn.isAvailable());
  }
}


