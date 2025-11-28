package com.library.service;

/**
 * Domain specific runtime exception.
 */
public class LibraryException extends RuntimeException {
  public LibraryException(String message) {
    super(message);
  }
}


