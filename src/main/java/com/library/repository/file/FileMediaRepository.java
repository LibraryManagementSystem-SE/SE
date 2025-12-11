package com.library.repository.file;

import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.Media;
import com.library.domain.MediaType;
import com.library.repository.MediaRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Simple file-based implementation of {@link MediaRepository}.
 *
 * <p>Stores books and CDs in text files where each line represents one record with fields separated
 * by ';'.
 *
 * <p>Books file format (`books.txt`):
 *
 * <pre>
 * id;title;author;isbn
 * </pre>
 *
 * <p>CDs file format (`cds.txt`):
 *
 * <pre>
 * id;title;artist
 * </pre>
 *
 * <p>This is intentionally simple and not optimized – it rewrites the whole file on each save.
 */
public class FileMediaRepository implements MediaRepository {

  private final Path booksFile;
  private final Path cdsFile;

  public FileMediaRepository() {
    this(Paths.get("data", "books.txt"));
  }

  public FileMediaRepository(Path booksFile) {
    this.booksFile = booksFile;
    this.cdsFile = booksFile.getParent().resolve("cds.txt");
  }

  @Override
  public synchronized void save(Media media) {
    // Load all, replace or add, then write back.
    List<Media> all = new ArrayList<>(findAll());
    all.removeIf(m -> m.getId().equals(media.getId()));
    all.add(media);
    writeAll(all);
  }

  @Override
  public Optional<Media> findById(String id) {
    return findAll().stream().filter(m -> m.getId().equals(id)).findFirst();
  }

  @Override
  public Collection<Media> findAll() {
    List<Media> result = new ArrayList<>();
    result.addAll(readBooks());
    result.addAll(readCds());
    return result;
  }

  @Override
  public List<Media> search(String query) {
      if (query == null || query.isBlank()) {
          return new ArrayList<>(findAll());
      }
      String needle = query.toLowerCase();
      List<Media> matches = new ArrayList<>();
      for (Media media : findAll()) {
          // Search by title for all media types
          if (media.getTitle().toLowerCase().contains(needle)) {
              matches.add(media);
              continue;
          }
          
          // For books, search by author and ISBN
          if (media.getType() == MediaType.BOOK && media instanceof Book book) {
              if (book.getAuthor().toLowerCase().contains(needle) || 
                  book.getIsbn().toLowerCase().contains(needle)) {
                  matches.add(media);
              }
          } 
          // For CDs, search by artist
          else if (media.getType() == MediaType.CD && media instanceof CD cd) {
              if (cd.getArtist().toLowerCase().contains(needle)) {
                  matches.add(media);
              }
          }
      }
      return matches;
  }

  private void writeAll(List<Media> all) {
    try {
      Files.createDirectories(booksFile.getParent());
      List<String> bookLines = new ArrayList<>();
      List<String> cdLines = new ArrayList<>();
      for (Media media : all) {
        if (media.getType() == MediaType.BOOK && media instanceof Book book) {
          // id;title;author;isbn
          String line =
              String.join(
                  ";",
                  escape(book.getId()),
                  escape(book.getTitle()),
                  escape(book.getAuthor()),
                  escape(book.getIsbn()));
          bookLines.add(line);
        } else if (media.getType() == MediaType.CD && media instanceof CD cd) {
          // id;title;artist
          String line =
              String.join(
                  ";",
                  escape(cd.getId()),
                  escape(cd.getTitle()),
                  escape(cd.getArtist()));
          cdLines.add(line);
        }
      }
      Files.write(booksFile, bookLines, StandardCharsets.UTF_8);
      Files.write(cdsFile, cdLines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to write media files: " + booksFile + " and " + cdsFile, e);
    }
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }
    return value.replace(";", ",");
  }

  private List<Media> readBooks() {
    List<Media> result = new ArrayList<>();
    if (!Files.exists(booksFile)) {
      return result;
    }
    try {
      List<String> lines = Files.readAllLines(booksFile, StandardCharsets.UTF_8);
      for (String line : lines) {
        if (line.isBlank() || line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(";");
        // Expect: id;title;author;isbn
        if (parts.length < 4) {
          continue;
        }
        String id = parts[0];
        String title = parts[1];
        String author = parts[2];
        String isbn = parts[3];
        result.add(new Book(id, title, author, isbn));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read books file: " + booksFile, e);
    }
    return result;
  }

  private List<Media> readCds() {
    List<Media> result = new ArrayList<>();
    if (!Files.exists(cdsFile)) {
      return result;
    }
    try {
      List<String> lines = Files.readAllLines(cdsFile, StandardCharsets.UTF_8);
      for (String line : lines) {
        if (line.isBlank() || line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(";");
        // Expect: id;title;artist
        if (parts.length < 3) {
          continue;
        }
        String id = parts[0];
        String title = parts[1];
        String artist = parts[2];
        result.add(new CD(id, title, artist));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read CDs file: " + cdsFile, e);
    }
    return result;
  }

  @Override
  public void delete(String id) {
	// TODO Auto-generated method stub
	
  }
}


