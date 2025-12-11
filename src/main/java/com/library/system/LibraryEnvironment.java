package com.library.system;

import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.FineStrategyFactory;
import com.library.notification.EmailNotifier;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.repository.UserRepository;
import com.library.repository.file.FileMediaRepository;
import com.library.repository.file.FileUserRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.service.AuthService;
import com.library.service.BorrowService;
import com.library.service.CatalogService;
import com.library.service.FineService;
import com.library.service.ReminderService;
import com.library.service.UserService;
import com.library.support.DateProvider;
import com.library.repository.LoanRepository;
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
  private final LoanRepository loanRepository;
  private final MediaRepository mediaRepository;
  private final DateProvider dateProvider;

  private LibraryEnvironment(
		    AuthService authService,
		    CatalogService catalogService,
		    BorrowService borrowService,
		    FineService fineService,
		    ReminderService reminderService,
		    UserService userService,
		    EmailNotifier emailNotifier,
		    LoanRepository loanRepository,
	  		MediaRepository mediaRepository,
	  		DateProvider dateProvider) {
		  this.authService = authService;
		  this.catalogService = catalogService;
		  this.borrowService = borrowService;
		  this.fineService = fineService;
		  this.reminderService = reminderService;
		  this.userService = userService;
		  this.emailNotifier = emailNotifier;
		  this.loanRepository = loanRepository; 
		  this.mediaRepository = mediaRepository;  
		  this.dateProvider = dateProvider;  

		}

  public static LibraryEnvironment bootstrap() {
    // Use file-based repositories for users and media so data is kept between runs.
    UserRepository userRepository = new FileUserRepository();
    MediaRepository mediaRepository = new FileMediaRepository();
    LoanRepository loanRepository = new InMemoryLoanRepository();
    DateProvider dateProvider = new DateProvider.System();
    FineStrategyFactory fineStrategyFactory = new FineStrategyFactory();

    // Seed some demo books and CDs into the media repository if it's empty so that
    // the files are populated and the catalog is not empty on first run.
    if (mediaRepository.findAll().isEmpty()) {
      mediaRepository.save(
          new Book("B1", "Clean Code", "Robert C. Martin", "9780132350884"));
      mediaRepository.save(
          new Book("B2", "Effective Java", "Joshua Bloch", "9780134685991"));
      mediaRepository.save(new CD("C1", "Thriller", "Michael Jackson"));
      mediaRepository.save(new CD("C2", "Back in Black", "AC/DC"));
    }

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

    // Seed default admin accounts
    if (userRepository.findByUsername("sally").isEmpty()) {
      userService.registerAdmin("sally", "Sally", "admin123");
    }
    if (userRepository.findByUsername("tala").isEmpty()) {
      userService.registerAdmin("tala", "Tala", "tala123");
    }

    return new LibraryEnvironment(
    	    authService,
    	    catalogService,
    	    borrowService,
    	    fineService,
    	    reminderService,
    	    userService,
    	    emailNotifier,
    	    loanRepository,
    	    mediaRepository,
    	    dateProvider);
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

  /**
   * Returns the loan repository instance, initializing it if necessary.
   * @return the loan repository instance
   */
  public LoanRepository getLoanRepository() {
	    return loanRepository;
	}

  public MediaRepository getMediaRepository() {
	    return mediaRepository;
	}
  public UserRepository getUserRepository() {
	    return userService.getUserRepository();
	}


  public DateProvider getDateProvider() {
	    return dateProvider;
	}
}


