package com.library.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import java.util.Optional;

/**
 * Handles authentication concerns (US1.1/US1.2).
 */
public class AuthService {
  private final UserRepository userRepository;
  private User currentUser;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User login(String username, String password) {
    Optional<User> user = userRepository.findByUsername(username);
    if (user.isEmpty() || !user.get().passwordMatches(password)) {
      throw new LibraryException("Invalid credentials");
    }
    currentUser = user.get();
    return currentUser;
  }

  public void logout() {
    currentUser = null;
  }

  public User requireAdmin() {
    if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
      throw new LibraryException("Admin privileges required");
    }
    return currentUser;
  }

  public Optional<User> getCurrentUser() {
    return Optional.ofNullable(currentUser);
  }
}


