# Hermes Visual Node - Android App

Android приложение для системы визуального управления Hermes Agent.

## Возможности

- 🎯 Плавающая кнопка поверх всех приложений
- 📸 Захват скриншотов одним нажатием
- 🔄 Автоматическая отправка на центральный сервер
- ⚙️ Настройка URL сервера и имени устройства

## Сборка

### Автоматическая сборка (GitHub Actions)

1. Запушить код в GitHub
2. Actions автоматически соберёт APK
3. Скачать из Artifacts

### Локальная сборка

Требуется x86_64 машина или Android Studio.

```bash
./gradlew assembleDebug
# APK в app/build/outputs/apk/debug/app-debug.apk
```

## Установка

1. Скачать APK из GitHub Actions Artifacts
2. Установить на Android устройство
3. Разрешить "Поверх других приложений"
4. Настроить URL сервера (по умолчанию http://192.168.1.100:8766)
5. Запустить сервис

## Настройка

- **Server URL**: адрес центрального сервера Hermes Visual Nodes
- **Node Name**: имя этого устройства (для идентификации)

## Требования

- Android 5.0+ (API 21+)
- Разрешение "Поверх других приложений"
- Доступ к интернету

## Архитектура

- **MainActivity**: настройки приложения
- **OverlayService**: плавающая кнопка (foreground service)
- **ScreenshotActivity**: захват экрана через MediaProjection API

## Связь с Desktop системой

Это приложение работает вместе с:
- Desktop агентами (Python/pyautogui)
- Центральным сервером (:8766)
- Hermes Agent интеграцией

См. основной README в корне проекта.

## Лицензия

MIT
