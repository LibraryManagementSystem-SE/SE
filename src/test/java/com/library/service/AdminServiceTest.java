package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.admin.service.AdminService;
import com.library.common.AuthService;
import com.library.common.LibraryException;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.domain.Loan;

import com.library.repository.UserRepository;
import com.library.repository.LoanRepository;
import com.library.repository.memory.InMemoryUserRepository;
import com.library.repository.memory.InMemoryLoanRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

class AdminServiceTest {

    private AdminService adminService;
    private UserRepository userRepository;
    private LoanRepository loanRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        loanRepository = new InMemoryLoanRepository();
        authService = mock(AuthService.class);

        adminService = new AdminService(userRepository, authService);
    }

    private User mockAdmin() {
        User admin = new User("admin1", "admin", "Admin", UserRole.ADMIN, "pw");
        when(authService.requireAdmin()).thenReturn(admin);
        return admin;
    }

    // ---------------------------------------------------------
    // registerAdmin
    // ---------------------------------------------------------

    @Test
    void registerAdminSuccess() {
        User u = adminService.registerAdmin("boss", "Boss", "pw");
        assertEquals(UserRole.ADMIN, u.getRole());
        assertEquals("boss", u.getUsername());
    }

    @Test
    void registerAdminDuplicateUsernameThrows() {
        adminService.registerAdmin("admin", "A", "pw");

        assertThrows(LibraryException.class,
                () -> adminService.registerAdmin("admin", "B", "pw"));
    }

    // ---------------------------------------------------------
    // listAllUsers
    // ---------------------------------------------------------

    @Test
    void listAllUsersRequiresAdmin() {
        mockAdmin();

        adminService.registerAdmin("a1", "A1", "pw");
        adminService.registerAdmin("a2", "A2", "pw");

        var list = adminService.listAllUsers();
        assertEquals(2, list.size());
    }

    @Test
    void listAllUsersThrowsWhenNotAdmin() {
        doThrow(new LibraryException("Admin required"))
                .when(authService).requireAdmin();

        assertThrows(LibraryException.class,
                () -> adminService.listAllUsers());
    }

    // ---------------------------------------------------------
    // unregisterUser
    // ---------------------------------------------------------

    @Test
    void unregisterUserNotFoundThrows() {
        mockAdmin();

        assertThrows(LibraryException.class,
                () -> adminService.unregisterUser("missing"));
    }

    @Test
    void unregisterUserFailsWhenActiveLoans() {
        mockAdmin();

        User u = new User("u1", "bob", "Bob", UserRole.MEMBER, "pw");
        userRepository.save(u);

        // create active loan
        Loan loan = new Loan(
                "11",
                u.getId(),
                "media1",
                LocalDate.of(2025,1,1),
                LocalDate.of(2025,1,10)
        );
        loanRepository.save(loan);

        // IMPORTANT: this marks the loan as active for the user
        u.addLoan("11");

        assertThrows(LibraryException.class,
                () -> adminService.unregisterUser(u.getId()));
    }

    @Test
    void unregisterUserFailsWhenOutstandingFines() {
        mockAdmin();

        User u = new User("u1", "bob", "Bob", UserRole.MEMBER, "pw");
        u.addFine(BigDecimal.TEN);
        userRepository.save(u);

        assertThrows(LibraryException.class,
                () -> adminService.unregisterUser(u.getId()));
    }

    @Test
    void unregisterUserSucceeds() {
        mockAdmin();

        User u = new User("u1", "bob", "Bob", UserRole.MEMBER, "pw");
        userRepository.save(u);

        assertDoesNotThrow(() -> adminService.unregisterUser(u.getId()));
        assertTrue(userRepository.findById(u.getId()).isEmpty());
    }
}
