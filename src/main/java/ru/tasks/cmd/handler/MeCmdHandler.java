package ru.tasks.cmd.handler;

import ru.tasks.Session;
import ru.tasks.cmd.CmdHandler;

public class MeCmdHandler extends CmdHandler {

    private final Session session;

    public MeCmdHandler(final Session session) {
        this.session = session;
    }

    @Override
    public void handle() {
        System.out.println("Your UUID: " + session.user());
    }
}
