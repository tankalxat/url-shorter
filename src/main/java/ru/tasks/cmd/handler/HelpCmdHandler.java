package ru.tasks.cmd.handler;

import ru.tasks.cmd.CmdHandler;

import static ru.tasks.util.Utils.printHelp;

public class HelpCmdHandler extends CmdHandler {

    @Override
    public void handle() {
        printHelp();
    }
}
