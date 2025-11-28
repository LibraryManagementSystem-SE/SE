package com.library.repository.memory;

import com.library.domain.Loan;
import com.library.repository.LoanRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    List<Loan> active = new ArrayList<>();
    for (Loan loan : loans.values()) {
      if (loan.getUserId().equals(userId) && !loan.isReturned()) {
        active.add(loan);
      }
    }
    return active;
  }

  @Override
  public Optional<Loan> findActiveByMedia(String mediaId) {
    return loans.values().stream()
        .filter(loan -> loan.getMediaId().equals(mediaId) && !loan.isReturned())
        .findFirst();
  }

  @Override
  public Collection<Loan> findAll() {
    return List.copyOf(loans.values());
  }
}