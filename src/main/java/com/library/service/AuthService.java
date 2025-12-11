package com.library.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import java.util.List;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

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

  public void updateCurrentUser(User updatedUser) {
	    if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
	        currentUser = updatedUser;
	        userRepository.save(currentUser);
	    }
	}
  public void register(String username, String name, String password) {
	    // Validate input
	    if (username == null || username.trim().isEmpty()) {
	        throw new IllegalArgumentException("Username cannot be empty");
	    }
	    if (name == null || name.trim().isEmpty()) {
	        throw new IllegalArgumentException("Name cannot be empty");
	    }
	    if (password == null || password.trim().isEmpty()) {
	        throw new IllegalArgumentException("Password cannot be empty");
	    }

	    // Check if username already exists
	    if (userRepository.findByUsername(username).isPresent()) {
	        throw new IllegalStateException("Username already exists");
	    }

	    // Create and save new user
	    User newUser = new User(
	    	    UUID.randomUUID().toString(), 
	    	    username.trim(), 
	    	    name.trim(), 
	    	    UserRole.MEMBER,
	    	    password.trim()
	    	);

	    userRepository.save(newUser);
	}
}


