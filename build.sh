#!/bin/bash
# Скрипт для быстрой сборки APK

set -e

echo "=== Сборка Hermes Visual Node Android App ==="
echo

# Проверка Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "❌ ANDROID_HOME не установлен"
    echo "Установите Android SDK и добавьте в PATH"
    echo "Или откройте проект в Android Studio"
    exit 1
fi

echo "✓ Android SDK найден: $ANDROID_HOME"

cd ~/hermes-visual-nodes/android-app

# Сборка
echo
echo "Сборка APK..."
./gradlew assembleDebug

echo
echo "✓ Сборка завершена!"
echo
echo "APK находится в:"
echo "  app/build/outputs/apk/debug/app-debug.apk"
echo
echo "Установка на устройство:"
echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
