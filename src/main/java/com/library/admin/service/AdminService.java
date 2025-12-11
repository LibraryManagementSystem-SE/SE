package com.library.admin.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.common.LibraryException;
import com.library.common.AuthService;
import com.library.repository.UserRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that provides administrative features such as creating admin
 * accounts, removing users, and viewing all registered users.
 * 
 * <p>All operations here are intended for system administrators, and most
 * methods enforce admin-only access through {@link AuthService}.</p>
 */
public class AdminService {

    /** Repository used for storing and retrieving user information. */
    private final UserRepository userRepository;

    /** Authentication service used for checking admin permissions. */
    private final AuthService authService;

    /**
     * Creates a new {@code AdminService}.
     *
     * @param userRepository the repository responsible for user persistence
     * @param authService service handling authentication and authorization checks
     */
    public AdminService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Registers a new admin user in the system.
     *
     * <p>The method ensures that the chosen username is not already in use
     * before creating the new admin account.</p>
     *
     * @param username the desired username for the admin account
     * @param name the display name of the admin
     * @param password the password for the admin user
     * @return the newly created admin user
     * @throws LibraryException if the username is already taken
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
     * Removes a user account from the system.
     *
     * <p>This action is restricted to administrators. A user can only be removed
     * if they have no active loans and no unpaid fines. Any violation results
     * in a {@link LibraryException} being thrown.</p>
     *
     * @param userId the ID of the user to remove
     * @throws LibraryException if the user does not exist, has active loans,
     *                          has outstanding fines, or if the caller is not admin
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
     * Returns a list of all users registered in the system.
     *
     * <p>Only administrators are allowed to access the full user list.</p>
     *
     * @return a collection containing all users
     * @throws LibraryException if the caller does not have admin privileges
     */
    public Collection<User> listAllUsers() {
        authService.requireAdmin();
        return userRepository.findAll();
    }

    /**
     * Checks whether the given username is already in use.
     *
     * @param username the username to validate
     * @throws LibraryException if another user already has this username
     */
    private void ensureUsernameAvailable(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new LibraryException("Username already in use");
        }
    }
}
