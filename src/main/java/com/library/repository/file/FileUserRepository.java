package com.library.repository.file;

import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.repository.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simple file-based implementation of {@link UserRepository}.
 *
 * <p>Stores users (including admins) in a text file, one user per line, with fields separated by
 * ';':
 *
 * <pre>
 * id;username;name;role;password;fineBalance
 * </pre>
 *
 * <p>This is intentionally simple: the whole file is read/written on each modification.
 */
public class FileUserRepository implements UserRepository {

  private final Path usersFile;

  public FileUserRepository() {
    this(Paths.get("data", "users.txt"));
  }

  public FileUserRepository(Path usersFile) {
    this.usersFile = usersFile;
  }

  @Override
  public synchronized void save(User user) {
    Map<String, User> byId = new HashMap<>();
    Map<String, User> byUsername = new HashMap<>();
    loadAllInto(byId, byUsername);
    byId.put(user.getId(), user);
    byUsername.put(user.getUsername(), user);
    writeAll(byId.values());
  }

  @Override
  public Optional<User> findById(String id) {
    Map<String, User> byId = new HashMap<>();
    Map<String, User> byUsername = new HashMap<>();
    loadAllInto(byId, byUsername);
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    Map<String, User> byId = new HashMap<>();
    Map<String, User> byUsername = new HashMap<>();
    loadAllInto(byId, byUsername);
    return Optional.ofNullable(byUsername.get(username));
  }

  @Override
  public Collection<User> findAll() {
    Map<String, User> byId = new HashMap<>();
    Map<String, User> byUsername = new HashMap<>();
    loadAllInto(byId, byUsername);
    return new ArrayList<>(byId.values());
  }

  @Override
  public synchronized void delete(String id) {
    Map<String, User> byId = new HashMap<>();
    Map<String, User> byUsername = new HashMap<>();
    loadAllInto(byId, byUsername);
    User removed = byId.remove(id);
    if (removed != null) {
      byUsername.remove(removed.getUsername());
      writeAll(byId.values());
    }
  }

  private void loadAllInto(Map<String, User> byId, Map<String, User> byUsername) {
    byId.clear();
    byUsername.clear();
    if (!Files.exists(usersFile)) {
      return;
    }
    try {
      List<String> lines = Files.readAllLines(usersFile, StandardCharsets.UTF_8);
      for (String line : lines) {
        if (line.isBlank() || line.startsWith("#")) {
          continue;
        }
        String[] parts = line.split(";");
        // id;username;name;role;password;fineBalance
        if (parts.length < 6) {
          continue;
        }
        String id = parts[0];
        String username = parts[1];
        String name = parts[2];
        UserRole role = UserRole.valueOf(parts[3]);
        String password = parts[4];
        BigDecimal fineBalance = new BigDecimal(parts[5]);

        // Simple migration logic: older files may have stored the username as the password.
        // If we detect that situation, we switch to sensible defaults so login works
        // without the user having to manually edit the file.
        if (role == UserRole.ADMIN && "admin".equals(password)) {
          password = "admin123";
        } else if (role == UserRole.MEMBER && username.equals(password)) {
          password = "123";
        }

        User user = new User(id, username, name, role, password);
        if (fineBalance.signum() > 0) {
          user.addFine(fineBalance);
        }
        byId.put(id, user);
        byUsername.put(username, user);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read users file: " + usersFile, e);
    }
  }

  private void writeAll(Collection<User> users) {
    try {
      Files.createDirectories(usersFile.getParent());
      List<String> lines = new ArrayList<>();
      for (User user : users) {
        // id;username;name;role;password;fineBalance
        String line =
            String.join(
                ";",
                escape(user.getId()),
                escape(user.getUsername()),
                escape(user.getName()),
                user.getRole().name(),
                // Passwords are stored in plain text here – fine for this simple demo,
                // but not for production.
                escapeForPassword(user),
                user.getFineBalance().toPlainString());
        lines.add(line);
      }
      Files.write(usersFile, lines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write users file: " + usersFile, e);
    }
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }
    return value.replace(";", ",");
  }

  private String escapeForPassword(User user) {
    // In this simple educational project we persist the raw password in plain text.
    // Do NOT do this in a real application – passwords should be hashed and never exposed.
    return escape(user.getPassword());
  }
}


