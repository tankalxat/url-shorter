package ru.tasks.dao;

import ru.tasks.session.Link;
import ru.tasks.session.Notification;

import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Storage implements Serializable {

    public final Map<String, Link> linksByCode = new HashMap<>();
    public final Map<UUID, Set<String>> codesByUser = new HashMap<>();
    public final Map<UUID, Deque<Notification>> notifications = new HashMap<>();
}
