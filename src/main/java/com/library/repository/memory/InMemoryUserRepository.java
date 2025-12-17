package com.library.repository.memory;

import com.library.domain.User;
import com.library.repository.UserRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of UserRepository.
 * Uses internal maps for fast lookup by id and username.
 */
public class InMemoryUserRepository implements UserRepository {

  private final Map<String, User> usersById = new ConcurrentHashMap<>();
  private final Map<String, String> usernameToId = new ConcurrentHashMap<>();

  @Override
  public void save(User user) {
    usersById.put(user.getId(), user);
    usernameToId.put(user.getUsername(), user.getId());
  }

  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(usersById.get(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    String userId = usernameToId.get(username);
    if (userId == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(usersById.get(userId));
  }

  @Override
  public Collection<User> findAll() {
    return usersById.values();
  }

  @Override
  public void delete(String id) {
    User removed = usersById.remove(id);
    if (removed != null) {
      usernameToId.remove(removed.getUsername());
    }
  }
}
