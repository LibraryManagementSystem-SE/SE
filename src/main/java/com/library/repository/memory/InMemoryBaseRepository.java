package com.library.repository.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Shared in-memory store for repositories to reduce duplication.
 */
abstract class InMemoryBaseRepository<T> {
  protected final Map<String, T> store = new LinkedHashMap<>();

  protected void saveInternal(String id, T entity) {
    store.put(id, entity);
  }

  protected Optional<T> findByIdInternal(String id) {
    return Optional.ofNullable(store.get(id));
  }

  protected Collection<T> findAllInternal() {
    return Collections.unmodifiableCollection(store.values());
  }

  protected void deleteInternal(String id) {
    store.remove(id);
  }

  protected boolean existsInternal(String id) {
    return store.containsKey(id);
  }
}
