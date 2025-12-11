package com.library.domain;

import java.util.Objects;

/**
 * Base class for all media items in the library (Book, CD).
 *
 * <p>Each {@code Media} instance represents a title that can have multiple physical copies
 * in the library, tracked via {@link #quantity}. Availability is derived from this quantity:
 * when {@code quantity > 0} the item is considered available, otherwise it is unavailable.
 */
public abstract class Media {

  private final String id;
  private final String title;
  private final MediaType type;

  /** Number of copies currently available in the library. */
  private int quantity = 1;

  /** Cached availability flag, kept in sync with {@link #quantity}. */
  private boolean available = true;

  /**
   * Creates a media item with a given id, title, and type.
   */
  protected Media(String id, String title, MediaType type) {
    this.id = Objects.requireNonNull(id, "id");
    this.title = Objects.requireNonNull(title, "title");
    this.type = Objects.requireNonNull(type, "type");
    // quantity defaults to 1; availability already true
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

  /**
   * @return number of available copies of this media item.
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * Sets the current quantity, updating availability accordingly.
   *
   * <p>This is primarily used by persistence layers when loading from storage.</p>
   *
   * @param quantity new quantity (negative values are treated as zero)
   */
  public void setQuantity(int quantity) {
    if (quantity < 0) {
      quantity = 0;
    }
    this.quantity = quantity;
    this.available = this.quantity > 0;
  }

  /** @return true if the item is available to borrow */
  public boolean isAvailable() {
    return available;
  }

  /**
   * Marks a single copy of this item as borrowed.
   *
   * <p>If at least one copy is available, the quantity is decreased by one. When the quantity
   * reaches zero, {@link #isAvailable()} will start returning {@code false}.</p>
   */
  public void markUnavailable() {
    if (quantity > 0) {
      quantity--;
    }
    if (quantity <= 0) {
      available = false;
    }
  }

  /**
   * Marks a single copy of this item as returned.
   *
   * <p>The quantity is increased by one and availability is updated accordingly.</p>
   */
  public void markAvailable() {
    quantity++;
    if (quantity > 0) {
      available = true;
    }
  }
}
