#!/bin/bash

# Скрипт сборки Hermes Visual Node Android v2.0

set -e

echo "🚀 Начинаю сборку Hermes Visual Node v2.0..."

cd ~/hermes-visual-nodes/android-app

# Проверка зависимостей
echo "📦 Проверка зависимостей..."

if ! command -v java &> /dev/null; then
    echo "❌ Java не найдена. Установите JDK 11 или выше"
    exit 1
fi

echo "✅ Java: $(java -version 2>&1 | head -n 1)"

# Очистка предыдущей сборки
echo "🧹 Очистка предыдущей сборки..."
./gradlew clean

# Сборка debug APK
echo "🔨 Сборка debug APK..."
./gradlew assembleDebug

# Проверка результата
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
    echo "✅ Debug APK собран успешно!"
    echo "📦 Размер: $APK_SIZE"
    echo "📍 Путь: app/build/outputs/apk/debug/app-debug.apk"
    
    # Копировать в корень для удобства
    cp app/build/outputs/apk/debug/app-debug.apk hermes-visual-node-v2.0-debug.apk
    echo "📋 Скопирован в: hermes-visual-node-v2.0-debug.apk"
else
    echo "❌ Ошибка сборки!"
    exit 1
fi

# Опционально: сборка release APK
read -p "🤔 Собрать release APK? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🔨 Сборка release APK..."
    ./gradlew assembleRelease
    
    if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
        APK_SIZE=$(du -h app/build/outputs/apk/release/app-release-unsigned.apk | cut -f1)
        echo "✅ Release APK собран успешно!"
        echo "📦 Размер: $APK_SIZE"
        echo "📍 Путь: app/build/outputs/apk/release/app-release-unsigned.apk"
        
        cp app/build/outputs/apk/release/app-release-unsigned.apk hermes-visual-node-v2.0-release.apk
        echo "📋 Скопирован в: hermes-visual-node-v2.0-release.apk"
        echo "⚠️  Внимание: APK не подписан! Для установки нужна подпись."
    fi
fi

echo ""
echo "🎉 Сборка завершена!"
echo ""
echo "📱 Установка на устройство:"
echo "   adb install hermes-visual-node-v2.0-debug.apk"
echo ""
echo "📚 Документация: README_V2.md"
