package ru.tasks.cmd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    private PrintStream prevOut;
    private PrintStream prevErr;
    private InputStream prevIn;
    private String prevHome;
    private String prevCleanup;

    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setup(@TempDir final Path tmp) {
        prevOut = System.out;
        prevErr = System.err;
        prevIn = System.in;
        prevHome = System.getProperty("user.home");
        prevCleanup = System.getProperty("cleanupSeconds");

        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));

        System.setProperty("user.home", tmp.toString());
        System.setProperty("cleanupSeconds", "3600");
    }

    @AfterEach
    void tearDown() {
        System.setOut(prevOut);
        System.setErr(prevErr);
        System.setIn(prevIn);

        if (prevHome != null) {
            System.setProperty("user.home", prevHome);
        } else {
            System.clearProperty("user.home");
        }

        if (prevCleanup != null) {
            System.setProperty("cleanupSeconds", prevCleanup);
        } else {
            System.clearProperty("cleanupSeconds");
        }
    }

    private void setInput(final String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void runWithArgs_help() {
        final App app = new App();
        app.run(new String[]{"help"});
        final String s = out.toString(StandardCharsets.UTF_8);
        assertTrue(s.contains("URL Shorter"));
        assertTrue(s.contains("Commands:"));
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("ERROR:"));
    }

    @Test
    void run_exit() {
        setInput("exit\n");
        final App app = new App();
        app.run(null);
        final String s = out.toString(StandardCharsets.UTF_8);
        assertTrue(s.contains("URL Shorter"));
        assertTrue(s.contains(")> "));
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("ERROR:"));
    }

    @Test
    void run_help_exit() {
        setInput("help\nexit\n");
        final App app = new App();
        app.run(null);
        final String s = out.toString(StandardCharsets.UTF_8);
        assertTrue(s.contains("Commands:"));
    }

    @Test
    void run_unknown_exit() {
        setInput("unknown\nexit\n");
        final App app = new App();
        app.run(null);
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("ERROR:"));
    }

    @Test
    void run_blank_then_exit_noError() {
        setInput("\n   \nexit\n");
        final App app = new App();
        app.run(null);
        assertFalse(err.toString(StandardCharsets.UTF_8).contains("ERROR:"));
    }
}
