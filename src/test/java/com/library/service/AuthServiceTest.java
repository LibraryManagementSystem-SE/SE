package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import com.library.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();

        // Admin
        userRepository.save(new User("1", "admin", "Admin", UserRole.ADMIN, "pass"));

        // Non-admin user
        userRepository.save(new User("2", "bob", "Bob", UserRole.MEMBER, "pw"));

        authService = new AuthService(userRepository);
    }


    @Test
    void loginWithValidCredentialsSucceeds() {
        User user = authService.login("admin", "pass");
        assertEquals("Admin", user.getName());
    }

    @Test
    void loginWithInvalidCredentialsThrows() {
        assertThrows(LibraryException.class, () -> authService.login("admin", "wrong"));
    }

    @Test
    void requireAdminEnforcesRole() {
        authService.login("admin", "pass");
        assertDoesNotThrow(() -> authService.requireAdmin());
        authService.logout();
        assertThrows(LibraryException.class, () -> authService.requireAdmin());
    }

    @Test
    void loginFailsWithUnknownUsername() {
        assertThrows(LibraryException.class, () -> authService.login("unknown", "pass"));
    }

    @Test
    void logoutClearsCurrentUser() {
        authService.login("admin", "pass");
        authService.logout();
        assertTrue(authService.getCurrentUser().isEmpty());
    }

    @Test
    void getCurrentUserReturnsUserAfterLogin() {
        authService.login("admin", "pass");
        assertTrue(authService.getCurrentUser().isPresent());
        assertEquals("admin", authService.getCurrentUser().get().getUsername());
    }

    @Test
    void requireAdminFailsWhenUserIsNotAdmin() {
        authService.login("bob", "pw"); // Member
        assertThrows(LibraryException.class, () -> authService.requireAdmin());
    }

    @Test
    void getCurrentUserEmptyWhenNeverLoggedIn() {
        assertTrue(authService.getCurrentUser().isEmpty());
    }
}

