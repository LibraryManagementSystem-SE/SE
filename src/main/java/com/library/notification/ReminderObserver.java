package com.library.notification;

import com.library.domain.User;

/**
 //Observer that receives reminder notifications.
**/
@FunctionalInterface
public interface ReminderObserver {
  void notify(User user, String message);
}


