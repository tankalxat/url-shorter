package ru.tasks.cmd.handler;

import ru.tasks.session.Link;
import ru.tasks.service.URLService;
import ru.tasks.session.Session;
import ru.tasks.cmd.CmdHandler;

import java.time.Instant;
import java.util.List;

import static ru.tasks.util.Utils.fmt;
import static ru.tasks.util.Utils.requireUser;
import static ru.tasks.util.Utils.shortUuid;
import static ru.tasks.util.Utils.truncate;

public class ListCmdHandler extends CmdHandler {

    private final URLService service;
    private final Session session;

    public ListCmdHandler(final URLService service, final Session session) {
        this.service = service;
        this.session = session;
    }

    @Override
    public void handle() {
        requireUser(session);

        final List<Link> links = service.listByUser(session.user());

        if (links.isEmpty()) {
            System.out.println("No links.");
        } else {
            System.out.printf(
                    "%-10s %-8s %-30s %-19s %-19s %-6s %-6s %-8s%n", "CODE", "OWNER", "URL", "CREATED",
                    "EXPIRES", "MAX", "USED", "STATUS"
            );

            for (final Link link : links) {
                final String status = link.isExpired(Instant.now())
                        ? "EXPIRED"
                        : (link.isLimitReached() ? "BLOCKED" : "OK");
                System.out.printf(
                        "%-10s %-8s %-30s %-19s %-19s %-6s %-6s %-8s%n", link.code(),
                        shortUuid(link.ownerId()), truncate(link.originalUrl(), 30),
                        fmt(link.createdAt()), fmt(link.expiresAt()),
                        link.maxClicks() < 1 ? "NO" : String.valueOf(link.maxClicks()),
                        link.clicksMade(), status
                );
            }
        }
    }
}
