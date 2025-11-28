package com.library.system;

/**
 * Boots the CLI application.
 */
public class LibraryApplication {
  public static void main(String[] args) {
    LibraryEnvironment environment = LibraryEnvironment.bootstrap();
    new LibraryCli(environment).run();
  }
}


