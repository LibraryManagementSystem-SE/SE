package com.library.domain;

import java.util.Objects;

/**
 * Base class for all media items in the library (Book, CD).
 */
public abstract class Media {

  private final String id;
  private final String title;
  private final MediaType type;
  private boolean available = true;

  /**
   * Creates a media item with a given id, title, and type.
   */
  protected Media(String id, String title, MediaType type) {
    this.id = Objects.requireNonNull(id, "id");
    this.title = Objects.requireNonNull(title, "title");
    this.type = Objects.requireNonNull(type, "type");
  }

  /** @return unique media id */
  public String getId() {
    return id;
  }

  /** @return title of the media */
  public String getTitle() {
    return title;
  }

  /** @return media type (BOOK or CD) */
  public MediaType getType() {
    return type;
  }

  /** @return true if the item is available to borrow */
  public boolean isAvailable() {
    return available;
  }

  /** Marks this item as borrowed. */
  public void markUnavailable() {
    this.available = false;
  }

  /** Marks this item as returned and available. */
  public void markAvailable() {
    this.available = true;
  }
}
