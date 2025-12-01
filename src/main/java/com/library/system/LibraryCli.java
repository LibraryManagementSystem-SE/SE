package com.library.system;

import com.library.domain.Media;
import com.library.domain.OverdueReport;
import com.library.domain.User;
import com.library.service.AuthService;
import com.library.service.BorrowService;
import com.library.service.CatalogService;
import com.library.service.FineService;
import com.library.service.LibraryException;
import com.library.service.ReminderService;
import com.library.service.UserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console based presentation layer.
 */
public class LibraryCli {
  private final AuthService authService;
  private final CatalogService catalogService;
  private final BorrowService borrowService;
  private final FineService fineService;
  private final ReminderService reminderService;
  private final UserService userService;
  private final LibraryEnvironment environment;
  private final Scanner scanner = new Scanner(System.in);

  public LibraryCli(LibraryEnvironment environment) {
    this.environment = environment;
    this.authService = environment.getAuthService();
    this.catalogService = environment.getCatalogService();
    this.borrowService = environment.getBorrowService();
    this.fineService = environment.getFineService();
    this.reminderService = environment.getReminderService();
    this.userService = environment.getUserService();
  }

  public void run() {
    System.out.println("=== Library Management System ===");
    boolean running = true;
    while (running) {
      showMenu();
      String choice = scanner.nextLine().trim();
      try {
        running = handleChoice(choice);
      } catch (LibraryException ex) {
        System.out.println("Error: " + ex.getMessage());
      } catch (Exception ex) {
        System.out.println("Unexpected error: " + ex.getMessage());
      }
    }
    System.out.println("Goodbye!");
  }

  private void showMenu() {
    System.out.println("\n=== Library Management System ===");
    Optional<User> currentUser = authService.getCurrentUser();
    
    if (currentUser.isEmpty()) {
      showInitialMenu();
    } else if (currentUser.get().isAdmin()) {
      showAdminMenu();
    } else {
      showMemberMenu();
    }
  }

  private void showInitialMenu() {
    System.out.println("\n=== Library Management System ===");
    System.out.println("1. Login");
    System.out.println("2. Register member");
    System.out.println("3. Search media");
    System.out.println("0. Exit");
    System.out.print("> ");
  }

  private void showMemberMenu() {
    User currentUser = authService.getCurrentUser().orElseThrow();
    System.out.printf("\nWelcome %s (Member)\n", currentUser.getName());
    System.out.println("1. Borrow media");
    System.out.println("2. Return media");
    System.out.println("3. Pay fine");
    System.out.println("4. Search media");
    System.out.println("0. Logout");
    System.out.print("> ");
  }

  private void showAdminMenu() {
    User currentUser = authService.getCurrentUser().orElseThrow();
    System.out.printf("\nWelcome %s (Admin)\n", currentUser.getName());
    System.out.println("1. Add book");
    System.out.println("2. Add CD");
    System.out.println("3. Send reminders");
    System.out.println("4. Show overdue report");
    System.out.println("5. List all users");
    System.out.println("6. Unregister user");
    System.out.println("0. Logout");
    System.out.print("> ");
  }

  private boolean handleChoice(String choice) {
    Optional<User> currentUser = authService.getCurrentUser();
    
    if (currentUser.isEmpty()) {
      return handleInitialMenuChoice(choice);
    } else if (currentUser.get().isAdmin()) {
      return handleAdminMenuChoice(choice);
    } else {
      return handleMemberMenuChoice(choice);
    }
  }

  private boolean handleInitialMenuChoice(String choice) {
    return switch (choice) {
      case "1" -> { login(); yield true; }
      case "2" -> { registerMember(); yield true; }
      case "3" -> { search(); yield true; }
      case "0" -> { System.out.println("Goodbye!"); yield false; }
      default -> { System.out.println("Invalid option, please try again"); yield true; }
    };
  }

  private boolean handleMemberMenuChoice(String choice) {
    return switch (choice) {
      case "1" -> { borrow(); yield true; }
      case "2" -> { returnMedia(); yield true; }
      case "3" -> { payFine(); yield true; }
      case "4" -> { search(); yield true; }
      case "0" -> { 
        authService.logout();
        System.out.println("Successfully logged out");
        yield true; 
      }
      default -> { System.out.println("خيار غير صالح، الرجاء المحاولة مرة أخرى"); yield true; }
    };
  }

  private boolean handleAdminMenuChoice(String choice) {
    return switch (choice) {
      case "1" -> { addBook(); yield true; }
      case "2" -> { addCd(); yield true; }
      case "3" -> { sendReminders(); yield true; }
      case "4" -> { showOverdueReport(); yield true; }
      case "5" -> { listAllUsers(); yield true; }
      case "6" -> { unregister(); yield true; }
      case "0" -> { 
        authService.logout();
        System.out.println("Successfully logged out");
        yield true; 
      }
      default -> { System.out.println("خيار غير صالح، الرجاء المحاولة مرة أخرى"); yield true; }
    };
  }

