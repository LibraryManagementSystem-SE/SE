package com.library.support;

import java.time.LocalDate;

/**
 * Abstraction around the system clock to simplify testing.
 */
@FunctionalInterface
public interface DateProvider {
  LocalDate today();

  /**
   * Production implementation backed by the system clock.
   */
  class System implements DateProvider {
    @Override
    public LocalDate today() {
      return LocalDate.now();
    }
  }
}
