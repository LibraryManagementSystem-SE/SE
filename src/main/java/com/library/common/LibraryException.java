package com.library.common;

/**
 * A general-purpose exception used throughout the library system.
 *
 * <p>This exception is thrown for any application-level errors such as
 * invalid user operations, authentication failures, or rule violations.
 * Using a custom exception keeps error handling consistent across the
 * whole system.</p>
 */
public class LibraryException extends RuntimeException {

  /**
   * Creates a new exception with a descriptive error message.
   *
   * @param message a brief explanation of the problem
   */
  public LibraryException(String message) {
    super(message);
  }

  /**
   * Creates a new exception with a message and an underlying cause.
   * This is useful when wrapping lower-level exceptions.
   *
   * @param message a description of the error
   * @param cause the original exception that triggered this error
   */
  public LibraryException(String message, Throwable cause) {
    super(message, cause);
  }
}
