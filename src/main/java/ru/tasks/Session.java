package ru.tasks;

import ru.tasks.config.Config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

public class Session {

    private UUID user;
    private final Path userFile;

    public Session(final Path baseDir) {
        this.userFile = Config.userFile(baseDir);
    }

    public UUID user() {
        return user;
    }

    public void loadOrInit() {
        if (Files.exists(userFile)) {
            try {
                final String s = Files.readString(userFile).trim();

                if (!s.isEmpty()) {
                    this.user = UUID.fromString(s);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void initNewUser() {
        this.user = UUID.randomUUID();
        persist();
    }

    public void switchTo(final UUID user) {
        this.user = Objects.requireNonNull(user);
        persist();
    }

    private void persist() {
        try {
            Files.writeString(
                    userFile, user.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
