package com.library.support;

import java.time.LocalDate;

public class FakeDateProvider implements DateProvider {
  private LocalDate date;

  public FakeDateProvider(LocalDate initial) {
    this.date = initial;
  }

  @Override
  public LocalDate today() {
    return date;
  }

  public void advanceDays(long days) {
    date = date.plusDays(days);
  }
}


