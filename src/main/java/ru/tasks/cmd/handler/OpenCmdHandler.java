package ru.tasks.cmd.handler;

import ru.tasks.OpenResult;
import ru.tasks.URLService;
import ru.tasks.cmd.CmdHandler;

import java.util.List;

import static ru.tasks.util.Utils.checkArgs;
import static ru.tasks.util.Utils.codeFromInput;
import static ru.tasks.util.Utils.openInBrowser;

public class OpenCmdHandler extends CmdHandler {

    private final List<String> parts;
    private final URLService service;

    public OpenCmdHandler(final List<String> parts, final URLService service) {
        this.parts = parts;
        this.service = service;
    }

    @Override
    public void handle() {
        checkArgs(parts, 2);

        final String token = parts.get(1);
        final String code = codeFromInput(token);
        final OpenResult openResult = service.open(code);

        switch (openResult.status()) {
            case OPENED -> {
                System.out.println("Opening: " + openResult.url());
                openInBrowser(openResult.url());
            }
            case NOT_FOUND -> System.out.println("Not found or removed.");
            case EXPIRED -> System.out.println("Link expired and unavailable.");
            case LIMIT_REACHED -> System.out.println("Click limit reached. Link is unavailable.");
        }
    }
}
