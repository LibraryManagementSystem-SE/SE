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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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

        // user
        user = new User("u1", "tala", "Tala", UserRole.MEMBER, "pw");
        userRepository.save(user);

        // media
        Book book = new Book("b1", "Clean Code", "Martin", "111");
        mediaRepository.save(book);

        // overdue loan
        Loan loan = new Loan(
                "l1",
                user.getId(),
                book.getId(),
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 2, 20) // overdue on March 10
        );
        loanRepository.save(loan);
    }

    /* =========================
       register / remove observer
       ========================= */

    @Test
    void observerIsNotifiedWhenOverdueExists() {
        ReminderObserver observer = mock(ReminderObserver.class);
        reminderService.register(observer);

        reminderService.sendReminder(user);

        verify(observer).notify(
                eq(user),
                eq("You have 1 overdue book(s).")
        );
    }

    @Test
    void removedObserverIsNotNotified() {
        ReminderObserver observer = mock(ReminderObserver.class);
        reminderService.register(observer);
        reminderService.remove(observer);

        reminderService.sendReminder(user);

        verify(observer, never()).notify(any(), anyString());
    }

    /* =========================
       sendReminder
       ========================= */

    @Test
    void sendReminderReturnsTrueWhenOverdueExists() {
        boolean result = reminderService.sendReminder(user);
        assertTrue(result);
    }

    @Test
    void sendReminderReturnsFalseWhenNoOverdues() {
        // mark loan as returned
        loanRepository.findAll()
                .forEach(loan -> loan.markReturned(dateProvider.today()));

        boolean result = reminderService.sendReminder(user);
        assertFalse(result);
    }

    /* =========================
       sendDailyReminders
       ========================= */

    @Test
    void sendDailyRemindersReturnsListOfNotifiedUsers() {
        List<User> notified = reminderService.sendDailyReminders();

        assertEquals(1, notified.size());
        assertEquals(user.getId(), notified.get(0).getId());
    }

    @Test
    void sendDailyRemindersReturnsEmptyWhenNoUsersOverdue() {
        // clear loans
        loanRepository.findAll()
                .forEach(loan -> loanRepository.delete(loan.getId()));

        List<User> notified = reminderService.sendDailyReminders();

        assertTrue(notified.isEmpty());
    }

    @Test
    void sendDailyRemindersWorksWithNoObservers() {
        assertDoesNotThrow(() -> reminderService.sendDailyReminders());
    }
}
