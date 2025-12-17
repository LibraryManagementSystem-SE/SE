package com.library.service;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.common.AuthService;
import com.library.common.LibraryException;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;

/**
 * Manages user lifecycle operations.
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

    public User registerMember(String username, String name, String password) {
        return registrationService.register(
                username,
                name,
                password,
                UserRole.MEMBER
        );
    }

    public User registerAdmin(String username, String name, String password) {
        return registrationService.register(
                username,
                name,
                password,
                UserRole.ADMIN
        );
    }

    public void unregister(String userId) {
        authService.requireAdmin();

        User user = userRepository.findById(userId)
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

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
