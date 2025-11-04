package ru.tasks.util;

import ru.tasks.session.Session;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    public static void requireUser(final Session session) {
        if (session.user() == null) {
            throw new IllegalStateException("No user UUID yet. Use 'shorten' or 'switch <uuid>'.");
        }
    }

    public static void checkArgs(final List<String> parts, final int min) {
        if (parts.size() < min) {
            throw new IllegalArgumentException("Invalid arguments. Type 'help'.");
        }
    }

    public static List<String> tokenize(final String line) {
        final List<String> out = new ArrayList<>();
        final Matcher m = Pattern.compile("\\s*\"([^\"]*)\"|\\S+").matcher(line);

        while (m.find()) {
            String token = m.group().trim();

            if (token.isBlank()) {
                continue;
            }

            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
            }

            out.add(token);
        }

        return out;
    }

    public static void printHelp() {
        System.out.println("""
                Commands:
                  help                                  - show this help
                  me                                    - show your UUID
                  switch <uuid>                         - switch to another user
                  shorten <url> [maxClicks]             - create short link (auto-creates user on first use)
                  list                                  - list my links
                  open <code|clck.ru/code>              - open original URL in browser
                  delete <code>                         - delete my link
                  limit <code> <newLimit|0=unlimited>   - update click limit for my link
                  inbox                                 - show notifications
                  exit                                  - quit
                """);
    }

    public static String codeFromInput(final String in) {
        if (in == null) {
            return null;
        }

        final int i = in.lastIndexOf('/');
        return i >= 0 ? in.substring(i + 1) : in;
    }

    public static void openInBrowser(final String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Desktop browse not supported in this environment.");
            }
        } catch (final IOException | URISyntaxException e) {
            System.out.println("Failed to open browser: " + e.getMessage());
        }
    }

    public static String fmt(final Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(instant);
    }

    public static String truncate(final String s, final int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    public static String shortUuid(final UUID id) {
        return id.toString().substring(0, 8);
    }

    public static Long parsePositiveLong(final String s) {
        try {
            final long v = Long.parseLong(s.trim());
            return v > 0 ? v : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private Utils() {
    }
}
