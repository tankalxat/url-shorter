package ru.tasks.dao;

import ru.tasks.session.Link;
import ru.tasks.session.Notification;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Repository {

    private final Path file;
    private final Storage storage;

    public Repository(final Path file) {
        this.file = file;
        this.storage = load(file);
    }

    public synchronized Link get(final String code) {
        return storage.linksByCode.get(code);
    }

    public synchronized boolean existsCode(final String code) {
        return storage.linksByCode.containsKey(code);
    }

    public synchronized void add(final Link link) {
        storage.linksByCode.put(link.code(), link);
        storage.codesByUser.computeIfAbsent(link.ownerId(), k -> new HashSet<>()).add(link.code());
        persist();
    }

    public synchronized boolean delete(final UUID user, final String code) {
        final Link link = storage.linksByCode.get(code);

        if (link == null || !link.ownerId().equals(user)) {
            return false;
        }

        storage.linksByCode.remove(code);
        storage.codesByUser.getOrDefault(user, Set.of()).remove(code);
        persist();
        return true;
    }

    public synchronized boolean updateLimit(final UUID user, final String code, final int newLimit) {
        final Link link = storage.linksByCode.get(code);

        if (link == null || !link.ownerId().equals(user)) {
            return false;
        }

        link.setMaxClicks(newLimit);
        persist();
        return true;
    }

    public synchronized List<Link> listByUser(final UUID user) {
        final Set<String> codes = storage.codesByUser.getOrDefault(user, Set.of());
        final List<Link> res = new ArrayList<>();

        for (final String c : codes) {
            final Link link = storage.linksByCode.get(c);

            if (link != null) {
                res.add(link);
            }
        }

        res.sort(Comparator.comparing(Link::createdAt));
        return res;
    }

    public synchronized void notify(final UUID user, final String msg) {
        storage.notifications.computeIfAbsent(user, k -> new ArrayDeque<>())
                             .addLast(new Notification(Instant.now(), msg));
        persist();
    }

    public synchronized List<Notification> resetNotifications(final UUID user) {
        final Deque<Notification> notifications = storage.notifications.get(user);

        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        }

        final List<Notification> out = new ArrayList<>(notifications);
        notifications.clear();
        persist();
        return out;
    }

    public synchronized int deleteExpiredLinks() {
        final Instant now = Instant.now();
        final List<String> links = new ArrayList<>();

        for (final Map.Entry<String, Link> e : storage.linksByCode.entrySet()) {
            if (e.getValue().isExpired(now)) {
                links.add(e.getKey());
            }
        }

        for (final String code : links) {
            final Link link = storage.linksByCode.remove(code);

            if (link != null) {
                final Set<String> set = storage.codesByUser.get(link.ownerId());

                if (set != null) {
                    set.remove(code);
                }

                notify(link.ownerId(), "Link '" + code + "' expired and removed.");
            }
        }

        if (!links.isEmpty()) {
            persist();
        }

        return links.size();
    }

    public synchronized void persist() {
        save(file, storage);
    }

    private static Storage load(final Path file) {
        if (!Files.exists(file)) {
            return new Storage();
        }

        try (final ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            return (Storage) in.readObject();
        } catch (final Exception e) {
            System.err.println("failed to read: " + e.getMessage());
            return new Storage();
        }
    }

    private static void save(final Path file, final Storage storage) {
        try {
            Files.createDirectories(file.getParent());
            try (
                    final ObjectOutputStream out = new ObjectOutputStream(
                            new BufferedOutputStream(
                                    Files.newOutputStream(
                                            file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
                                    )
                            )
                    )
            ) {
                out.writeObject(storage);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
