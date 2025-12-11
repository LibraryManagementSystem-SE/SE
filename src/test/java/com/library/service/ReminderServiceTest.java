package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.domain.Book;
import com.library.domain.Loan;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.notification.ReminderObserver;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.repository.memory.InMemoryMediaRepository;
import com.library.repository.memory.InMemoryUserRepository;
import com.library.support.FakeDateProvider;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReminderServiceTest {

  private ReminderService reminderService;
  private UserRepository userRepository;
  private LoanRepository loanRepository;
  private FakeDateProvider dateProvider;
  private User user;

  @BeforeEach
  void setUp() {
    userRepository = new InMemoryUserRepository();
    loanRepository = new InMemoryLoanRepository();
    var mediaRepository = new InMemoryMediaRepository();

    dateProvider = new FakeDateProvider(LocalDate.of(2025, 3, 10));
    reminderService = new ReminderService(loanRepository, userRepository, dateProvider);

    // create user
    user = new User("user1", "carol", "Carol", UserRole.MEMBER, "pw");
    userRepository.save(user);

    // create a book + overdue loan
    Book book = new Book("book1", "Refactoring", "Fowler", "222");
    mediaRepository.save(book);

    Loan loan =
        new Loan(
            "loan1",
            user.getId(),
            book.getId(),
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 20));   // overdue by March 10

    loanRepository.save(loan);
  }


  // 1) basic working scenario one overdue observer notified

  @Test
  void sendsReminderToObserver() {
    ReminderObserver observer = mock(ReminderObserver.class);
    reminderService.register(observer);

    reminderService.sendDailyReminders();

    verify(observer).notify(user, "You have 1 overdue book(s).");
  }

  
  // 2) sendReminder returns true when overdue exists

  @Test
  void sendReminderReturnsTrueWhenOverdue() {
    assertTrue(reminderService.sendReminder(user));
  }

  // 3) sendReminder returns false when no overdue items

  @Test
  void sendReminderReturnsFalseWhenNoOverdue() {
    // make a new user with no overdue items
    User user2 = new User("u2", "tom", "Tom", UserRole.MEMBER, "pw");
    userRepository.save(user2);

    assertFalse(reminderService.sendReminder(user2));
  }

  // 4) sendDailyReminders returns list of notified users

  @Test
  void sendDailyRemindersReturnsUsersWhoWereNotified() {
    List<User> notified = reminderService.sendDailyReminders();
    assertEquals(1, notified.size());
    assertEquals(user.getId(), notified.get(0).getId());
  }

  // 5) sendDailyReminders with no overdue users returns empty list

  @Test
  void sendDailyRemindersReturnsEmptyListWhenNobodyOverdue() {
    // Clear loan repository
    for (Loan l : loanRepository.findActiveByUser(user.getId())) {
      loanRepository.delete(l.getId());
    }

    assertTrue(reminderService.sendDailyReminders().isEmpty());
  }


  // 6) Observer removal works (no notification sent)

  @Test
  void removeObserverStopsNotifications() {
    ReminderObserver observer = mock(ReminderObserver.class);
    reminderService.register(observer);
    reminderService.remove(observer);

    reminderService.sendReminder(user);

    verify(observer, never()).notify(any(), anyString());
  }

  // 7) multiple observers are all notified
  @Test
  void multipleObserversAllReceiveNotification() {
    ReminderObserver obs1 = mock(ReminderObserver.class);
    ReminderObserver obs2 = mock(ReminderObserver.class);

    reminderService.register(obs1);
    reminderService.register(obs2);

    reminderService.sendDailyReminders();

    verify(obs1).notify(user, "You have 1 overdue book(s).");
    verify(obs2).notify(user, "You have 1 overdue book(s).");
  }

  // 8) works correctly when no observers registered
  @Test
  void noObserversDoesNotThrow() {
    assertDoesNotThrow(() -> reminderService.sendDailyReminders());
  }
}
