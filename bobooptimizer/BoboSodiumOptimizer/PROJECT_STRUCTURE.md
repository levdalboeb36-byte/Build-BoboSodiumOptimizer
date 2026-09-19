# BoboSodiumOptimizer - Полная Структура Проекта

## Версия
- Minecraft: 1.21.11
- Fabric Loader: 0.19.5
- Fabric API: 0.119.2+1.21.11
- Sodium: 0.8.14+mc1.21.11 (target)
- Java: 21

================================================================================
СТРУКТУРА ПРОЕКТА
================================================================================

BoboSodiumOptimizer/
├── build.gradle                          # Gradle build configuration
├── settings.gradle                       # Gradle settings
├── gradle.properties                     # Project properties
├── gradlew                               # Gradle wrapper script
├── LICENSE                               # MIT License
├── README.md                             # Documentation
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties     # Gradle wrapper config
└── src/main/
    ├── java/com/bobo/optimizer/
    │   ├── BoboSodiumOptimizer.java           # Main mod entry point
    │   ├── client/
    │   │   └── BoboSodiumOptimizerClient.java # Client entry point
    │   ├── benchmark/
    │   │   └── PerformanceMetrics.java        # FPS/frametime tracking
    │   ├── command/
    │   │   └── OptimizerCommands.java         # /bobooptimizer commands
    │   ├── compatibility/
    │   │   └── SodiumCompatibility.java       # Sodium version check
    │   ├── config/
    │   │   └── BoboOptimizerConfig.java       # Configuration manager
    │   ├── memory/
    │   │   └── AllocationOptimization.java    # Object pooling
    │   ├── mixin/
    │   │   ├── MinecraftClientMixin.java      # Frame timing mixin
    │   │   ├── WorldRendererMixin.java        # Chunk render mixin
    │   │   ├── ChunkBuilderAccessor.java      # Chunk builder access
    │   │   └── sodium/
    │   │       ├── ChunkRenderBackendMixin.java  # Sodium backend (pseudo)
    │   │       ├── ChunkRendererMixin.java       # Sodium renderer (pseudo)
    │   │       └── GraphicsStateMixin.java       # Sodium state (pseudo)
    │   ├── rendering/
    │   │   ├── ChunkOptimization.java         # Chunk rebuild throttling
    │   │   ├── EntityRenderOptimization.java  # Entity culling cache
    │   │   ├── BlockEntityOptimization.java   # Block entity batching
    │   │   ├── RenderSubmissionOptimization.java # Command batching
    │   │   └── VertexBufferPool.java          # Buffer pooling
    │   └── scheduling/
    │       └── ChunkUpdateScheduler.java      # Priority scheduling
    └── resources/
        ├── fabric.mod.json                    # Mod metadata
        ├── bobosodiumoptimizer.mixins.json    # Mixin config (vanilla)
        ├── bobosodiumoptimizer.sodium.mixins.json # Mixin config (Sodium)
        ├── bobosodiumoptimizer.accesswidener  # Access widener
        └── assets/bobosodiumoptimizer/
            └── icon.png                       # Mod icon (placeholder)

================================================================================
ОПИСАНИЕ ФАЙЛОВ
================================================================================

## Конфигурация сборки

### gradle.properties
```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.1
loader_version=0.19.5
fabric_api_version=0.119.2+1.21.11
sodium_version=0.8.14+mc1.21.11
mod_version=1.0.0
java_version=21
```

### build.gradle
- Использует Fabric Loom 1.10-SNAPSHOT
- modCompileOnly для Sodium (не включает в JAR)
- Java 21 target release

### settings.gradle
- Repositories: Fabric Maven, Maven Central
- Root project name: BoboSodiumOptimizer

================================================================================
MOD METADATA (fabric.mod.json)
================================================================================

```json
{
  "id": "bobosodiumoptimizer",
  "version": "${version}",
  "name": "BoboSodiumOptimizer",
  "depends": {
    "fabricloader": ">=0.19.5",
    "minecraft": "~1.21.11",
    "java": ">=21",
    "fabric-api": "*"
  },
  "suggests": {
    "sodium": ">=0.8.14"
  }
}
```

================================================================================
MIXIN КОНФИГУРАЦИЯ
================================================================================

### bobosodiumoptimizer.mixins.json
- Package: com.bobo.optimizer.mixin
- Target: Vanilla Minecraft classes
- Mixins: MinecraftClientMixin, WorldRendererMixin, ChunkBuilderAccessor

### bobosodiumoptimizer.sodium.mixins.json
- Package: com.bobo.optimizer.mixin.sodium
- Required: false (опционально)
- Uses @Pseudo annotation для безопасности
- Target: Sodium internal classes (могут измениться в разных версиях)

================================================================================
ОПТИМИЗАЦИИ
================================================================================

## 1. ChunkOptimization
**Файл:** rendering/ChunkOptimization.java

