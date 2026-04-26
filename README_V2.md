# Hermes Visual Node Android v2.0

Полнофункциональное Android приложение для управления телефоном через Алису (Hermes Agent).

## Новые возможности v2.0

### 🎨 Современный UI
- **Jetpack Compose** — современный декларативный UI фреймворк
- **Material Design 3** — красивый интерфейс в стиле Яндекс Алисы
- **Тёмная/светлая тема** — автоматическое переключение
- **Динамические цвета** (Android 12+)

### 💬 Встроенный чат
- Полноценный чат с Алисой прямо в приложении
- Отправка текстовых сообщений
- Прикрепление скриншотов к сообщениям
- История переписки
- Красивые пузыри сообщений с временными метками

### 📸 Улучшенные скриншоты
- Быстрый скриншот через плавающую кнопку
- Автоматическое открытие чата после скриншота
- Превью скриншота перед отправкой
- Возможность добавить текст к скриншоту

### 🎯 Полное управление телефоном
- **Клики** по координатам
- **Свайпы** в любом направлении
- **Ввод текста** в поля
- **Навигация**: Назад, Домой, Недавние приложения
- **Системные действия**: Уведомления, Быстрые настройки
- **Открытие приложений** по package name
- **Поиск элементов** по тексту
- **Клик по тексту** элемента
- **Чтение всего текста** с экрана

### ⚙️ Удобные настройки
- Настройка URL сервера
- Настройка ID устройства
- Управление плавающей кнопкой
- Очистка истории чата
- Информация о подключении

## Архитектура

```
MainActivity (Compose)
├── ChatScreen — основной экран с чатом
├── SettingsScreen — настройки
└── ChatViewModel — управление состоянием

OverlayService
├── Плавающая кнопка
├── Overlay чат (поверх приложений)
└── PhoneController — выполнение команд

HermesAccessibilityService
├── Клики и свайпы
├── Ввод текста
├── Системные действия
└── Анализ UI элементов

ScreenshotActivity
└── Захват экрана через Media Projection API
```

## Установка и настройка

### 1. Сборка APK

```bash
cd ~/hermes-visual-nodes/android-app

# Через Gradle
./gradlew assembleRelease

# APK будет в: app/build/outputs/apk/release/app-release.apk
```

### 2. Установка на телефон

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

### 3. Настройка разрешений

После установки нужно предоставить разрешения:

1. **Отображение поверх других приложений**
   - Настройки → Приложения → Hermes Visual Node → Отображение поверх других приложений → Разрешить

2. **Специальные возможности** (для полного управления)
   - Настройки → Специальные возможности → Hermes Visual Node → Включить
   - ⚠️ Это даст приложению полный контроль над телефоном!

3. **Уведомления** (для foreground service)
   - Автоматически запрашивается при первом запуске

### 4. Настройка подключения

1. Откройте приложение
2. Нажмите на иконку настроек (⚙️)
3. Введите URL сервера (например: `http://192.168.1.100:8766`)
4. Введите ID устройства (например: `my-phone`)
5. Нажмите "Сохранить и подключиться"

### 5. Запуск плавающей кнопки

В настройках нажмите "Запустить" в секции "Плавающая кнопка"

## Использование

### Чат в приложении

1. Откройте приложение
2. Напишите сообщение Алисе
3. Нажмите кнопку камеры для прикрепления скриншота
4. Отправьте сообщение

### Плавающая кнопка

1. Плавающая кнопка видна поверх всех приложений
2. Нажмите на неё для создания скриншота
3. Автоматически откроется overlay чат
4. Добавьте текст к скриншоту и отправьте

### Команды для Алисы

Алиса может выполнять команды через AccessibilityService:

```python
# Клик по координатам
controller.executeCommand("click", {"x": 500, "y": 300})

# Свайп
controller.executeCommand("swipe", {
    "start_x": 500, "start_y": 1000,
    "end_x": 500, "end_y": 300,
    "duration": 300
})

# Ввод текста
controller.executeCommand("type", {"text": "Hello World"})

# Системные действия
controller.executeCommand("back", {})
controller.executeCommand("home", {})
controller.executeCommand("recents", {})
controller.executeCommand("notifications", {})

# Открыть приложение
controller.executeCommand("open_app", {"package": "com.android.chrome"})

# Найти элемент по тексту
controller.executeCommand("find_text", {"text": "Settings"})

# Кликнуть по тексту
controller.executeCommand("click_text", {"text": "OK"})

# Получить весь текст с экрана
controller.executeCommand("get_screen_text", {})
```

## API Endpoints

Приложение взаимодействует с Hermes через следующие endpoints:

### POST /nodes/register
Регистрация устройства
```json
{
  "node_id": "my-phone",
  "url": "android://my-phone"
}
```

### POST /chat
Отправка сообщения в чат
```json
{
  "message": "Привет, Алиса!",
  "image": "base64_encoded_screenshot"
}
```

Response:
```json
{
  "response": "Привет! Чем могу помочь?",
  "timestamp": 1714089600000
}
```

### GET /chat/history
Получение истории чата

Response:
```json
[
  {
    "role": "user",
    "content": "Привет",
    "timestamp": 1714089600000
  },
  {
    "role": "assistant",
    "content": "Привет!",
    "timestamp": 1714089601000
  }
]
```

### POST /command/result
Результат выполнения команды (отправляется приложением)
```json
{
  "command": "click",
  "result": {
    "success": true,
    "message": "Clicked at (500, 300)"
  },
  "timestamp": 1714089600000
}
```

## Интеграция с Hermes

### Серверная часть

Нужно добавить endpoints в central_server.py:

```python
@app.route('/chat', methods=['POST'])
async def chat():
    data = await request.get_json()
    message = data.get('message', '')
    image_base64 = data.get('image')
    
    # Обработать через Hermes Agent
    response = await process_with_hermes(message, image_base64)
    
    return jsonify({
        'response': response,
        'timestamp': int(time.time() * 1000)
    })

@app.route('/chat/history', methods=['GET'])
async def chat_history():
    # Вернуть историю из базы
    return jsonify(get_chat_history())

@app.route('/command/result', methods=['POST'])
async def command_result():
    data = await request.get_json()
    # Сохранить результат выполнения команды
    save_command_result(data)
    return jsonify({'status': 'ok'})
```

## Безопасность

⚠️ **ВАЖНО**: Приложение получает полный контроль над телефоном!

- Используйте только в доверенной сети
- Не подключайтесь к публичным серверам
- AccessibilityService может читать всё на экране
- Приложение может выполнять любые действия от вашего имени

## Troubleshooting

### Плавающая кнопка не появляется
- Проверьте разрешение "Отображение поверх других приложений"
- Перезапустите приложение

### Команды не выполняются
- Убедитесь что AccessibilityService включён
- Настройки → Специальные возможности → Hermes Visual Node

### Скриншоты не работают
- Требуется Android 5.0+
- Предоставьте разрешение при первом запросе

### Не подключается к серверу
- Проверьте URL сервера
- Убедитесь что телефон и сервер в одной сети
- Проверьте firewall

## Зависимости

```gradle
// Jetpack Compose
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.activity:activity-compose

// Networking
com.squareup.okhttp3:okhttp
com.squareup.retrofit2:retrofit
com.squareup.retrofit2:converter-gson

// Image loading
io.coil-kt:coil-compose

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android
```

## Версии

- **v1.0** — Базовая плавающая кнопка + скриншоты
- **v2.0** — Compose UI + чат + полное управление телефоном

## Лицензия

MIT
