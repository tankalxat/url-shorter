package ru.tasks.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tasks.dao.Repository;
import ru.tasks.session.Link;
import ru.tasks.session.Notification;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URLServiceTest {

    @TempDir
    Path tempDir;

    private Repository repo;
    private URLService service;

    @BeforeEach
    void setUp() {
        repo = new Repository(tempDir.resolve("data.bin"));
        service = new URLService(repo);
        System.clearProperty("ttlMinutes");
        System.clearProperty("cleanupSeconds");
    }

    @Test
    void createShortLink_Normalize() {
        final Link l = service.createShortLink(null, "example.com", 0);
        assertNotNull(l.ownerId());
        assertTrue(l.originalUrl().startsWith("https://"));
        assertTrue(l.expiresAt().isAfter(l.createdAt()));
    }

    @Test
    void createShortLink_SetMaxClicks() {
        final Link l = service.createShortLink(UUID.randomUUID(), "https://example.com", 2);
        assertEquals(2, l.maxClicks());
    }

    @Test
    void createShortLink_GeneratesUniqueCode() {
        final Link a = service.createShortLink(UUID.randomUUID(), "https://a", 0);
        final Link b = service.createShortLink(UUID.randomUUID(), "https://b", 0);
        assertNotEquals(a.code(), b.code());
    }

    @Test
    void open_NotFound() throws Exception {
        final Object res = service.open("NOPE");
        assertEquals("NOT_FOUND", statusOf(res));
    }

    @Test
    void open_Expired() throws Exception {
        final UUID u = UUID.randomUUID();
        final Instant now = Instant.now();
        final Link l = new Link("OLD", "https://old", u, now.minusSeconds(5), now.minusSeconds(1), 0);
        repo.add(l);
        final Object res = service.open("OLD");
        assertEquals("EXPIRED", statusOf(res));
        assertNull(repo.get("OLD"));
        final List<Notification> notes = repo.resetNotifications(u);
        assertEquals(1, notes.size());
        assertTrue(notes.getFirst().message().contains("OLD"));
    }

    @Test
    void open_LimitReached() throws Exception {
        final UUID u = UUID.randomUUID();
        final Instant now = Instant.now();
        final Link l = new Link("LIM", "https://lim", u, now, now.plusSeconds(1000), 1);
        repo.add(l);

        final Object r1 = service.open("LIM");
        assertEquals("OPENED", statusOf(r1));
        assertEquals(1, repo.get("LIM").clicksMade());

        final Object r2 = service.open("LIM");
        assertEquals("LIMIT_REACHED", statusOf(r2));
        List<Notification> notes = repo.resetNotifications(u);
        assertFalse(notes.isEmpty());
        assertTrue(notes.getFirst().message().toLowerCase(Locale.ROOT).contains("limit"));
    }

    @Test
    void open() throws Exception {
        final UUID u = UUID.randomUUID();
        final Instant now = Instant.now();
        final Link l = new Link("OK", "https://ok", u, now, now.plusSeconds(1000), 2);
        repo.add(l);
        final Object r1 = service.open("OK");
        assertEquals("OPENED", statusOf(r1));
        assertTrue(repo.resetNotifications(u).isEmpty());
    }

    @Test
    void listUpdateDelete() {
        final UUID u = UUID.randomUUID();
        final Link l = service.createShortLink(u, "https://x", 3);
        assertEquals(1, service.listByUser(u).size());
        assertTrue(service.updateLimit(u, l.code(), 10));
        assertEquals(10, repo.get(l.code()).maxClicks());
        assertTrue(service.delete(u, l.code()));
        assertTrue(service.listByUser(u).isEmpty());
    }

    @Test
    void deleteExpiredLinks() {
        final UUID u = UUID.randomUUID();
        final Instant now = Instant.now();
        repo.add(new Link("A", "https://a", u, now.minusSeconds(10), now.minusSeconds(1), 0));
        repo.add(new Link("B", "https://b", u, now, now.plusSeconds(1000), 0));
        final int removed = service.deleteExpiredLinks();
        assertEquals(1, removed);
        assertNull(repo.get("A"));
        assertNotNull(repo.get("B"));
    }

    private static String statusOf(final Object res) throws Exception {
        if (res == null) {
            return null;
        }

        try {
            return String.valueOf(invokeMethod(res, "status"));
        } catch (final NoSuchMethodException ignore) {
        }

        try {
            return String.valueOf(invokeMethod(res, "getStatus"));
        } catch (final NoSuchMethodException ignore) {
        }

        return res.toString();
    }

    private static Object invokeMethod(final Object target, final String method) throws Exception {
        final Method m = target.getClass().getMethod(method);
        m.setAccessible(true);
        return m.invoke(target);
    }
}
