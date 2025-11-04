package ru.tasks;

public record OpenResult(OpenStatus status, String url) {

    public static OpenResult fromURL(final String url) {
        return new OpenResult(OpenStatus.OPENED, url);
    }

    public static OpenResult notFound() {
        return new OpenResult(OpenStatus.NOT_FOUND, null);
    }

    public static OpenResult expired() {
        return new OpenResult(OpenStatus.EXPIRED, null);
    }

    public static OpenResult limitReached() {
        return new OpenResult(OpenStatus.LIMIT_REACHED, null);
    }
}
