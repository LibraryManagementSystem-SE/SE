package com.library.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Loan connecting a user with a piece of media.
 */
public class Loan {
  private final String id;
  private final String userId;
  private final String mediaId;
  private final LocalDate checkoutDate;
  private final LocalDate dueDate;
  private LocalDate returnedDate;

  public Loan(String id, String userId, String mediaId, LocalDate checkoutDate, LocalDate dueDate) {
    this.id = Objects.requireNonNull(id, "id");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.mediaId = Objects.requireNonNull(mediaId, "mediaId");
    this.checkoutDate = Objects.requireNonNull(checkoutDate, "checkoutDate");
    this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getMediaId() {
    return mediaId;
  }

  public LocalDate getCheckoutDate() {
    return checkoutDate;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public LocalDate getReturnedDate() {
    return returnedDate;
  }

  public boolean isReturned() {
    return returnedDate != null;
  }

  public void markReturned(LocalDate returnDate) {
    this.returnedDate = Objects.requireNonNull(returnDate, "returnDate");
  }

  public boolean isOverdue(LocalDate referenceDate) {
    return !isReturned() && referenceDate.isAfter(dueDate);
  }

  public long daysOverdue(LocalDate referenceDate) {
    if (!isOverdue(referenceDate)) {
      return 0;
    }
    return java.time.temporal.ChronoUnit.DAYS.between(dueDate, referenceDate);
  }
}
