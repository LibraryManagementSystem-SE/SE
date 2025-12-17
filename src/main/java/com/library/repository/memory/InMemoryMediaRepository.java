package com.library.repository.memory;

import com.library.domain.Book;
import com.library.domain.Media;
import com.library.repository.MediaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of MediaRepository.
 * Provides basic CRUD and search functionality.
 */
public class InMemoryMediaRepository implements MediaRepository {

  private final Map<String, Media> mediaStore = new ConcurrentHashMap<>();

  @Override
  public void save(Media media) {
    mediaStore.put(media.getId(), media);
  }

  @Override
  public Optional<Media> findById(String id) {
    return Optional.ofNullable(mediaStore.get(id));
  }

  @Override
  public Collection<Media> findAll() {
    return mediaStore.values();
  }

  @Override
  public List<Media> search(String query) {
    if (query == null || query.isBlank()) {
      return new ArrayList<>(mediaStore.values());
    }

    String needle = query.toLowerCase();
    List<Media> results = new ArrayList<>();

    for (Media media : mediaStore.values()) {
      if (matches(media, needle)) {
        results.add(media);
      }
    }

    return results;
  }

  @Override
  public void delete(String id) {
    mediaStore.remove(id);
  }

  /**
   * Checks whether a media item matches the given search keyword.
   */
  private boolean matches(Media media, String needle) {
    if (media.getTitle().toLowerCase().contains(needle)) {
      return true;
    }

    if (media instanceof Book book) {
      return book.getAuthor().toLowerCase().contains(needle)
          || book.getIsbn().toLowerCase().contains(needle);
    }

    return false;
  }
}