  private void login() {
    System.out.print("Username: ");
    String username = scanner.nextLine().trim();
    System.out.print("Password: ");
    String password = scanner.nextLine().trim();
    User user = authService.login(username, password);
    System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");
  }

  private void registerMember() {
    System.out.print("Username: ");
    String username = scanner.nextLine().trim();
    System.out.print("Full name: ");
    String name = scanner.nextLine().trim();
    System.out.print("Password: ");
    String password = scanner.nextLine().trim();
    User member = userService.registerMember(username, name, password);
    System.out.println("Member created with id " + member.getId());
  }

  private void addBook() {
    authService.requireAdmin();
    System.out.print("Title: ");
    String title = scanner.nextLine();
    System.out.print("Author: ");
    String author = scanner.nextLine();
    System.out.print("ISBN: ");
    String isbn = scanner.nextLine();
    Media book = catalogService.addBook(title, author, isbn);
    System.out.println("Book added with id " + book.getId());
  }

  private void addCd() {
    authService.requireAdmin();
    System.out.print("Title: ");
    String title = scanner.nextLine();
    System.out.print("Artist: ");
    String artist = scanner.nextLine();
    Media cd = catalogService.addCd(title, artist);
    System.out.println("CD added with id " + cd.getId());
  }

  private void search() {
    System.out.print("Search term (blank for all): ");
    String term = scanner.nextLine();
    List<Media> results = catalogService.search(term);
    if (results.isEmpty()) {
      System.out.println("No results.");
      return;
    }
    results.forEach(
        media ->
            System.out.printf(
                "%s - %s (%s) available=%s%n",
                media.getId(), media.getTitle(), media.getType(), media.isAvailable()));
  }

  private void borrow() {
    Optional<User> current = authService.getCurrentUser();
    if (current.isEmpty()) {
      throw new LibraryException("Login required");
    }
    if (current.get().isAdmin()) {
      throw new LibraryException("Members only");
    }
    System.out.print("Media id: ");
    String mediaId = scanner.nextLine().trim();
    var loan = borrowService.borrow(current.get().getId(), mediaId);
    System.out.println(
        "Loan created. Due on "
            + loan.getDueDate()
            + ". Loan id: "
            + loan.getId());
  }

  private void returnMedia() {
    Optional<User> current = authService.getCurrentUser();
    if (current.isEmpty()) {
      throw new LibraryException("Login required");
    }
    System.out.print("Loan id: ");
    String loanId = scanner.nextLine().trim();
    BigDecimal fine = borrowService.returnMedia(loanId);
    if (fine.signum() > 0) {
      System.out.println("Returned with fine: " + fine);
    } else {
      System.out.println("Returned successfully.");
    }
  }

  private void payFine() {
    Optional<User> current = authService.getCurrentUser();
    if (current.isEmpty()) {
      throw new LibraryException("Login required");
    }
    User user = current.get();
    if (!user.hasOutstandingFines()) {
      System.out.println("You have no outstanding fines.");
      return;
    }
    System.out.println("Your current outstanding fine: " + user.getFineBalance());
    System.out.print("Payment amount: ");
    BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
    BigDecimal remaining = fineService.payFine(user.getId(), amount);
    System.out.println("Remaining balance: " + remaining);
  }

  private void sendReminders() {
    authService.requireAdmin();
    var notified = reminderService.sendDailyReminders();
    System.out.println("Reminders sent to " + notified.size() + " user(s).");
    environment
        .getEmailNotifier()
        .getSentMessages()
        .forEach(msg -> System.out.println("Email -> " + msg));
    environment.getEmailNotifier().clear();
  }

  private void unregister() {
    authService.requireAdmin();
    System.out.print("User id to remove: ");
    String userId = scanner.nextLine().trim();
    userService.unregister(userId);
    System.out.println("User removed.");
  }

  private void showOverdueReport() {
    Optional<User> current = authService.getCurrentUser();
    if (current.isEmpty()) {
      throw new LibraryException("Login required");
    }
    OverdueReport report = fineService.generateOverdueReport(current.get().getId());
    if (report.getItems().isEmpty()) {
      System.out.println("No overdue items.");
      return;
    }
    report
        .getItems()
        .forEach(
            item ->
                System.out.printf(
                    "%s (%s) %d days overdue -> fine %s%n",
                    item.mediaTitle(),
                    item.mediaType(),
                    item.overdueDays(),
                    item.fineAmount()));
    System.out.println("Total fine: " + report.getTotalFine());
  }

  private void listAllUsers() {
    authService.requireAdmin();
    java.util.Collection<User> users = userService.listAllUsers();
    if (users.isEmpty()) {
      System.out.println("No users found.");
      return;
    }
    System.out.println("\nAll Users:");
    System.out.println("ID\t\tUsername\tName\t\tRole\t\tFine Balance");
    System.out.println("------------------------------------------------------------");
    for (User user : users) {
      System.out.printf(
          "%-12s\t%-12s\t%-12s\t%-8s\t%s%n",
          user.getId(),
          user.getUsername(),
          user.getName(),
          user.getRole(),
          user.getFineBalance());
    }
    System.out.println("\nTotal users: " + users.size());
  }
}


