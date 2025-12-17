package com.library.service;

import com.library.common.AuthService;
import com.library.common.LibraryException;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;

import java.util.Collection;

/**
 * Manages user lifecycle operations.
 * Admin-only operations are enforced via AuthService.
 */
public class UserService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final AuthService authService;
    private final UserRegistrationService registrationService;

    public UserService(
            UserRepository userRepository,
            LoanRepository loanRepository,
            AuthService authService) {

        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.authService = authService;
        this.registrationService = new UserRegistrationService(userRepository);
    }

    /* =========================
       Registration
       ========================= */

    public User registerMember(String username, String name, String password) {
        return registrationService.register(
                username,
                name,
                password,
                UserRole.MEMBER
        );
    }

    public User registerAdmin(String username, String name, String password) {
        authService.requireAdmin();
        return registrationService.register(
                username,
                name,
                password,
                UserRole.ADMIN
        );
    }

    /* =========================
       Admin-only management
       ========================= */

    public void unregisterUser(String userId) {
        authService.requireAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LibraryException("User not found"));

        if (!loanRepository.findActiveByUser(userId).isEmpty()) {
            throw new LibraryException("Cannot remove user with active loans");
        }

        if (user.hasOutstandingFines()) {
            throw new LibraryException("Cannot remove user with unpaid fines");
        }

        userRepository.delete(userId);
    }

    public Collection<User> listAllUsers() {
        authService.requireAdmin();
        return userRepository.findAll();
    }

    /* =========================
       Infrastructure
       ========================= */

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