**Что делает Minecraft/Sodium:**
- Перестраивает меши чанков при изменении блоков
- Sodium использует параллельную сборку чанков
- Каждое перестроение выделяет новые vertex buffers

**Почему медленно на Intel N95/UHD:**
- Только 4 ядра, ограниченный параллелизм
- Интегрированная GPU делит RAM с CPU (медленные buffer uploads)
- Частые аллокации вызывают GC pressure
- Высокое разрешение (2560x1440) = больше видимых чанков

**Что меняет BoboSodiumOptimizer:**
- Throttling перестроений чанков для предотвращения спайков
- Переиспользование vertex buffer аллокаций
- Приоритизация видимых чанков над скрытыми
- Батчинг небольших обновлений

**Визуальный результат:** Идентичный (те же данные, другое время выполнения)

**Тип нагрузки:** CPU (пиковая), Memory (аллокации), Synchronization

**Побочные эффекты:** Небольшая задержка обновлений чанков (обычно незаметна)

--------------------------------------------------------------------------------

## 2. EntityRenderOptimization
**Файл:** rendering/EntityRenderOptimization.java

**Что делает Minecraft/Sodium:**
- Рендерит все сущности в пределах render distance
- Сортирует сущности по текстурам для батч рендеринга
- Каждая сущность проходит индивидуальную обработку модели

**Почему медленно на Intel N95/UHD:**
- Интегрированная GPU чувствительна к количеству draw calls
- CPU bottleneck от итерации и сортировки сущностей
- Высокое разрешение = больше пикселей на сущность

**Что меняет BoboSodiumOptimizer:**
- Кэширование bounding box сущностей
- Улучшенный frustum culling (раннее отсечение невидимых)
- Уменьшение overhead итерации

**Визуальный результат:** Идентичный (все видимые сущности рендерятся)

**Тип нагрузки:** CPU (culling calculations), GPU (draw calls)

**Побочные эффекты:** Минимальные - сущности на краю экрана могут cull'иться иначе

--------------------------------------------------------------------------------

## 3. BlockEntityOptimization
**Файл:** rendering/BlockEntityOptimization.java

**Что делает Minecraft/Sodium:**
- Рендерит block entities (сундуки, печи, знаки) отдельно
- Каждый block entity получает индивидуальный pass рендеринга
- Нет специальной батч обработки

**Почему медленно на Intel N95/UHD:**
- Много изменений состояния между рендерами block entities
- Каждый block entity = отдельный draw call
- Интегрированная GPU особенно чувствительна к state changes

**Что меняет BoboSodiumOptimizer:**
- Группировка block entities по типу для батчинга
- Уменьшение изменений привязки текстур
- Кэширование render данных block entities

**Визуальный результат:** Идентичный (те же block entities в том же порядке)

**Тип нагрузки:** CPU (state changes), GPU (overhead)

**Побочные эффекты:** Прозрачные block entities могут иметь немного другой порядок сортировки

--------------------------------------------------------------------------------

## 4. RenderSubmissionOptimization
**Файл:** rendering/RenderSubmissionOptimization.java

**Что делает Minecraft/Sodium:**
- Отправляет render команды GPU каждый кадр
- Ждёт завершения GPU в точках синхронизации
- Каждый draw call отправляется индивидуально

**Почему медленно на Intel N95/UHD:**
- Интегрированная GPU имеет больший CPU overhead на draw call
- CPU-GPU синхронизация вызывает stalls
- Ограниченная очередь command buffer

**Что меняет BoboSodiumOptimizer:**
- Батчинг render команд где возможно
- Уменьшение ненужных точек синхронизации
- Умное управление command buffer

**Визуальный результат:** Идентичный (те же draw calls, только сбатченные)

**Тип нагрузки:** CPU (submission overhead), GPU (utilization), Sync

**Побочные эффекты:** Frame latency может немного измениться (обычно в лучшую сторону)

--------------------------------------------------------------------------------

## 5. AllocationOptimization
**Файл:** memory/AllocationOptimization.java

**Что делает Minecraft/Sodium:**
- Часто аллоцирует объекты во время рендеринга
- Создаёт новые буферы, массивы, временные объекты каждый кадр
- Полагается на Java GC для очистки

**Почему медленно на Intel N95/UHD:**
- Ограниченная CPU мощность = GC занимает заметное время
- 16GB RAM достаточно, но GC паузы вызывают спайки frametime
- Интегрированная GPU делит RAM, bandwidth важен
- Высокое разрешение требует больше vertex data = больше аллокаций

**Что меняет BoboSodiumOptimizer:**
- Object pooling для часто аллоцируемых типов
- Переиспользование byte buffers
- WeakReference кэши для возможности GC
- Пре-аллокация распространённых структур

**Визуальный результат:** Идентичный (те же данные обрабатываются)

