package com.library.service;

import com.library.domain.Book;
import com.library.domain.Media;
import com.library.domain.MediaType;
import com.library.repository.MediaRepository;
import java.util.List;
import java.util.UUID;

/**
 * Provides CRUD-style operations for media.
 */
public class CatalogService {
  private final MediaRepository mediaRepository;
  private final AuthService authService;

  public CatalogService(MediaRepository mediaRepository, AuthService authService) {
    this.mediaRepository = mediaRepository;
    this.authService = authService;
  }

  public Book addBook(String title, String author, String isbn) {
    authService.requireAdmin();
    Book book = new Book(UUID.randomUUID().toString(), title, author, isbn);
    mediaRepository.save(book);
    return book;
  }

  public Media addCd(String title, String artist) {
    authService.requireAdmin();
    com.library.domain.CD cd =
        new com.library.domain.CD(UUID.randomUUID().toString(), title, artist);
    mediaRepository.save(cd);
    return cd;
  }

  public List<Media> search(String term) {
    return mediaRepository.search(term);
  }

  public List<Media> listByType(MediaType type) {
    return mediaRepository.findAll().stream().filter(media -> media.getType() == type).toList();
  }
}


