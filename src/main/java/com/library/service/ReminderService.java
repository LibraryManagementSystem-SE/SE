package com.library.service;

import com.library.domain.Loan;
import com.library.domain.User;
import com.library.notification.ReminderObserver;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import com.library.support.DateProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject side of the observer pattern for overdue reminders.
 */
public class ReminderService {
  private final LoanRepository loanRepository;
  private final UserRepository userRepository;
  private final DateProvider dateProvider;
  private final List<ReminderObserver> observers = new CopyOnWriteArrayList<>();

  public ReminderService(
      LoanRepository loanRepository, UserRepository userRepository, DateProvider dateProvider) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.dateProvider = dateProvider;
  }

  public void register(ReminderObserver observer) {
    observers.add(observer);
  }

  public void remove(ReminderObserver observer) {
    observers.remove(observer);
  }

  public List<User> sendDailyReminders() {
    List<User> notified = new ArrayList<>();
    for (User user : userRepository.findAll()) {
      if (sendReminder(user)) {
        notified.add(user);
      }
    }
    return notified;
  }

  public boolean sendReminder(User user) {
    LocalDate today = dateProvider.today();
    long overdueCount =
        loanRepository.findActiveByUser(user.getId()).stream()
            .filter(loan -> loan.isOverdue(today))
            .count();
    if (overdueCount == 0) {
      return false;
    }
    String message = "You have %d overdue book(s).".formatted(overdueCount);
    notifyObservers(user, message);
    return true;
  }

  private void notifyObservers(User user, String message) {
    for (ReminderObserver observer : observers) {
      observer.notify(user, message);
    }
  }
}


