# Инструкция по публикации на GitHub

## Шаг 1: Создать репозиторий на GitHub

1. Зайти на https://github.com/new
2. Название: `hermes-visual-node-android`
3. Описание: `Android app for Hermes Visual Nodes - floating button screenshot capture`
4. Public или Private (на выбор)
5. **НЕ** добавлять README, .gitignore, license (уже есть)
6. Создать репозиторий

## Шаг 2: Запушить код

```bash
cd ~/hermes-visual-nodes/android-app

# Добавить remote
git remote add origin https://github.com/Svatosalav/hermes-visual-node-android.git

# Запушить
git push -u origin master
```

## Шаг 3: Автоматическая сборка

После push GitHub Actions автоматически:
1. Запустит workflow `.github/workflows/build-apk.yml`
2. Соберёт APK на x86_64 runner (Ubuntu)
3. Загрузит APK в Artifacts

## Шаг 4: Скачать APK

1. Зайти в репозиторий на GitHub
2. Вкладка **Actions**
3. Выбрать последний workflow run
4. Скачать **hermes-visual-node-debug** из Artifacts
5. Распаковать ZIP, внутри `app-debug.apk`

## Альтернатива: Ручная сборка через GitHub UI

Если не хочешь пушить с сервера:

1. Создать репозиторий на GitHub
2. Загрузить файлы через веб-интерфейс:
   - Перетащить все файлы из `~/hermes-visual-nodes/android-app/`
   - Commit changes
3. Actions запустится автоматически

## Проверка статуса сборки

```bash
# Посмотреть статус последнего workflow
gh run list --repo Svatosalav/hermes-visual-node-android

# Скачать артефакт через CLI
gh run download --repo Svatosalav/hermes-visual-node-android
```

## Создание Release (опционально)

Для создания релиза с APK:

```bash
cd ~/hermes-visual-nodes/android-app

# Создать тег
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions автоматически создаст Release с APK.

## Troubleshooting

### Если Actions не запускается:
1. Settings → Actions → General
2. Включить "Allow all actions and reusable workflows"
3. Включить "Read and write permissions" для GITHUB_TOKEN

### Если сборка падает:
1. Проверить логи в Actions
2. Убедиться что все файлы закоммичены
3. Проверить что gradle wrapper есть в репозитории

## Время сборки

Обычно 3-5 минут на GitHub Actions (x86_64).

---

**Готово!** После push APK будет собран автоматически.
