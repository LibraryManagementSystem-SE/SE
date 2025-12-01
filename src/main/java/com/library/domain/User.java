package com.library.domain;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


 //Represents both administrators and members.

public class User {
  private final String id;
  private final String username;
  private final String name;
  private final UserRole role;
  private String password;
  private BigDecimal fineBalance = BigDecimal.ZERO;
  private final Set<String> activeLoanIds = new HashSet<>();

  public User(String id, String username, String name, UserRole role, String password) {
    this.id = Objects.requireNonNull(id, "id");
    this.username = Objects.requireNonNull(username, "username");
    this.name = Objects.requireNonNull(name, "name");
    this.role = Objects.requireNonNull(role, "role");
    this.password = Objects.requireNonNull(password, "password");
  }

  public String getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getName() {
    return name;
  }

  public UserRole getRole() {
    return role;
  }

  // For this assignment-level project we expose the raw password so that
  // simple file-based persistence can store it. In a real system, you would
  // never expose or store raw passwords like this.
  public String getPassword() {
    return password;
  }

  public BigDecimal getFineBalance() {
    return fineBalance;
  }

  public boolean isAdmin() {
    return role == UserRole.ADMIN;
  }

  public boolean passwordMatches(String candidate) {
    return Objects.equals(password, candidate);
  }

  public void changePassword(String newPassword) {
    this.password = Objects.requireNonNull(newPassword, "newPassword");
  }

  public void addLoan(String loanId) {
    activeLoanIds.add(loanId);
  }

  public void closeLoan(String loanId) {
    activeLoanIds.remove(loanId);
  }

  public Set<String> getActiveLoanIds() {
    return Collections.unmodifiableSet(activeLoanIds);
  }

  public void addFine(BigDecimal amount) {
    if (amount.signum() <= 0) {
      return;
    }
    fineBalance = fineBalance.add(amount);
  }

  public void payFine(BigDecimal amount) {
    if (amount.signum() <= 0) {
      return;
    }
    fineBalance = fineBalance.subtract(amount);
    if (fineBalance.signum() < 0) {
      fineBalance = BigDecimal.ZERO;
    }
  }

  public boolean hasOutstandingFines() {
    return fineBalance.signum() > 0;
  }

  public boolean hasActiveLoans() {
    return !activeLoanIds.isEmpty();
  }
}
