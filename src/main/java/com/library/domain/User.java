package com.library.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents both administrators and members in the library system.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String username;
    private final String name;
    private final UserRole role;
    private String password;

    // Fine balance in NIS
    private BigDecimal fineBalance = BigDecimal.ZERO;

    // Active loans stored as loan IDs (loan objects stored elsewhere)
    private final Set<String> activeLoanIds = new HashSet<>();


    /**
     * Main constructor used in the system.
     */
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

    public String getPassword() {
        return password;
    }

    public BigDecimal getFineBalance() {
        return fineBalance;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }


    // ===============================
    // Loan Management
    // ===============================

    public void addLoan(String loanId) {
        activeLoanIds.add(loanId);
    }

    public void closeLoan(String loanId) {
        activeLoanIds.remove(loanId);
    }

    public Set<String> getActiveLoanIds() {
        return Collections.unmodifiableSet(activeLoanIds);
    }

    public boolean hasActiveLoans() {
        return !activeLoanIds.isEmpty();
    }


    public void addFine(BigDecimal amount) {
        if (amount.signum() > 0) {
            fineBalance = fineBalance.add(amount);
        }
    }

    public void payFine(BigDecimal amount) {
        if (amount.signum() > 0) {
            fineBalance = fineBalance.subtract(amount);
            if (fineBalance.signum() < 0) {
                fineBalance = BigDecimal.ZERO;
            }
        }
    }

    public boolean hasOutstandingFines() {
        return fineBalance.signum() > 0;
    }


    public boolean passwordMatches(String candidate) {
        return Objects.equals(password, candidate);
    }

    public void changePassword(String newPassword) {
        this.password = Objects.requireNonNull(newPassword, "newPassword");
    }


    /**
     * Retrieves the current fine amount for the logged-in user
     * @return BigDecimal representing the fine amount, or BigDecimal.ZERO if no fine exists
     */
    private BigDecimal fineAmount = BigDecimal.ZERO;  // Add this field at the top of your User class

    /**
     * Sets the fine amount for this user.
     * @param fineAmount the fine amount to set (can be null, will be converted to ZERO)
     */
    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount != null ? fineAmount : BigDecimal.ZERO;
    }



	/**
 * Gets the current fine balance.
 * @return the fine amount as a BigDecimal, never null (returns ZERO if no fine)
 */
public BigDecimal getFineAmount() {
    return getFineBalance();  // Reuse the existing getFineBalance() method
}
}
