package com.library.domain;
//sally
import java.util.Objects;


public class Book extends Media {
  private final String author;
  private final String isbn;

  public Book(String id, String title, String author, String isbn) {
    super(id, title, MediaType.BOOK);
    this.author = Objects.requireNonNull(author, "author");
    this.isbn = Objects.requireNonNull(isbn, "isbn");
  }

  public String getAuthor() {
    return author;
  }

  public String getIsbn() {
    return isbn;
  }
}
