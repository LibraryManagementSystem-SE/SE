package com.library.repository.memory;

import com.library.domain.User;
import com.library.repository.UserRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
  private final Map<String, User> byId = new ConcurrentHashMap<>();
  private final Map<String, User> byUsername = new ConcurrentHashMap<>();

  @Override
  public void save(User user) {
    byId.put(user.getId(), user);
    byUsername.put(user.getUsername(), user);
  }

  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(byUsername.get(username));
  }

  @Override
  public Collection<User> findAll() {
    return java.util.List.copyOf(byId.values());
  }

  @Override
  public void delete(String id) {
    User removed = byId.remove(id);
    if (removed != null) {
      byUsername.remove(removed.getUsername());
    }
  }
}


