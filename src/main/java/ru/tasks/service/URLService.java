package ru.tasks.service;

import ru.tasks.config.Config;
import ru.tasks.dao.Repository;
import ru.tasks.response.OpenResult;
import ru.tasks.session.Link;
import ru.tasks.session.Notification;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class URLService {

    private final Repository repo;
    private final SecureRandom rnd = new SecureRandom();
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public URLService(final Repository repo) {
        this.repo = repo;
    }

    public Link createShortLink(final UUID user, final String longUrl, final Integer maxClicks) {
        Objects.requireNonNull(longUrl, "url");

        final String code = generateUniqueCode();
        final Instant now = Instant.now();
        final Instant exp = now.plus(Config.linkTtl());
        final int limit = (maxClicks == null ? Config.DEFAULT_MAX_CLICKS : maxClicks);
        final Link link = new Link(
                code, normalizeUrl(longUrl), Objects.requireNonNullElse(user, UUID.randomUUID()), now, exp, limit
        );
        repo.add(link);

        return link;
    }

    public OpenResult open(final String code) {
        final Link link = repo.get(code);

        if (link == null) {
            return OpenResult.notFound();
        }

        final Instant now = Instant.now();

        if (link.isExpired(now)) {
            repo.delete(link.ownerId(), code);
            repo.notify(link.ownerId(), "Attempted to open expired link '" + code + "'. It was removed.");
            return OpenResult.expired();
        }

        if (link.isLimitReached()) {
            repo.notify(link.ownerId(), "Click limit reached for link '" + code + "'. Link is blocked.");
            return OpenResult.limitReached();
        }

        synchronized (repo) {
            link.incClicks();
            repo.persist();
        }

        if (link.isLimitReached()) {
            repo.notify(link.ownerId(), "Click limit for link '" + code + "'. Link is blocked.");
        }

        return OpenResult.fromURL(link.originalUrl());
    }

    public boolean delete(final UUID user, final String code) {
        return repo.delete(user, code);
    }

    public boolean updateLimit(final UUID user, final String code, final int newLimit) {
        return repo.updateLimit(user, code, newLimit);
    }

    public List<Link> listByUser(final UUID user) {
        return repo.listByUser(user);
    }

    public List<Notification> resetNotifications(final UUID user) {
        return repo.resetNotifications(user);
    }

    public int deleteExpiredLinks() {
        return repo.deleteExpiredLinks();
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 10000; i++) {
            final String code = randomBase62(7 + rnd.nextInt(2));

            if (!repo.existsCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Failed to get unique code");
    }

    private String randomBase62(final int len) {
        final StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(BASE62.charAt(rnd.nextInt(BASE62.length())));
        }

        return sb.toString();
    }

    private static String normalizeUrl(final String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        return "https://" + url;
    }
}
