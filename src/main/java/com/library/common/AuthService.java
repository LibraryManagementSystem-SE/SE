package com.library.common;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import java.util.Objects;
import java.util.Optional;

/**
 * handles authentication concerns (US1.1/US1.2).
 */
public class AuthService {
  private final UserRepository userRepository;
  private User currentUser;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * authenticates a user with the given username and password.
   * 
   * @param username the username to authenticate
   * @param password the password to verify
   * @return the authenticated user
   * @throws LibraryException if authentication fails
   */
  public User login(String username, String password) {
    Objects.requireNonNull(username, "Username cannot be null");
    Objects.requireNonNull(password, "Password cannot be null");
    /**
    // First get the optional user
     */
    Optional<User> userOptional = userRepository.findByUsername(username);
    /**
    // If user doesn't exist, throw exception
     */
    if (!userOptional.isPresent()) {
      throw new LibraryException("Invalid username or password");
    }
    /**
    // Get the user from the optional
     */
    User user = userOptional.get();
    /**
    // Verify password
     */
    if (!user.passwordMatches(password)) {
      throw new LibraryException("Invalid username or password");
    }
    
    /**
    // Set current user and return
    ///**
     */
    currentUser = user;
    return currentUser;
  }

  public void logout() {
    currentUser = null;
  }

  /**
   * Ensures the current user has admin privileges.
   * 
   * @return the current admin user
   * @throws LibraryException if no user is logged in or user is not an admin
   */
  public User requireAdmin() {
    if (currentUser == null) {
      throw new LibraryException("Authentication required");
    }
    if (!UserRole.ADMIN.equals(currentUser.getRole())) {
      throw new LibraryException("Admin privileges required");
    }
    return currentUser;
  }

  public Optional<User> getCurrentUser() {
    return Optional.ofNullable(currentUser);
  }
}
