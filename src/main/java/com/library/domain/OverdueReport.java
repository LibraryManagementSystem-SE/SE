package com.library.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregate summary for overdue loans.
 */
public class OverdueReport {
  private final String userId;
  private final List<Item> items;
  private final BigDecimal totalFine;

  public OverdueReport(String userId, List<Item> items) {
    this.userId = userId;
    this.items = List.copyOf(items);
    this.totalFine =
        items.stream()
            .map(Item::fineAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public String getUserId() {
    return userId;
  }

  public List<Item> getItems() {
    return items;
  }

  public BigDecimal getTotalFine() {
    return totalFine;
  }

  /**
   * Item within an overdue report that includes the fine amount per media.
   */
  public record Item(
      String mediaTitle, MediaType mediaType, long overdueDays, BigDecimal fineAmount) {}
}


