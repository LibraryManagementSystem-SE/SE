package com.library.repository;

import com.library.domain.Media;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MediaRepository {
  void save(Media media);

  Optional<Media> findById(String id);

  Collection<Media> findAll();

  List<Media> search(String query);
}


