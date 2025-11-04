package ru.tasks.cmd.handler;

import ru.tasks.Link;
import ru.tasks.URLService;
import ru.tasks.Session;
import ru.tasks.cmd.CmdHandler;
import ru.tasks.config.Config;

import java.util.List;

import static ru.tasks.util.Utils.checkArgs;

public class ShortenCmdHandler extends CmdHandler {

    private final List<String> parts;
    private final URLService service;
    private final Session session;

    public ShortenCmdHandler(final List<String> parts, final URLService service, final Session session) {
        this.parts = parts;
        this.service = service;
        this.session = session;
    }

    @Override
    public void handle() {
        checkArgs(parts, 2);

        final String longUrl = parts.get(1);
        Integer maxClicks = null;

        if (parts.size() >= 3) {
            maxClicks = Integer.parseInt(parts.get(2));

            if (maxClicks < 1) {
                maxClicks = null;
            }
        }

        if (session.user() == null) {
            session.initNewUser();
            System.out.println("Generated your UUID: " + session.user());
        }

        final Link shortLink = service.createShortLink(session.user(), longUrl, maxClicks);
        System.out.println("Short link: " + Config.BASE_SHORT_HOST + shortLink.code());
    }
}
