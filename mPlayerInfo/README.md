# mPlayerInfo
Плагин меню информации об игроке для сервера Minecraft Paper 1.21+.
Открывает GUI с головой, инвентарём, эндер-сундуком, состоянием, слежкой и заморозкой.

## Требования

- Java 21
- Paper 1.21+

## Установка

1. Скачайте плагин mPlayerInfo
2. Поместите файл .jar в папку `plugins` вашего сервера
3. Перезапустите сервер
4. Настройте конфигурацию в `plugins/mPlayerInfo/config.yml`

## Команды

### `/mplayerinfo <nick>`
Открывает меню информации об игроке: ник, онлайн, мир, координаты, пинг, время сессии, здоровье, голод, эффекты. Алиасы: `/playerinfo`, `/pinfo`, `/mi`.

### `/mplayerinfo reload`
Перезагружает конфигурацию плагина.

### `/invsee <nick>`
Открывает инвентарь выбранного игрока: всё содержимое, броня и вторая рука. Изменения сохраняются в инвентарь игрока. Алиасы: `/inv`, `/openinv`.

## Меню

- Голова игрока — основная информация (слот 22)
- Сундук — открыть инвентарь игрока (слот 29)
- Эндер-сундук — открыть эндер-сундук игрока (слот 30)
- Яблоко — состояние: здоровье, голод, режим, уровень, эффекты (слот 31)
- Зелье — слежка: ваниш + телепорт к игроку (слот 32)
- Лёд — заморозить / разморозить игрока (слот 33)

Все слоты, материалы, названия, лор и действия меняются в конфиге.

## Права

- `mplayerinfo.use` - Позволяет использовать все команды плагина (по умолчанию: op)

## Конфигурация

```yaml
menu:
  title: "&8Информация о &c{target}"
  size: 54
  filler:
    mode: "BORDER"                    # BORDER - только рамка, EMPTY - все пустые слоты
    material: PINK_STAINED_GLASS_PANE
  items:
    head:
      slot: 22
      material: PLAYER_HEAD
      action: INFO
    inventory:
      slot: 29
      material: CHEST
      action: INVSEE                  # открыть инвентарь (как /invsee)
    enderchest:
      slot: 30
      material: ENDER_CHEST
      action: ENDERSEE                 # открыть эндер-сундук
    state:
      slot: 31
      material: APPLE
      action: STATE                   # только показывает лор
    vanish:
      slot: 32
      material: POTION
      action: VANISH                  # ваниш + телепорт к игроку
    freeze:
      slot: 33
      material: ICE
      action: FREEZE                  # заморозить / разморозить
```

### Плейсхолдеры
`{target}`, `{viewer}`, `{uuid}`, `{online}`, `{world}`, `{x}`, `{y}`, `{z}`, `{ping}`, `{session}`, `{health}`, `{food}`, `{gamemode}`, `{level}`, `{effects}`, `{frozen}`, `{vanished}`.

## Сборка

Для сборки плагина используйте Maven:

```bash
mvn clean package
```

Готовый файл будет находиться в папке `target`.

## Версия

Текущая версия: 1.0-BETA
