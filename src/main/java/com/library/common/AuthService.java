package com.library.common;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import java.util.Objects;
import java.util.Optional;

/**
 * Handles all authentication-related functionality such as logging in,
 * logging out, and verifying whether the current user has the required
 * permissions. This class acts as the central point for user identity
 * during system operations.
 */
public class AuthService {

  /** Repository used to look up users during login. */
  private final UserRepository userRepository;

  /** Holds the user who is currently logged in (if any). */
  private User currentUser;

  /**
   * Creates a new authentication service.
   *
   * @param userRepository the repository used for user lookups
   */
  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Attempts to authenticate a user using the provided username and password.
   *
   * <p>If the username does not exist or the password is incorrect, a
   * {@link LibraryException} is thrown. Upon successful authentication, the
   * user is stored as the currently logged-in user.</p>
   *
   * @param username the username entered by the user
   * @param password the password entered by the user
   * @return the authenticated {@link User}
   * @throws LibraryException if credentials are invalid
   */
  public User login(String username, String password) {
    Objects.requireNonNull(username, "Username cannot be null");
    Objects.requireNonNull(password, "Password cannot be null");

    // First get the optional user
    Optional<User> userOptional = userRepository.findByUsername(username);

    // If user doesn't exist, throw exception
    if (!userOptional.isPresent()) {
      throw new LibraryException("Invalid username or password");
    }

    // Get the user from the optional
    User user = userOptional.get();

    // Verify password
    if (!user.passwordMatches(password)) {
      throw new LibraryException("Invalid username or password");
    }

    // Set current user and return
    currentUser = user;
    return currentUser;
  }

  /**
   * Logs out the current user by clearing the stored session reference.
   */
  public void logout() {
    currentUser = null;
  }

  /**
   * Checks that the currently logged-in user is an administrator.
   *
   * <p>This method is typically called before performing admin-only
   * operations such as unregistering users or listing all accounts.</p>
   *
   * @return the currently logged-in admin user
   * @throws LibraryException if no user is logged in or the user is not an admin
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

  /**
   * Returns the user who is currently logged in.
   *
   * @return an {@link Optional} containing the current user,
   *         or empty if no one is logged in
   */
  public Optional<User> getCurrentUser() {
    return Optional.ofNullable(currentUser);
  }
}
