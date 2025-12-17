package com.library.service;

import com.library.common.LibraryException;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class UserRegistrationService {
  private final UserRepository userRepository;

  public UserRegistrationService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User register(String username, String name, String password, UserRole role) {
    ensureUsernameAvailable(username);

    User user = new User(
        UUID.randomUUID().toString(),
        username,
        name,
        role,
        password
    );

    userRepository.save(user);
    return user;
  }

  private void ensureUsernameAvailable(String username) {
    Optional<User> existing = userRepository.findByUsername(username);
    if (existing.isPresent()) {
      throw new LibraryException("Username already in use");
    }
  }
}
