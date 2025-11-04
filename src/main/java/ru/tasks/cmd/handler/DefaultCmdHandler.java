package ru.tasks.cmd.handler;

import ru.tasks.cmd.CmdHandler;

public class DefaultCmdHandler extends CmdHandler {

    @Override
    public void handle() {
        System.out.println("Unknown command. Use 'help'.");
    }
}
