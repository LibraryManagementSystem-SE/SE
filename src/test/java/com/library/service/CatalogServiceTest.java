package com.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.library.common.AuthService;
import com.library.common.LibraryException;
import com.library.domain.Book;
import com.library.domain.Media;
import com.library.domain.MediaType;
import com.library.repository.MediaRepository;
import com.library.repository.memory.InMemoryMediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class CatalogServiceTest {

    private CatalogService catalogService;
    private MediaRepository mediaRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mediaRepository = new InMemoryMediaRepository();
        authService = mock(AuthService.class);

        catalogService = new CatalogService(mediaRepository, authService);
    }

    /* =========================
       addBook
       ========================= */

    @Test
    void addBookSucceedsForAdmin() {
        Book book = catalogService.addBook(
                "Clean Code",
                "Robert Martin",
                "111"
        );

        assertNotNull(book);
        assertEquals("Clean Code", book.getTitle());
        assertEquals(1, mediaRepository.findAll().size());
    }

    @Test
    void addBookFailsWhenNotAdmin() {
        doThrow(new LibraryException("Admin required"))
                .when(authService).requireAdmin();

        assertThrows(LibraryException.class,
                () -> catalogService.addBook("X", "Y", "Z"));
    }

    /* =========================
       addCd
       ========================= */

    @Test
    void addCdSucceedsForAdmin() {
        Media cd = catalogService.addCd("Thriller", "Michael Jackson");

        assertNotNull(cd);
        assertEquals(MediaType.CD, cd.getType());
    }

    /* =========================
       search
       ========================= */

    @Test
    void searchReturnsMatchingResults() {
        catalogService.addBook("Clean Code", "Robert Martin", "111");
        catalogService.addBook("Effective Java", "Joshua Bloch", "222");

        List<Media> result = catalogService.search("Clean");

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
    }

    @Test
    void searchReturnsEmptyListWhenNoMatch() {
        List<Media> result = catalogService.search("Nothing");
        assertTrue(result.isEmpty());
    }

    /* =========================
       listByType
       ========================= */

    @Test
    void listByTypeReturnsOnlyBooks() {
        catalogService.addBook("Clean Code", "Robert Martin", "111");
        catalogService.addCd("Thriller", "MJ");

        List<Media> books = catalogService.listByType(MediaType.BOOK);

        assertEquals(1, books.size());
        assertEquals(MediaType.BOOK, books.get(0).getType());
    }
}
