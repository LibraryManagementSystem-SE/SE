package com.library.notification;

import static org.junit.jupiter.api.Assertions.*;

import com.library.domain.User;
import com.library.domain.UserRole;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmailNotifierTest {

    private EmailNotifier notifier;
    private User user;

    @BeforeEach
    void setUp() {
        notifier = new EmailNotifier();
        user = new User("1", "tala", "Tala", UserRole.MEMBER, "pw");
    }

    @Test
    void notifyStoresFormattedMessage() {
        notifier.notify(user, "Hello!");

        List<String> messages = notifier.getSentMessages();

        assertEquals(1, messages.size());
        assertEquals("To tala: Hello!", messages.get(0));
    }

    @Test
    void notifyStoresMultipleMessages() {
        notifier.notify(user, "Msg1");
        notifier.notify(user, "Msg2");

        List<String> messages = notifier.getSentMessages();

        assertEquals(2, messages.size());
        assertEquals("To tala: Msg1", messages.get(0));
        assertEquals("To tala: Msg2", messages.get(1));
    }

    @Test
    void notifyWorksWithDifferentUsernames() {
        User u2 = new User("2", "sally", "Sally", UserRole.MEMBER, "pw");

        notifier.notify(user, "Hi");
        notifier.notify(u2, "Hello");

        List<String> messages = notifier.getSentMessages();

        assertEquals("To tala: Hi", messages.get(0));
        assertEquals("To sally: Hello", messages.get(1));
    }

    @Test
    void clearRemovesAllMessages() {
        notifier.notify(user, "Hello");
        notifier.notify(user, "Again");

        notifier.clear();

        assertTrue(notifier.getSentMessages().isEmpty());
    }

    @Test
    void getSentMessagesIsUnmodifiable() {
        notifier.notify(user, "Test");

        List<String> stored = notifier.getSentMessages();

        assertThrows(UnsupportedOperationException.class, () -> {
            stored.add("Should fail");
        });
    }

    @Test
    void notifyAcceptsEmptyMessage() {
        notifier.notify(user, "");

        assertEquals("To tala: ", notifier.getSentMessages().get(0));
    }
}
