package com.library.system;

import com.library.domain.FineStrategyFactory;
import com.library.notification.EmailNotifier;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.repository.UserRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.repository.memory.InMemoryMediaRepository;
import com.library.repository.memory.InMemoryUserRepository;
import com.library.service.AuthService;
import com.library.service.BorrowService;
import com.library.service.CatalogService;
import com.library.service.FineService;
import com.library.service.ReminderService;
import com.library.service.UserService;
import com.library.support.DateProvider;

/**
 * Wiring helper for the layered architecture.
 */
public class LibraryEnvironment {
  private final AuthService authService;
  private final CatalogService catalogService;
  private final BorrowService borrowService;
  private final FineService fineService;
  private final ReminderService reminderService;
  private final UserService userService;
  private final EmailNotifier emailNotifier;

  private LibraryEnvironment(
      AuthService authService,
      CatalogService catalogService,
      BorrowService borrowService,
      FineService fineService,
      ReminderService reminderService,
      UserService userService,
      EmailNotifier emailNotifier) {
    this.authService = authService;
    this.catalogService = catalogService;
    this.borrowService = borrowService;
    this.fineService = fineService;
    this.reminderService = reminderService;
    this.userService = userService;
    this.emailNotifier = emailNotifier;
  }

  public static LibraryEnvironment bootstrap() {
    UserRepository userRepository = new InMemoryUserRepository();
    MediaRepository mediaRepository = new InMemoryMediaRepository();
    LoanRepository loanRepository = new InMemoryLoanRepository();
    DateProvider dateProvider = new DateProvider.System();
    FineStrategyFactory fineStrategyFactory = new FineStrategyFactory();

    AuthService authService = new AuthService(userRepository);
    BorrowService borrowService =
        new BorrowService(
            loanRepository, mediaRepository, userRepository, dateProvider, fineStrategyFactory);
    FineService fineService =
        new FineService(
            userRepository, loanRepository, mediaRepository, dateProvider, fineStrategyFactory);
    ReminderService reminderService = new ReminderService(loanRepository, userRepository, dateProvider);
    EmailNotifier emailNotifier = new EmailNotifier();
    reminderService.register(emailNotifier);
    CatalogService catalogService = new CatalogService(mediaRepository, authService);
    UserService userService = new UserService(userRepository, loanRepository, authService);

    // Seed default admin account
    if (userRepository.findByUsername("admin").isEmpty()) {
      userService.registerAdmin("admin", "System Admin", "admin123");
    }

    return new LibraryEnvironment(
        authService,
        catalogService,
        borrowService,
        fineService,
        reminderService,
        userService,
        emailNotifier);
  }

  public AuthService getAuthService() {
    return authService;
  }

  public CatalogService getCatalogService() {
    return catalogService;
  }

  public BorrowService getBorrowService() {
    return borrowService;
  }

  public FineService getFineService() {
    return fineService;
  }

  public ReminderService getReminderService() {
    return reminderService;
  }

  public UserService getUserService() {
    return userService;
  }

  public EmailNotifier getEmailNotifier() {
    return emailNotifier;
  }
}


