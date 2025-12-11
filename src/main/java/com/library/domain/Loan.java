package com.library.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a loan of a media item by a user.
 *
 * <p>A Loan connects a user to a specific media item and keeps track of
 * when it was borrowed, its due date, and (if returned) the return date.
 * It also provides simple helpers for checking overdue status and calculating
 * how many days past due the item is.</p>
 */
public class Loan {

  /** Unique identifier for this loan. */
  private final String id;

  /** ID of the user who borrowed the item. */
  private final String userId;

  /** ID of the borrowed media item. */
  private final String mediaId;

  /** The date when the media item was checked out. */
  private final LocalDate checkoutDate;

  /** The due date by which the item must be returned. */
  private final LocalDate dueDate;

  /** The actual return date, or null if not yet returned. */
  private LocalDate returnedDate;

  /**
   * Creates a new loan record.
   *
   * @param id unique loan ID
   * @param userId ID of the borrowing user
   * @param mediaId ID of the borrowed media
   * @param checkoutDate date when the item was borrowed
   * @param dueDate date when the item should be returned
   */
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

  /**
   * @return true if the item has been returned, false otherwise.
   */
  public boolean isReturned() {
    return returnedDate != null;
  }

  /**
   * Marks the loan as returned on the given date.
   *
   * @param returnDate the date the item was actually returned
   */
  public void markReturned(LocalDate returnDate) {
    this.returnedDate = Objects.requireNonNull(returnDate, "returnDate");
  }

  /**
   * Checks whether the loan is overdue on a specific date.
   *
   * @param referenceDate date to compare against the due date
   * @return true if overdue and not yet returned, false otherwise
   */
  public boolean isOverdue(LocalDate referenceDate) {
    return !isReturned() && referenceDate.isAfter(dueDate);
  }

  /**
   * Calculates how many days the item is overdue.
   *
   * @param referenceDate the date used for the calculation
   * @return overdue days, or 0 if not overdue
   */
  public long daysOverdue(LocalDate referenceDate) {
    if (!isOverdue(referenceDate)) {
      return 0;
    }
    return java.time.temporal.ChronoUnit.DAYS.between(dueDate, referenceDate);
  }
}
