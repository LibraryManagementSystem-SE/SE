package com.library.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReminderServiceTest {
  private ReminderService reminderService;
  private User user;
  private LoanRepository loanRepository;
  private FakeDateProvider dateProvider;

  @BeforeEach
  void setUp() {
    UserRepository userRepository = new InMemoryUserRepository();
    loanRepository = new InMemoryLoanRepository();
    var mediaRepository = new InMemoryMediaRepository();
    dateProvider = new FakeDateProvider(LocalDate.of(2025, 3, 10));
    reminderService = new ReminderService(loanRepository, userRepository, dateProvider);

    user = new User("user1", "carol", "Carol", UserRole.MEMBER, "pw");
    userRepository.save(user);

    Book book = new Book("book1", "Refactoring", "Fowler", "222");
    mediaRepository.save(book);
    Loan loan =
        new Loan(
            "loan1",
            user.getId(),
            book.getId(),
            LocalDate.of(2025, 2, 1),
            LocalDate.of(2025, 2, 20));
    loanRepository.save(loan);
  }

  @Test
  void sendsReminderToObserver() {
    ReminderObserver observer = mock(ReminderObserver.class);
    reminderService.register(observer);

    reminderService.sendDailyReminders();

    verify(observer).notify(user, "You have 1 overdue book(s).");
  }
}


