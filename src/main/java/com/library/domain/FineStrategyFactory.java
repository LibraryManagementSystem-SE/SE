package com.library.domain;

import java.math.BigDecimal;


 // Resolves a fine strategy for each media type. Uses the Strategy pattern with nested implementations.

public class FineStrategyFactory {
  private final Strategy bookStrategy = new BookStrategy();
  private final Strategy cdStrategy = new CDStrategy();

  public Strategy forType(MediaType type) {
    return switch (type) {
      case BOOK -> bookStrategy;
      case CD -> cdStrategy;
    };
  }

  /**
   * Strategy interface for fine calculation per media type.
   */
  public interface Strategy {
    /**
     * Calculates a fine amount for the given overdue days.
     *
     * @param overdueDays number of days past the due date
     * @return fine owed for the delay
     */
    BigDecimal calculateFine(long overdueDays);
  }

  	/**
    //Flat rate fine for overdue books: 10 NIS per day.
	**/
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
   // Higher daily fine for CDs: 20 NIS per day.
**/
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

