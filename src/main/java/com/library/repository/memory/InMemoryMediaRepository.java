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
 * Simple in-memory storage for demo purposes.
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
    return List.copyOf(mediaStore.values());
  }

  @Override
  public List<Media> search(String query) {
    if (query == null || query.isBlank()) {
      return new ArrayList<>(mediaStore.values());
    }
    String needle = query.toLowerCase();
    List<Media> matches = new ArrayList<>();
    for (Media media : mediaStore.values()) {
      if (media.getTitle().toLowerCase().contains(needle)) {
        matches.add(media);
        continue;
      }
      if (media instanceof Book book) {
        if (book.getAuthor().toLowerCase().contains(needle)
            || book.getIsbn().toLowerCase().contains(needle)) {
          matches.add(media);
        }
      }
    }
    return matches;
  }
  @Override
  public void delete(String id) {
      mediaStore.remove(id);
  }

}


