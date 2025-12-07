package com.library.repository;

import com.library.domain.Loan;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {
  void save(Loan loan);

  Optional<Loan> findById(String id);

  List<Loan> findActiveByUser(String userId);

  Optional<Loan> findActiveByMedia(String mediaId);

  Collection<Loan> findAll();

  void delete(String id);
}


