package ru.tasks.cmd;

import ru.tasks.service.URLService;
import ru.tasks.session.Session;
import ru.tasks.cmd.handler.DefaultCmdHandler;
import ru.tasks.cmd.handler.DeleteCmdHandler;
import ru.tasks.cmd.handler.HelpCmdHandler;
import ru.tasks.cmd.handler.InboxCmdHandler;
import ru.tasks.cmd.handler.LimitCmdHandler;
import ru.tasks.cmd.handler.ListCmdHandler;
import ru.tasks.cmd.handler.MeCmdHandler;
import ru.tasks.cmd.handler.OpenCmdHandler;
import ru.tasks.cmd.handler.ShortenCmdHandler;
import ru.tasks.cmd.handler.SwitchCmdHandler;

import java.util.List;

public final class CmdHandlerFactory {

    public static CmdHandler from(final String command, final List<String> parts, final URLService service,
                                  final Session session) {
        return switch (command) {
            case "help" -> new HelpCmdHandler();
            case "me" -> new MeCmdHandler(session);
            case "open" -> new OpenCmdHandler(parts, service);
            case "list" -> new ListCmdHandler(service, session);
            case "inbox" -> new InboxCmdHandler(service, session);
            case "switch" -> new SwitchCmdHandler(parts, session);
            case "limit" -> new LimitCmdHandler(parts, service, session);
            case "delete" -> new DeleteCmdHandler(parts, service, session);
            case "shorten" -> new ShortenCmdHandler(parts, service, session);
            default -> new DefaultCmdHandler();
        };
    }

    private CmdHandlerFactory() {
    }
}
