package ru.tasks.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class Config {

    public static final String BASE_SHORT_HOST = "clck.ru/";
    public static final String DIR_NAME = ".urlshortener";
    public static final String DATA_FILE_NAME = "data.bin";
    public static final String USER_FILE_NAME = "user.uuid";
    public static final int DEFAULT_MAX_CLICKS = 0;
    public static final long DEFAULT_CLEANUP_SECONDS = 60;

    public static Duration linkTtl() {
        long minutes = Long.getLong("ttlMinutes", 24L * 60L);
        return Duration.ofMinutes(minutes);
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
}
