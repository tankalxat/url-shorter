package ru.tasks.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tasks.session.Link;
import ru.tasks.session.Notification;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {

    @TempDir
    Path tempDir;

    Path dataFile;

    @BeforeEach
    void setup() {
        dataFile = tempDir.resolve("data.bin");
    }

    private static Link link(final String code, final UUID owner, final Instant created, final Instant expires,
                             final int maxClicks, final String url) {
        return new Link(code, url, owner, created, expires, maxClicks);
    }

    @Test
    void addGetExists() {
        final Repository repo = new Repository(dataFile);
        final UUID u = UUID.randomUUID();
        final Link l = link("A1", u, Instant.now(), Instant.now().plusSeconds(3600), 0, "https://example.com");
        repo.add(l);
        assertTrue(repo.existsCode("A1"));
        assertEquals(l, repo.get("A1"));
    }

    @Test
    void listByUser() {
        final Repository repo = new Repository(dataFile);
        final UUID u = UUID.randomUUID();
        final Instant t1 = Instant.now();
        final Instant t2 = t1.plusSeconds(1);
        final Link l1 = link("C1", u, t1, t1.plusSeconds(10), 0, "https://c1");
        final Link l2 = link("C2", u, t2, t2.plusSeconds(10), 0, "https://c2");
        repo.add(l2);
        repo.add(l1);
        final List<Link> list = repo.listByUser(u);
        assertEquals(2, list.size());
        assertEquals("C1", list.get(0).code());
        assertEquals("C2", list.get(1).code());
    }

    @Test
    void deleteByOwner() {
        final Repository repo = new Repository(dataFile);
        final UUID owner = UUID.randomUUID();
        final UUID other = UUID.randomUUID();
        final Link l = link("D1", owner, Instant.now(), Instant.now().plusSeconds(10), 0, "https://d1");
        repo.add(l);
        assertFalse(repo.delete(other, "D1"));
        assertTrue(repo.delete(owner, "D1"));
        assertFalse(repo.existsCode("D1"));
    }

    @Test
    void updateLimitByOwner() {
        final Repository repo = new Repository(dataFile);
        final UUID owner = UUID.randomUUID();
        final UUID other = UUID.randomUUID();
        final Link l = link("LIM1", owner, Instant.now(), Instant.now().plusSeconds(100), 1, "https://lim");
        repo.add(l);
        assertFalse(repo.updateLimit(other, "LIM1", 5));
        assertTrue(repo.updateLimit(owner, "LIM1", 5));
        assertEquals(5, repo.get("LIM1").maxClicks());
    }

    @Test
    void notificationsReset() {
        final Repository repo = new Repository(dataFile);
        final UUID u = UUID.randomUUID();
        repo.notify(u, "m1");
        repo.notify(u, "m2");
        final List<Notification> got = repo.resetNotifications(u);
        assertEquals(2, got.size());
        assertEquals("m1", got.get(0).message());
        assertEquals("m2", got.get(1).message());
        assertTrue(repo.resetNotifications(u).isEmpty());
    }

    @Test
    void deleteExpired() {
        final Repository repo = new Repository(dataFile);
        final UUID u = UUID.randomUUID();
        final Instant now = Instant.now();
        final Link fresh = link("FRESH", u, now, now.plusSeconds(1000), 0, "https://fresh");
        final Link expired = link("OLD", u, now.minusSeconds(1000), now.minusSeconds(1), 0, "https://old");
        repo.add(fresh);
        repo.add(expired);
        final int removed = repo.deleteExpiredLinks();
        assertEquals(1, removed);
        assertNull(repo.get("OLD"));
        assertNotNull(repo.get("FRESH"));
        final List<Notification> notes = repo.resetNotifications(u);
        assertEquals(1, notes.size());
        assertTrue(notes.getFirst().message().contains("Link 'OLD'"));
    }

    @Test
    void persists() {
        final Repository repo1 = new Repository(dataFile);
        final UUID u = UUID.randomUUID();
        final Link l = link("P1", u, Instant.now(), Instant.now().plusSeconds(3600), 0, "https://p1");
        repo1.add(l);
        final Repository repo2 = new Repository(dataFile);
        assertNotNull(repo2.get("P1"));
        assertEquals(u, repo2.get("P1").ownerId());
    }
}
