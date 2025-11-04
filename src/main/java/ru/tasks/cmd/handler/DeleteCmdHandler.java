package ru.tasks.cmd.handler;

import ru.tasks.service.URLService;
import ru.tasks.session.Session;
import ru.tasks.cmd.CmdHandler;

import java.util.List;

import static ru.tasks.util.Utils.checkArgs;
import static ru.tasks.util.Utils.codeFromInput;
import static ru.tasks.util.Utils.requireUser;

public class DeleteCmdHandler extends CmdHandler {

    private final List<String> parts;
    private final URLService service;
    private final Session session;

    public DeleteCmdHandler(final List<String> parts, final URLService service, final Session session) {
        this.parts = parts;
        this.service = service;
        this.session = session;
    }

    @Override
    public void handle() {
        checkArgs(parts, 2);
        requireUser(session);

        final String code = codeFromInput(parts.get(1));
        final boolean ok = service.delete(session.user(), code);
        System.out.println(ok ? "Deleted." : "Not found / not owner.");
    }
}
