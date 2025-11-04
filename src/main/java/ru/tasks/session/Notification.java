package ru.tasks.session;

import java.io.Serializable;
import java.time.Instant;

public record Notification(Instant timestamp, String message) implements Serializable {
}
