package com.library.service;

import static org.junit.jupiter.api.Assertions.*;

import com.library.common.LibraryException;
import com.library.domain.*;
import com.library.repository.*;
import com.library.repository.memory.*;
import com.library.support.FakeDateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

class FineServiceTest {

    private User user;
    private FineService fineService;
    private LoanRepository loanRepository;
    private MediaRepository mediaRepository;
    private UserRepository userRepository;
    private FakeDateProvider dateProvider;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        mediaRepository = new InMemoryMediaRepository();
        loanRepository = new InMemoryLoanRepository();
        dateProvider = new FakeDateProvider(LocalDate.of(2025, 2, 1));

        fineService = new FineService(
                userRepository,
                loanRepository,
                mediaRepository,
                dateProvider,
                new FineStrategyFactory()
        );

        user = new User("u1", "bob", "Bob", UserRole.MEMBER, "pw");
        userRepository.save(user);

        Book book = new Book("b1", "Clean Code", "Martin", "111");
        CD cd = new CD("c1", "Blue Train", "Coltrane");
        mediaRepository.save(book);
        mediaRepository.save(cd);

        loanRepository.save(new Loan(
                "l1",
                user.getId(),
                book.getId(),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 20)
        ));

        loanRepository.save(new Loan(
                "l2",
                user.getId(),
                cd.getId(),
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 25)
        ));
    }

    /* =========================
       generateOverdueReport()
       ========================= */

    @Test
    void overdueReportAggregatesAllOverdueItems() {
        OverdueReport report = fineService.generateOverdueReport(user.getId());

        assertEquals(2, report.getItems().size());
        assertTrue(report.getTotalFine().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void overdueReportReturnsEmptyWhenNoLoans() {
        loanRepository.findAll().forEach(loan -> loanRepository.delete(loan.getId()));

        OverdueReport report = fineService.generateOverdueReport(user.getId());

        assertTrue(report.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, report.getTotalFine());
    }

    @Test
    void overdueReportReturnsEmptyWhenAllLoansReturned() {
        loanRepository.findAll()
                .forEach(loan -> loan.markReturned(dateProvider.today()));

        OverdueReport report = fineService.generateOverdueReport(user.getId());

        assertTrue(report.getItems().isEmpty());
    }

    @Test
    void overdueReportThrowsWhenUserMissing() {
        assertThrows(
                LibraryException.class,
                () -> fineService.generateOverdueReport("missing")
        );
    }

    @Test
    void overdueReportThrowsWhenMediaMissing() {
        ((InMemoryMediaRepository) mediaRepository).delete("b1");

        assertThrows(
                LibraryException.class,
                () -> fineService.generateOverdueReport(user.getId())
        );
    }

    /* =========================
       payFine()
       ========================= */

    @Test
    void payFineSupportsPartialPayment() {
        user.addFine(BigDecimal.valueOf(50));

        BigDecimal remaining = fineService.payFine(user.getId(), BigDecimal.valueOf(20));

        assertEquals(BigDecimal.valueOf(30), remaining);
    }

    @Test
    void payFineAllowsExactPayment() {
        user.addFine(BigDecimal.valueOf(25));

        BigDecimal remaining = fineService.payFine(user.getId(), BigDecimal.valueOf(25));

        assertEquals(BigDecimal.ZERO, remaining);
    }

    @Test
    void payFineThrowsWhenUserHasNoFine() {
        assertThrows(
                LibraryException.class,
                () -> fineService.payFine(user.getId(), BigDecimal.TEN)
        );
    }

    @Test
    void payFineThrowsWhenPaymentExceedsFine() {
        user.addFine(BigDecimal.valueOf(20));

        assertThrows(
                LibraryException.class,
                () -> fineService.payFine(user.getId(), BigDecimal.valueOf(30))
        );
    }

    @Test
    void payFineThrowsWhenAmountZeroOrNegative() {
        user.addFine(BigDecimal.TEN);

        assertThrows(
                LibraryException.class,
                () -> fineService.payFine(user.getId(), BigDecimal.ZERO)
        );

        assertThrows(
                LibraryException.class,
                () -> fineService.payFine(user.getId(), BigDecimal.valueOf(-1))
        );
    }

    @Test
    void payFineThrowsWhenUserNotFound() {
        assertThrows(
                LibraryException.class,
                () -> fineService.payFine("missing", BigDecimal.TEN)
        );
    }
}
