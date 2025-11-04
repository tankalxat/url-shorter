package ru.tasks.cmd;

import ru.tasks.service.URLService;
import ru.tasks.config.Config;
import ru.tasks.dao.Repository;
import ru.tasks.session.Session;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.tasks.util.Utils.tokenize;

public class App {

    private final Path baseDir;
    private final URLService service;
    private final ScheduledExecutorService scheduler;

    public App() {
        this.baseDir = Config.baseDir();
        this.service = new URLService(new Repository(Config.dataFile(baseDir)));
        final long cleanupSeconds = Config.cleanupSeconds();
        this.scheduler = getScheduler();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                final int result = service.deleteExpiredLinks();

                if (result > 0) {
                    System.out.println("[cleaner] removed expired links: " + result);
                }
            } catch (final Throwable t) {
                System.err.println("[cleaner] error: " + t.getMessage());
            }
        }, cleanupSeconds, cleanupSeconds, TimeUnit.SECONDS);
    }

    private static ScheduledExecutorService getScheduler() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "cleaner");
            t.setDaemon(true);
            return t;
        });
    }

    public void run(final String[] args) {
        System.out.printf("""
                
                URL Shorter
                Data dir: %s
                Link TTL: %s
                Type 'help' to see commands. 'exit' to quit.
                
                %n""", baseDir, Config.linkTtl());

        final Scanner scanner = new Scanner(System.in);
        final Session session = new Session(baseDir);
        session.loadOrInit();

        if (args != null && args.length > 0) {
            executeArguments(session, args);
            return;
        }

        while (true) {
            try {
                System.out.print("(" + session.user() + ")> ");
                String line = scanner.nextLine();

                if (line == null) {
                    break;
                }

                if (line.isBlank()) {
                    continue;
                }

                line = line.trim();

                if (Objects.equals(line, "exit") || Objects.equals(line, "quit")) {
                    break;
                }

                handleCommand(session, line);
            } catch (final NoSuchElementException e) {
                break;
            } catch (final Throwable t) {
                System.err.println("ERROR: " + t.getMessage());
            }
        }

        scheduler.shutdownNow();
    }

    private void executeArguments(final Session session, final String[] args) {
        handleCommand(session, String.join(" ", args));
    }

    private void handleCommand(final Session session, final String line) {
        final List<String> parts = tokenize(line);

        if (parts.isEmpty()) {
            return;
        }

        final String command = parts.getFirst().toLowerCase(Locale.ROOT);
        CmdHandlerFactory.from(command, parts, service, session).handle();
    }
}
