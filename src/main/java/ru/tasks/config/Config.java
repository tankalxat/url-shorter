package ru.tasks.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static ru.tasks.util.Utils.parsePositiveLong;

public class Config {

    public static final String BASE_SHORT_HOST = "clck.ru/";
    public static final String DIR_NAME = ".urlshortener";
    public static final String DATA_FILE_NAME = "data.bin";
    public static final String USER_FILE_NAME = "user.uuid";
    public static final int DEFAULT_MAX_CLICKS = 0;
    public static final long DEFAULT_CLEANUP_SECONDS = 60L;
    public static final long DEFAULT_TTL_MINUTES = 24L * 60L;

    public static Duration linkTtl() {
        final String cliProp = System.getProperty("ttlMinutes");

        if (cliProp != null) {
            final Long val = parsePositiveLong(cliProp);

            if (val != null) {
                return Duration.ofMinutes(val);
            }
        }

        final String envVal = loadEnv().get("ttlMinutes");

        if (envVal != null) {
            final Long val = parsePositiveLong(envVal);

            if (val != null) {
                return Duration.ofMinutes(val);
            }
        }

        return Duration.ofMinutes(DEFAULT_TTL_MINUTES);
    }

    public static long cleanupSeconds() {
        final String cliProp = System.getProperty("cleanupSeconds");

        if (cliProp != null) {
            final Long val = parsePositiveLong(cliProp);

            if (val != null) {
                return val;
            }
        }

        final String envVal = loadEnv().get("cleanupSeconds");

        if (envVal != null) {
            final Long val = parsePositiveLong(envVal);

            if (val != null) {
                return val;
            }
        }

        return DEFAULT_CLEANUP_SECONDS;
    }

    public static Path baseDir() {
        final Path home = Path.of(System.getProperty("user.home"));
        final Path dir = home.resolve(DIR_NAME);
        try {
            Files.createDirectories(dir);
        } catch (final IOException ignored) {
        }
        return dir;
    }

    public static Path dataFile(final Path baseDir) {
        return baseDir.resolve(DATA_FILE_NAME);
    }

    public static Path userFile(final Path baseDir) {
        return baseDir.resolve(USER_FILE_NAME);
    }

    private static Map<String, String> loadEnv() {
        final Map<String, String> out = new HashMap<>();

        InputStream in = null;
        final ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        if (ctx != null) {
            in = ctx.getResourceAsStream("config.env");
        }

        if (in == null) {
            in = Config.class.getClassLoader().getResourceAsStream("config.env");
        }

        if (in == null) {
            return out;
        }

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String s = line.trim();

                if (s.isBlank()) {
                    continue;
                }

                final int eq = s.indexOf('=');

                if (eq <= 0) {
                    continue;
                }

                final String key = s.substring(0, eq).trim();
                final String val = s.substring(eq + 1).trim();

                if (!key.isBlank() && !val.isBlank()) {
                    out.put(key, val);
                }
            }
        } catch (final IOException ignored) {
        }

        return out;
    }
}
