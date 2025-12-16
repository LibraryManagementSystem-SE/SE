package com.library.service;

import com.library.common.LibraryException;
import com.library.domain.Loan;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private LoanRepository loanRepository;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        loanRepository = mock(LoanRepository.class);
        authService = mock(AuthService.class);
        userService = new UserService(userRepository, loanRepository, authService);
    }

    @Test
    void registerMember_success() {
        when(userRepository.findByUsername("tala")).thenReturn(Optional.empty());

        User user = userService.registerMember("tala", "Tala", "1234");

        assertNotNull(user);
        assertEquals(UserRole.MEMBER, user.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerAdmin_success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        User user = userService.registerAdmin("admin", "Admin", "admin123");

        assertEquals(UserRole.ADMIN, user.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerFails_whenUsernameAlreadyExists() {
        when(userRepository.findByUsername("tala"))
                .thenReturn(Optional.of(mock(User.class)));

        assertThrows(
                LibraryException.class,
                () -> userService.registerMember("tala", "Tala", "1234")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void unregister_success() {
        User user = mock(User.class);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(loanRepository.findActiveByUser("1")).thenReturn(Collections.emptyList());
        when(user.hasOutstandingFines()).thenReturn(false);

        userService.unregister("1");

        verify(userRepository).delete("1");
    }

    @Test
    void unregisterFails_whenUserNotFound() {
        doNothing().when(authService).requireAdmin();
        when(userRepository.findById("1")).thenReturn(Optional.empty());

        assertThrows(
                LibraryException.class,
                () -> userService.unregister("1")
        );
    }

    @Test
    void unregisterFails_whenUserHasActiveLoans() {
        User user = mock(User.class);

        doNothing().when(authService).requireAdmin();
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(loanRepository.findActiveByUser("1"))
        .thenReturn(Collections.singletonList(mock(Loan.class)));

        assertThrows(
                LibraryException.class,
                () -> userService.unregister("1")
        );

        verify(userRepository, never()).delete(any());
    }

    @Test
    void unregisterFails_whenUserHasOutstandingFines() {
        User user = mock(User.class);

        doNothing().when(authService).requireAdmin();
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(loanRepository.findActiveByUser("1")).thenReturn(Collections.emptyList());
        when(user.hasOutstandingFines()).thenReturn(true);

        assertThrows(
                LibraryException.class,
                () -> userService.unregister("1")
        );

        verify(userRepository, never()).delete(any());
    }

    @Test
    void listAllUsers_requiresAdmin() {
        doNothing().when(authService).requireAdmin();
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertNotNull(userService.listAllUsers());

        verify(authService).requireAdmin();
        verify(userRepository).findAll();
    }
}
