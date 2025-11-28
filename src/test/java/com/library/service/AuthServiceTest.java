package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

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
    userRepository.save(new User("1", "admin", "Admin", UserRole.ADMIN, "pass"));
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
}


