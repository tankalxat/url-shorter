package ru.tasks.session;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class Link implements Serializable {

    private final String code;
    private final String originalUrl;
    private final UUID ownerId;
    private final Instant createdAt;
    private final Instant expiresAt;
    private int maxClicks;
    private int clicksMade;

    public Link(final String code, final String originalUrl, final UUID ownerId, final Instant createdAt,
                final Instant expiresAt, final int maxClicks) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxClicks = maxClicks;
        this.clicksMade = 0;
    }

    public String code() {
        return code;
    }

    public String originalUrl() {
        return originalUrl;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public int maxClicks() {
        return maxClicks;
    }

    public int clicksMade() {
        return clicksMade;
    }

    public void setMaxClicks(int v) {
        this.maxClicks = v;
    }

    public void incClicks() {
        this.clicksMade++;
    }

    public boolean isLimitReached() {
        return maxClicks > 0 && clicksMade >= maxClicks;
    }

    public boolean isExpired(final Instant now) {
        return now.isAfter(expiresAt);
    }
}