**Тип нагрузки:** Memory (аллокации), CPU (GC time)

**Побочные эффекты:** Немного выше baseline использование памяти (пулы)

--------------------------------------------------------------------------------

## 6. ChunkUpdateScheduler
**Файл:** scheduling/ChunkUpdateScheduler.java

**Что делает Minecraft/Sodium:**
- Планирует перестроения чанков при изменении блоков
- Обрабатывает перестроения в worker потоках
- Нет явного приоритета кроме расстояния

**Почему медленно на Intel N95/UHD:**
- Только 4 ядра = ограниченный параллелизм
- Worker потоки конкурируют за CPU с game logic
- Всплески обновлений чанков вызывают спайки кадров
- Высокое разрешение показывает больше чанков = больше потенциальных обновлений

**Что меняет BoboSodiumOptimizer:**
- Приоритетное планирование
- Распределение перестроений чанков across multiple frames
- Лимит concurrent rebuilds на основе system load
- Откладывание несрочных rebuilds (далёкие чанки, скрытые области)

**Визуальный результат:** Идентичный (все чанки в итоге перестраиваются)

**Тип нагрузки:** CPU (work distribution), Sync (thread contention)

**Побочные эффекты:** Обновления далёких чанков могут быть немного задержаны

================================================================================
КОНФИГУРАЦИЯ
================================================================================

## Профили

### SAFE
- chunkOptimizationEnabled: true
- entityOptimizationEnabled: false
- blockEntityOptimizationEnabled: false
- renderSubmissionOptimizationEnabled: true
- memoryOptimizationEnabled: true
- schedulingOptimizationEnabled: false
- maxChunkRebuildsPerFrame: 2

### BALANCED (default)
- Все оптимизации включены
- maxChunkRebuildsPerFrame: 4
- chunkRebuildPriority: 5

### MAXIMUM
- Все оптимизации включены
- maxChunkRebuildsPerFrame: 8
- chunkRebuildPriority: 7

================================================================================
КОМАНДЫ
================================================================================

/bobooptimizer benchmark [duration]
- Запуск/остановка бенчмарка
- Показывает: FPS, average FPS, 1% low, 0.1% low, frametime, CPU/GPU times

/bobooptimizer profile <safe|balanced|maximum>
- Переключение профиля оптимизации

/bobooptimizer info
- Показать текущие настройки

/bobooptimizer toggle <optimization>
- Включить/выключить конкретную оптимизацию

/bobooptimizer help
- Показать справку по командам

================================================================================
СОВМЕСТИМОСТЬ
================================================================================

## Требуемые зависимости
- fabricloader >= 0.19.5
- minecraft ~1.21.11
- java >= 21
- fabric-api (любая совместимая версия)

## Опциональные зависимости
- sodium >= 0.8.14 (рекомендуется для полной функциональности)

## Примечания по Sodium миксинам
- Используют @Pseudo аннотацию для безопасности
- Не вызовут краш если классы Sodium изменились
- Могут потребовать обновления target class names для будущих версий Sodium
- Для Sodium 0.8.14+mc1.21.11 могут потребоваться корректировки

================================================================================
ИЗВЕСТНЫЕ ОГРАНИЧЕНИЯ
================================================================================

1. **Sodium Internal Classes**: Точные имена классов Sodium могут отличаться
   в версии 0.8.14. Псевдо-миксины безопасно игнорируются если классы не найдены.

2. **Access Widener**: Некоторые поля Minecraft могут требовать дополнительных
   разрешений в bobosodiumoptimizer.accesswidener.

3. **Benchmark Overhead**: Режим бенчмарка добавляет небольшой overhead.
   Рекомендуется отключать для обычной игры.

4. **No Visual Changes**: Мод намеренно не изменяет визуальный результат.
   Это ограничение дизайна, а не баг.

================================================================================
СБОРКА
================================================================================

```bash
# Сборка мода
./gradlew build

# Запуск клиента для тестирования
./gradlew runClient

# Очистка
./gradlew clean

# Результат в build/libs/bobosodiumoptimizer-1.0.0.jar
```

================================================================================
ТЕСТИРОВАНИЕ НА ЦЕЛЕВОЙ СИСТЕМЕ
================================================================================

Рекомендуемый процесс тестирования:

1. Установите Minecraft 1.21.11 с Fabric Loader 0.19.5
2. Установите Fabric API
3. Установите Sodium 0.8.14+mc1.21.11
4. Поместите bobosodiumoptimizer.jar в mods/
5. Запустите игру, проверьте логи на наличие ошибок
6. Используйте команду /bobooptimizer info для проверки статуса
7. Запустите бенчмарк: /bobooptimizer benchmark 60
8. Сравните результаты с включенными/выключенными оптимизациями

================================================================================
ЛИЦЕНЗИЯ
================================================================================

MIT License - свободное использование, модификация и распространение.

================================================================================
