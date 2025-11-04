package ru.tasks.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tasks.session.Session;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {

    @TempDir
    Path tempDir;

    private static void setSessionUser(final Session s, final UUID id) throws Exception {
        final Field f = Session.class.getDeclaredField("user");
        f.setAccessible(true);
        f.set(s, id);
    }

    @Test
    void requireUserOk() throws Exception {
        final Session session = new Session(tempDir);
        setSessionUser(session, UUID.randomUUID());
        assertDoesNotThrow(() -> Utils.requireUser(session));
    }

    @Test
    void requireUserThrows() {
        final Session session = new Session(tempDir);
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> Utils.requireUser(session));
        assertTrue(ex.getMessage().toLowerCase(Locale.ROOT).contains("no user uuid"));
    }

    @Test
    void checkArgsThrows() {
        final List<String> parts = List.of("a");
        assertThrows(IllegalArgumentException.class, () -> Utils.checkArgs(parts, 2));
    }

    @Test
    void checkArgsOkOn() {
        final List<String> parts = List.of("a", "b");
        assertDoesNotThrow(() -> Utils.checkArgs(parts, 2));
    }

    @Test
    void tokenize() {
        final String line = "shorten \"https://example.com/a b\" 5   end";
        final List<String> t = Utils.tokenize(line);
        assertEquals(List.of("shorten", "https://example.com/a b", "5", "end"), t);
    }

    @Test
    void codeFromInput() {
        assertEquals("code", Utils.codeFromInput("clck.ru/code"));
        assertEquals("code", Utils.codeFromInput("/code"));
        assertEquals("code", Utils.codeFromInput("code"));
        assertNull(Utils.codeFromInput(null));
    }

    @Test
    void fmt() {
        final Instant instant = Instant.ofEpochSecond(1_700_000_000L);
        final String expected = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                                 .withZone(ZoneId.systemDefault())
                                                 .format(instant);
        assertEquals(expected, Utils.fmt(instant));
    }

    @Test
    void truncateAddsPoints() {
        assertEquals("abc", Utils.truncate("abc", 3));
        assertEquals("ab…", Utils.truncate("abcde", 3));
    }

    @Test
    void shortUuid() {
        final UUID id = UUID.randomUUID();
        final String head = id.toString().substring(0, 8);
        assertEquals(head, Utils.shortUuid(id));
        assertEquals(8, Utils.shortUuid(id).length());
    }

    @Test
    void parsePositiveLong() {
        assertEquals(5L, Utils.parsePositiveLong("5"));
        assertEquals(5L, Utils.parsePositiveLong(" 5 "));
        assertNull(Utils.parsePositiveLong("0"));
        assertNull(Utils.parsePositiveLong("-1"));
        assertNull(Utils.parsePositiveLong("abc"));
    }

    @Test
    void printHelpWritesCommands() {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final PrintStream prev = System.out;
        System.setOut(new PrintStream(bout));
        try {
            Utils.printHelp();
        } finally {
            System.setOut(prev);
        }
        final String s = bout.toString();
        assertTrue(s.contains("help"));
        assertTrue(s.contains("shorten"));
        assertTrue(s.contains("open"));
        assertTrue(s.contains("inbox"));
    }

    @Test
    void openInBrowserDoesNotThrow() {
        assertDoesNotThrow(() -> Utils.openInBrowser("https://example.com"));
    }
}
