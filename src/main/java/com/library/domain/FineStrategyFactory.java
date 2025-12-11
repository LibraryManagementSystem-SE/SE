package com.library.domain;

import java.math.BigDecimal;

/**
 * Provides the correct fine-calculation strategy for each media type.
 *
 * <p>This class implements a simple version of the Strategy pattern:
 * each media type (Book, CD, etc.) has its own way of computing fines.
 * The factory returns the appropriate strategy based on the {@link MediaType}.</p>
 */
public class FineStrategyFactory {

  /** Fine strategy used for book items. */
  private final Strategy bookStrategy = new BookStrategy();

  /** Fine strategy used for CD items. */
  private final Strategy cdStrategy = new CDStrategy();

  /**
   * Returns the fine calculation strategy that matches the given media type.
   *
   * @param type the type of media (BOOK or CD)
   * @return the matching fine calculation strategy
   */
  public Strategy forType(MediaType type) {
    return switch (type) {
      case BOOK -> bookStrategy;
      case CD -> cdStrategy;
    };
  }

  /**
   * Represents a calculation strategy for overdue fines.
   *
   * <p>Each media type implements this differently, depending on the rules
   * defined for that category.</p>
   */
  public interface Strategy {

    /**
     * Calculates the fine for a given number of overdue days.
     *
     * @param overdueDays the number of days past the due date
     * @return the amount owed, or zero if not overdue
     */
    BigDecimal calculateFine(long overdueDays);
  }

  /**
   * Fine strategy for books.
   *
   * <p>Books use a flat rate of 10 NIS per overdue day.</p>
   */
  private static class BookStrategy implements Strategy {
    private static final BigDecimal DAILY_RATE = BigDecimal.TEN;

    @Override
    public BigDecimal calculateFine(long overdueDays) {
      if (overdueDays <= 0) {
        return BigDecimal.ZERO;
      }
      return DAILY_RATE.multiply(BigDecimal.valueOf(overdueDays));
    }
  }

  /**
   * Fine strategy for CDs.
   *
   * <p>CDs incur a higher fine of 20 NIS per overdue day.</p>
   */
  private static class CDStrategy implements Strategy {
    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(20);

    @Override
    public BigDecimal calculateFine(long overdueDays) {
      if (overdueDays <= 0) {
        return BigDecimal.ZERO;
      }
      return DAILY_RATE.multiply(BigDecimal.valueOf(overdueDays));
    }
  }
}
