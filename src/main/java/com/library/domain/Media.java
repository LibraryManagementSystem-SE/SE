package com.library.domain;

import java.util.Objects;

/**
 * Base class for all library media types.
 */
public abstract class Media {
  private final String id;
  private final String title;
  private final MediaType type;
  private boolean available = true;

  protected Media(String id, String title, MediaType type) {
    this.id = Objects.requireNonNull(id, "id");
    this.title = Objects.requireNonNull(title, "title");
    this.type = Objects.requireNonNull(type, "type");
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public MediaType getType() {
    return type;
  }

  public boolean isAvailable() {
    return available;
  }

  public void markUnavailable() {
    this.available = false;
  }

  public void markAvailable() {
    this.available = true;
  }
}
