package com.library.admin.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.common.LibraryException;
import com.library.common.AuthService;
import com.library.repository.UserRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for admin-specific operations.
 */
public class AdminService {
    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Register a new admin user.
     */
    public User registerAdmin(String username, String name, String password) {
        ensureUsernameAvailable(username);
        User admin = new User(
            UUID.randomUUID().toString(), 
            username, 
            name, 
            UserRole.ADMIN, 
            password
        );
        userRepository.save(admin);
        return admin;
    }

    /**
     * Unregister a user (admin only).
     */
    public void unregisterUser(String userId) {
        authService.requireAdmin();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new LibraryException("User not found"));
        
        if (user.hasActiveLoans()) {
            throw new LibraryException("Cannot remove user with active loans");
        }
        if (user.hasOutstandingFines()) {
            throw new LibraryException("Cannot remove user with unpaid fines");
        }
        userRepository.delete(userId);
    }

    /**
     * List all users (admin only).
     */
    public Collection<User> listAllUsers() {
        authService.requireAdmin();
        return userRepository.findAll();
    }

    private void ensureUsernameAvailable(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new LibraryException("Username already in use");
        }
    }
}
