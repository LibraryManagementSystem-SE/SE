package com.library.repository;

import com.library.domain.User;
import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
  void save(User user);

  Optional<User> findById(String id);

  Optional<User> findByUsername(String username);

  Collection<User> findAll();

  void delete(String id);
}


