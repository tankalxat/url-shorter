package ru.tasks.cmd.handler;

import ru.tasks.Notification;
import ru.tasks.URLService;
import ru.tasks.Session;
import ru.tasks.cmd.CmdHandler;

import java.util.List;

import static ru.tasks.util.Utils.fmt;
import static ru.tasks.util.Utils.requireUser;

public class InboxCmdHandler extends CmdHandler {

    private final URLService service;
    private final Session session;

    public InboxCmdHandler(final URLService service, final Session session) {
        this.service = service;
        this.session = session;
    }

    @Override
    public void handle() {
        requireUser(session);

        final List<Notification> notifications = service.resetNotifications(session.user());

        if (notifications.isEmpty()) {
            System.out.println("No new notifications.");
        } else {
            for (final Notification n : notifications) {
                System.out.println("[" + fmt(n.timestamp()) + "] " + n.message());
            }
        }
    }
}
