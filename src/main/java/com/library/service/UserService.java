package com.library.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.common.LibraryException;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages user lifecycle operations.
 */
public class UserService {
  private final UserRepository userRepository;
  private final LoanRepository loanRepository;
  private final AuthService authService;

  public UserService(
      UserRepository userRepository, LoanRepository loanRepository, AuthService authService) {
    this.userRepository = userRepository;
    this.loanRepository = loanRepository;
    this.authService = authService;
  }

  public User registerMember(String username, String name, String password) {
    ensureUsernameAvailable(username);
    User member =
        new User(UUID.randomUUID().toString(), username, name, UserRole.MEMBER, password);
    userRepository.save(member);
    return member;
  }

  public User registerAdmin(String username, String name, String password) {
    ensureUsernameAvailable(username);
    User admin = new User(UUID.randomUUID().toString(), username, name, UserRole.ADMIN, password);
    userRepository.save(admin);
    return admin;
  }

  public void unregister(String userId) {
    authService.requireAdmin();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new LibraryException("User not found"));
    boolean hasActiveLoans = !loanRepository.findActiveByUser(userId).isEmpty();
    if (hasActiveLoans) {
      throw new LibraryException("Cannot remove user with active loans");
    }
    if (user.hasOutstandingFines()) {
      throw new LibraryException("Cannot remove user with unpaid fines");
    }
    userRepository.delete(userId);
  }

  public java.util.Collection<User> listAllUsers() {
    authService.requireAdmin();
    return userRepository.findAll();
  }

  private void ensureUsernameAvailable(String username) {
    Optional<User> existing = userRepository.findByUsername(username);
    if (existing.isPresent()) {
      throw new LibraryException("Username already in use");
    }
  }
}


