package com.library.admin.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.common.LibraryException;
import com.library.common.AuthService;
import com.library.repository.UserRepository;
import com.library.service.UserRegistrationService;

import java.util.Collection;

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

    /** Shared service responsible for user registration logic. */
    private final UserRegistrationService registrationService;

    /**
     * Creates a new {@code AdminService}.
     *
     * @param userRepository the repository responsible for user persistence
     * @param authService service handling authentication and authorization checks
     */
    public AdminService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.registrationService = new UserRegistrationService(userRepository);
    }

    /**
     * Registers a new admin user in the system.
     *
     * <p>This action is restricted to administrators.</p>
     *
     * @param username the desired username for the admin account
     * @param name the display name of the admin
     * @param password the password for the admin user
     * @return the newly created admin user
     * @throws LibraryException if the username is already taken
     */
    public User registerAdmin(String username, String name, String password) {
        authService.requireAdmin();
        return registrationService.register(
                username,
                name,
                password,
                UserRole.ADMIN
        );
    }

    /**
     * Removes a user account from the system.
     *
     * <p>This action is restricted to administrators. A user can only be removed
     * if they have no active loans and no unpaid fines.</p>
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
     */
    public Collection<User> listAllUsers() {
        authService.requireAdmin();
        return userRepository.findAll();
    }
}
