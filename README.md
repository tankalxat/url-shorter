# URL Shorter

Принимает длинный URL и возвращает короткую ссылку. Ссылка привязана к пользователю.

## Настройки

- `-DttlMinutes=<minutes>` — срок жизни ссылки (по умолчанию 1440 мин = 24ч)
- `-DcleanupSeconds=<sec>` — частота очистки неактивных записей

Эти же настройки предпочтительно берутся из файла конфигурации `resources/config.env`.

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

## Описание архитектуры

Классы:

- `Link` - инкапсулированный URL
- `Session` - логика логина пользователей
- `Config` - всё про конфигурацию приложения
- `App` - основная часть работы приложения с cli-циклом и чистильщиком старых записей
- `CmdHandler` - семейство обработчиков введенных команд
- `Repository` - логика работы с данными приложения
- `OpenResult` - результат открытия ссылки
- `URLService` - логика работы со ссылками и пользователями

## Тестирование

Последовательность команд, покрывающая все требования ТЗ.

```text
help
switch 11111111-2222-3333-4444-555555555555
me
shorten
shorten invalid 2
shorten https://github.com/tankalxat/url-shorter 2
list
open
open UNKNOWN
open <code из list>
open clck.ru/<code из list>
list
switch
switch not-uuid
switch 11111111-2222-3333-4444-666666666666
me
list
open <code из list прошлого пользователя>
delete <code из list прошлого пользователя>
switch 11111111-2222-3333-4444-555555555555
open <code из list>
inbox
limit <code из list>
limit <code из list> 3
list
open <code из list>
list
inbox
switch 11111111-2222-3333-4444-666666666666
shorten https://github.com/tankalxat/url-shorter 2
list
open <УНИКАЛЬНЫЙ code из list>
limit UNKNOWN 12
delete <УНИКАЛЬНЫЙ code из list>
list
brbrbrbaaabababobobobobibibibooboo228
exit
```
