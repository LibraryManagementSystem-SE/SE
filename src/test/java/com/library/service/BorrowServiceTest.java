package com.library.service;

import com.library.common.LibraryException;
import com.library.domain.*;
import com.library.repository.*;
import com.library.support.DateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowServiceTest {

    private LoanRepository loanRepository;
    private MediaRepository mediaRepository;
    private UserRepository userRepository;
    private DateProvider dateProvider;
    private FineStrategyFactory fineFactory;

    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        loanRepository = mock(LoanRepository.class);
        mediaRepository = mock(MediaRepository.class);
        userRepository = mock(UserRepository.class);
        dateProvider = mock(DateProvider.class);
        fineFactory = new FineStrategyFactory();

        borrowService = new BorrowService(
                loanRepository,
                mediaRepository,
                userRepository,
                dateProvider,
                fineFactory
        );
    }

    /* =========================
       borrow()
       ========================= */

    @Test
    void borrowSuccess() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        Media book = new Book("b1","Clean Code","Martin","123");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.of(book));
        when(loanRepository.findActiveByMedia("b1")).thenReturn(Optional.empty());
        when(dateProvider.today()).thenReturn(LocalDate.of(2025,1,1));

        Loan loan = borrowService.borrow("u1","b1");

        assertNotNull(loan);
        verify(loanRepository).save(any());
    }

    @Test
    void borrowFailsWhenUserNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(LibraryException.class,
                () -> borrowService.borrow("u1","b1"));
    }

    @Test
    void borrowFailsWhenMediaNotFound() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.empty());

        assertThrows(LibraryException.class,
                () -> borrowService.borrow("u1","b1"));
    }

    @Test
    void borrowFailsWhenAlreadyBorrowed() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        Media book = new Book("b1","Clean Code","Martin","123");
        Loan activeLoan = mock(Loan.class);

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.of(book));
        when(loanRepository.findActiveByMedia("b1"))
                .thenReturn(Optional.of(activeLoan));

        assertThrows(LibraryException.class,
                () -> borrowService.borrow("u1","b1"));
    }

    @Test
    void borrowFailsWhenUserHasOutstandingFines() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        user.addFine(BigDecimal.TEN);

        Media book = new Book("b1","Clean Code","Martin","123");

        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.of(book));

        assertThrows(LibraryException.class,
                () -> borrowService.borrow("u1","b1"));
    }

    /* =========================
       returnMedia()
       ========================= */

    @Test
    void returnMediaNoFineWhenReturnedOnTime() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        Media book = new Book("b1","Clean Code","Martin","123");

        Loan loan = new Loan(
                "l1","u1","b1",
                LocalDate.of(2025,1,1),
                LocalDate.of(2025,1,10)
        );

        when(loanRepository.findById("l1")).thenReturn(Optional.of(loan));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.of(book));
        when(dateProvider.today()).thenReturn(LocalDate.of(2025,1,10));

        BigDecimal fine = borrowService.returnMedia("l1");

        assertEquals(BigDecimal.ZERO, fine);
    }

    @Test
    void returnMediaAddsFineWhenOverdue() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        Media book = new Book("b1","Clean Code","Martin","123");

        Loan loan = new Loan(
                "l1","u1","b1",
                LocalDate.of(2025,1,1),
                LocalDate.of(2025,1,5)
        );

        when(loanRepository.findById("l1")).thenReturn(Optional.of(loan));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.of(book));
        when(dateProvider.today()).thenReturn(LocalDate.of(2025,1,7)); // 2 days late

        BigDecimal fine = borrowService.returnMedia("l1");

        assertTrue(fine.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void returnMediaFailsWhenLoanNotFound() {
        when(loanRepository.findById("l1")).thenReturn(Optional.empty());

        assertThrows(LibraryException.class,
                () -> borrowService.returnMedia("l1"));
    }

    @Test
    void returnMediaFailsWhenUserNotFound() {
        Loan loan = new Loan(
                "l1","u1","b1",
                LocalDate.now().minusDays(5),
                LocalDate.now()
        );

        when(loanRepository.findById("l1")).thenReturn(Optional.of(loan));
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(LibraryException.class,
                () -> borrowService.returnMedia("l1"));
    }

    @Test
    void returnMediaFailsWhenMediaNotFound() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        Loan loan = new Loan(
                "l1","u1","b1",
                LocalDate.now().minusDays(5),
                LocalDate.now()
        );

        when(loanRepository.findById("l1")).thenReturn(Optional.of(loan));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.empty());

        assertThrows(LibraryException.class,
                () -> borrowService.returnMedia("l1"));
    }

    @Test
    void returningSameLoanTwiceReturnsZeroFine() {
        User user = new User("u1","tala","Tala",UserRole.MEMBER,"pw");
        Media book = new Book("b1","Clean Code","Martin","123");

        Loan loan = new Loan(
                "l1","u1","b1",
                LocalDate.of(2025,1,1),
                LocalDate.of(2025,1,5)
        );

        when(loanRepository.findById("l1")).thenReturn(Optional.of(loan));
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(mediaRepository.findById("b1")).thenReturn(Optional.of(book));
        when(dateProvider.today()).thenReturn(LocalDate.of(2025,1,7));

        borrowService.returnMedia("l1");
        BigDecimal second = borrowService.returnMedia("l1");

        assertEquals(BigDecimal.ZERO, second);
    }
}
