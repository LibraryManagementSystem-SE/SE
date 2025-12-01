package com.library.notification;

import com.library.domain.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 // Simple email notifier that records sent messages for verification.
**/
public class EmailNotifier implements ReminderObserver {
  private final List<String> sentMessages = new ArrayList<>();

  @Override
  public void notify(User user, String message) {
    sentMessages.add("To %s: %s".formatted(user.getUsername(), message));
  }

  public List<String> getSentMessages() {
    return Collections.unmodifiableList(sentMessages);
  }

  public void clear() {
    sentMessages.clear();
  }
}


