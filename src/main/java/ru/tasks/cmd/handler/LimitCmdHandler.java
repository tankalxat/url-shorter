package ru.tasks.cmd.handler;

import ru.tasks.URLService;
import ru.tasks.Session;
import ru.tasks.cmd.CmdHandler;

import java.util.List;

import static ru.tasks.util.Utils.checkArgs;
import static ru.tasks.util.Utils.codeFromInput;
import static ru.tasks.util.Utils.requireUser;

public class LimitCmdHandler extends CmdHandler {

    private final List<String> parts;
    private final URLService service;
    private final Session session;

    public LimitCmdHandler(final List<String> parts, final URLService service, final Session session) {
        this.parts = parts;
        this.service = service;
        this.session = session;
    }

    @Override
    public void handle() {
        checkArgs(parts, 3);
        requireUser(session);

        final String code = codeFromInput(parts.get(1));
        final int newLimit = Integer.parseInt(parts.get(2));
        final boolean ok = service.updateLimit(session.user(), code, newLimit);
        System.out.println(ok ? "Limit updated." : "Not found / not owner.");
    }
}
