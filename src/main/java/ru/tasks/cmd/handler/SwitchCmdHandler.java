package ru.tasks.cmd.handler;

import ru.tasks.session.Session;
import ru.tasks.cmd.CmdHandler;

import java.util.List;
import java.util.UUID;

import static ru.tasks.util.Utils.checkArgs;

public class SwitchCmdHandler extends CmdHandler {

    private final List<String> parts;
    private final Session session;

    public SwitchCmdHandler(final List<String> parts, final Session session) {
        this.parts = parts;
        this.session = session;
    }

    @Override
    public void handle() {
        checkArgs(parts, 2);
        session.switchTo(UUID.fromString(parts.get(1)));
        System.out.println("Switched user to: " + session.user());
    }
}
