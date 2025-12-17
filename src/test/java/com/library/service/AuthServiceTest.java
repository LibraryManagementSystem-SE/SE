package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.common.AuthService;
import com.library.common.LibraryException;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import com.library.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();

        // Admin user
        userRepository.save(
                new User("1", "admin", "Admin", UserRole.ADMIN, "pass")
        );

        // Member user
        userRepository.save(
                new User("2", "bob", "Bob", UserRole.MEMBER, "pw")
        );

        authService = new AuthService(userRepository);
    }

    @Test
    void loginWithValidCredentialsSucceeds() {
        User user = authService.login("admin", "pass");
        assertNotNull(user);
        assertEquals("Admin", user.getName());
        assertEquals(UserRole.ADMIN, user.getRole());
    }

    @Test
    void loginWithWrongPasswordThrows() {
        assertThrows(
                LibraryException.class,
                () -> authService.login("admin", "wrong")
        );
    }

    @Test
    void loginWithUnknownUsernameThrows() {
        assertThrows(
                LibraryException.class,
                () -> authService.login("unknown", "pass")
        );
    }

    @Test
    void requireAdminSucceedsForAdmin() {
        authService.login("admin", "pass");
        assertDoesNotThrow(() -> authService.requireAdmin());
    }

    @Test
    void requireAdminFailsForMember() {
        authService.login("bob", "pw");
        assertThrows(
                LibraryException.class,
                () -> authService.requireAdmin()
        );
    }

    @Test
    void requireAdminFailsWhenNotLoggedIn() {
        assertThrows(
                LibraryException.class,
                () -> authService.requireAdmin()
        );
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
        assertEquals(
                "admin",
                authService.getCurrentUser().get().getUsername()
        );
    }

    @Test
    void getCurrentUserEmptyWhenNeverLoggedIn() {
        assertTrue(authService.getCurrentUser().isEmpty());
    }
}
