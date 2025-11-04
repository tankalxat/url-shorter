# URL Shorter

Принимает длинный URL и возвращает короткую ссылку. Ссылка привязана к пользователю.

## Настройки

- `-DttlMinutes=<minutes>` — срок жизни ссылки (по умолчанию 1440 мин = 24ч)
- `-DcleanupSeconds=<sec>` — частота очистки неактивных записей

## Команды

```text
help — показать помощь
me — показать текущий UUID пользователя
switch <uuid> — переключиться на другой UUID (для тестов)
shorten <url> [maxClicks] — создать короткую ссылку (UUID создаётся автоматически при первом вызове, если нет активной сессии)
list — список моих ссылок (код, URL, даты, лимиты, статус)
open <code|clck.ru/code> — открыть исходный URL в браузере
delete <code> — удалить ссылку
limit <code> <newLimit|0=unlimited> — изменить лимит переходов на ссылке
inbox — показать уведомления
exit — выход
```

## Примеры

```text
(f00dd525-7bad-4be7-8129-4a4c46eaf97a)> switch 11111111-2222-3333-4444-555555555555
Switched user to: 11111111-2222-3333-4444-555555555555
(11111111-2222-3333-4444-555555555555)> shorten https://github.com/tankalxat/url-shorter 2
Short link: clck.ru/p7vmwYt
(11111111-2222-3333-4444-555555555555)> list
CODE       OWNER    URL                            CREATED             EXPIRES             MAX    USED   STATUS  
p7vmwYt    11111111 https://github.com/tankalxat/… 2025-11-04 18:02:11 2025-11-05 18:02:11 2      0      OK      
(11111111-2222-3333-4444-555555555555)> open p7vmwYt
Opening: https://github.com/tankalxat/url-shorter
(11111111-2222-3333-4444-555555555555)> list
CODE       OWNER    URL                            CREATED             EXPIRES             MAX    USED   STATUS  
p7vmwYt    11111111 https://github.com/tankalxat/… 2025-11-04 18:02:11 2025-11-05 18:02:11 2      1      OK      
(11111111-2222-3333-4444-555555555555)> open clck.ru/p7vmwYt
Opening: https://github.com/tankalxat/url-shorter
(11111111-2222-3333-4444-555555555555)> list
CODE       OWNER    URL                            CREATED             EXPIRES             MAX    USED   STATUS  
p7vmwYt    11111111 https://github.com/tankalxat/… 2025-11-04 18:02:11 2025-11-05 18:02:11 2      2      BLOCKED 
(11111111-2222-3333-4444-555555555555)> inbox
[2025-11-04 18:03:14] Click limit for link 'p7vmwYt'. Link is blocked.
(11111111-2222-3333-4444-555555555555)> exit

Process finished with exit code 0
```
