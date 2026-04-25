FROM --platform=linux/amd64 eclipse-temurin:17-jdk

# Установка зависимостей
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Рабочая директория
WORKDIR /app

# Копирование проекта
COPY . .

# Gradle сам скачает SDK при первой сборке
RUN chmod +x gradlew

# Переменные окружения для автоматической установки SDK
ENV ANDROID_SDK_ROOT=/app/android-sdk
ENV ANDROID_HOME=/app/android-sdk

# Сборка APK (Gradle автоматически скачает нужные компоненты)
RUN ./gradlew assembleDebug --no-daemon --stacktrace

# Команда по умолчанию
CMD ["cp", "app/build/outputs/apk/debug/app-debug.apk", "/output/"]
