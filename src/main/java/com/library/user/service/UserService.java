package com.library.user.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.common.LibraryException;
import com.library.repository.UserRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for user-specific operations.
 */
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new member user.
     */
    public User registerMember(String username, String name, String password) {
        ensureUsernameAvailable(username);
        User member = new User(
            UUID.randomUUID().toString(),
            username,
            name,
            UserRole.MEMBER,
            password
        );
        userRepository.save(member);
        return member;
    }

    /**
     * Change a user's password.
     */
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new LibraryException("User not found"));
        
        if (!user.passwordMatches(currentPassword)) {
            throw new LibraryException("Current password is incorrect");
        }
        
        user.changePassword(newPassword);
        userRepository.save(user);
    }

    /**
     * Get user profile information.
     */
    public User getUserProfile(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new LibraryException("User not found"));
    }

    private void ensureUsernameAvailable(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new LibraryException("Username already in use");
        }
    }
}
