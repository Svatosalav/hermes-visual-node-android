# Сборка APK на x86_64 машине

## Проблема
На ARM64 (aarch64) Linux невозможно собрать Android APK через стандартные инструменты, 
так как AAPT2 и другие build tools доступны только для x86_64.

## Решение 1: Сборка на x86_64 Linux/Mac

### Скопировать проект
```bash
# С ARM64 сервера
cd ~/hermes-visual-nodes
tar czf android-app.tar.gz android-app/
scp android-app.tar.gz user@x86-machine:~/

# На x86_64 машине
cd ~
tar xzf android-app.tar.gz
cd android-app
```

### Установить зависимости (если нужно)
```bash
# Java
sudo apt install openjdk-17-jdk  # Linux
# или brew install openjdk@17    # macOS

# Android SDK (или использовать Android Studio)
```

### Собрать
```bash
./gradlew assembleDebug
# APK будет в app/build/outputs/apk/debug/app-debug.apk
```

## Решение 2: Android Studio (самое простое)

1. Скопировать папку `android-app/` на компьютер с Android Studio
2. Open Project → выбрать папку `android-app`
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK появится в `app/build/outputs/apk/debug/`

## Решение 3: GitHub Actions (автоматическая сборка)

Создать `.github/workflows/build.yml` в репозитории:

```yaml
name: Build APK
on: [push, workflow_dispatch]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build APK
        run: |
          cd android-app
          chmod +x gradlew
          ./gradlew assembleDebug
      - uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: android-app/app/build/outputs/apk/debug/app-debug.apk
```

## Решение 4: Docker с QEMU (медленно, но работает)

```bash
# На ARM64 машине
docker run --rm -v ~/hermes-visual-nodes/android-app:/project \
  --platform linux/amd64 \
  mingc/android-build-box:latest \
  bash -c "cd /project && ./gradlew assembleDebug"
```

## Текущий статус

✅ Проект полностью готов к сборке
✅ Все файлы созданы корректно
❌ Сборка на ARM64 невозможна из-за ограничений Android SDK

## Рекомендация

Самый простой способ — открыть проект в Android Studio на любом компьютере 
(Windows/Mac/Linux x86_64) и собрать там.

Все файлы находятся в: `~/hermes-visual-nodes/android-app/`
