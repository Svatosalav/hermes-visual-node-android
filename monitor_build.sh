#!/bin/bash

# Скрипт мониторинга GitHub Actions сборки

REPO="Svatosalav/hermes-visual-node-android"

echo "🔍 Мониторинг сборки Hermes Visual Node Android v2.0"
echo "📦 Репозиторий: $REPO"
echo ""

while true; do
    # Получить статус последней сборки
    RESPONSE=$(curl -s "https://api.github.com/repos/$REPO/actions/runs?per_page=1")
    
    STATUS=$(echo "$RESPONSE" | grep -m1 '"status"' | cut -d'"' -f4)
    CONCLUSION=$(echo "$RESPONSE" | grep -m1 '"conclusion"' | cut -d'"' -f4)
    CREATED_AT=$(echo "$RESPONSE" | grep -m1 '"created_at"' | cut -d'"' -f4)
    RUN_URL=$(echo "$RESPONSE" | grep -m1 '"html_url".*actions/runs' | cut -d'"' -f4)
    
    clear
    echo "🔍 Мониторинг сборки Hermes Visual Node Android v2.0"
    echo "📦 Репозиторий: $REPO"
    echo "🔗 URL: $RUN_URL"
    echo ""
    echo "⏰ Запущена: $CREATED_AT"
    echo "📊 Статус: $STATUS"
    
    if [ "$STATUS" = "completed" ]; then
        echo "✅ Завершена: $CONCLUSION"
        echo ""
        
        if [ "$CONCLUSION" = "success" ]; then
            echo "🎉 Сборка успешна!"
            echo ""
            echo "📥 Скачать APK:"
            echo "   $RUN_URL"
            echo ""
            echo "Или через GitHub CLI:"
            echo "   gh run download --repo $REPO"
            break
        else
            echo "❌ Сборка провалилась!"
            echo ""
            echo "📋 Посмотреть логи:"
            echo "   $RUN_URL"
            break
        fi
    else
        echo "⏳ В процессе..."
        echo ""
        echo "Обновление через 30 секунд..."
    fi
    
    sleep 30
done
