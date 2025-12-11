package com.library.domain;

import java.util.Objects;

/**
 * Represents a book in the library collection.
 *
 * <p>A {@code Book} is a type of {@link Media} that has an author and an ISBN.
 * This class stores the book-specific details while inheriting the common
 * media attributes such as ID, title, and availability state.</p>
 */
public class Book extends Media {

  /** The author of the book. */
  private final String author;

  /** The book's ISBN identifier. */
  private final String isbn;

  /**
   * Creates a new {@code Book} with its basic identifying information.
   *
   * @param id    unique media ID used internally by the system
   * @param title the book title
   * @param author the author's name; must not be null
   * @param isbn the ISBN number; must not be null
   */
  public Book(String id, String title, String author, String isbn) {
    super(id, title, MediaType.BOOK);
    this.author = Objects.requireNonNull(author, "author");
    this.isbn = Objects.requireNonNull(isbn, "isbn");
  }

  /**
   * Returns the author of the book.
   *
   * @return the author's name
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Returns the ISBN assigned to this book.
   *
   * @return the isbn string
   */
  public String getIsbn() {
    return isbn;
  }
}
