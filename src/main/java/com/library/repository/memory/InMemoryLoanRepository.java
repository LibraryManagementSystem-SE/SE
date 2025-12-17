package com.library.repository.memory;

import com.library.domain.Loan;
import com.library.repository.LoanRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of LoanRepository.
 */
public class InMemoryLoanRepository implements LoanRepository {

  private final Map<String, Loan> loans = new ConcurrentHashMap<>();

  @Override
  public void save(Loan loan) {
    loans.put(loan.getId(), loan);
  }

  @Override
  public Optional<Loan> findById(String id) {
    return Optional.ofNullable(loans.get(id));
  }

  @Override
  public List<Loan> findActiveByUser(String userId) {
    List<Loan> activeLoans = new ArrayList<>();

    for (Loan loan : loans.values()) {
      if (isActiveLoanForUser(loan, userId)) {
        activeLoans.add(loan);
      }
    }

    return activeLoans;
  }

  @Override
  public Optional<Loan> findActiveByMedia(String mediaId) {
    return loans.values().stream()
        .filter(loan -> isActiveLoanForMedia(loan, mediaId))
        .findFirst();
  }

  @Override
  public Collection<Loan> findAll() {
    return loans.values();
  }

  @Override
  public void delete(String id) {
    loans.remove(id);
  }

  /**
   * Checks whether a loan is active for a given user.
   */
  private boolean isActiveLoanForUser(Loan loan, String userId) {
    return !loan.isReturned() && loan.getUserId().equals(userId);
  }

  /**
   * Checks whether a loan is active for a given media item.
   */
  private boolean isActiveLoanForMedia(Loan loan, String mediaId) {
    return !loan.isReturned() && loan.getMediaId().equals(mediaId);
  }
}
