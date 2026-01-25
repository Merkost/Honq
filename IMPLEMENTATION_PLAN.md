# Honq MVP Implementation Plan

> NSW Driving Knowledge Test preparation app built with Compose Multiplatform

---

## Current State

The project is a **bootstrap KMP template** with:
- Basic Compose Multiplatform setup (Android + iOS)
- Single `App.kt` with placeholder UI
- No domain models, screens, navigation, data layer, or design system

---

## Architecture Overview

### Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Screens   │  │  ViewModels │  │  UI State / Events  │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                       Domain Layer                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Models    │  │  Use Cases  │  │ Repository Interfaces│  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                        Data Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Remote    │  │    Local    │  │ Repository Impls    │  │
│  │  (Supabase) │  │ (SQLDelight)│  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                        Core Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │    Utils    │  │  Extensions │  │   Common Types      │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Layer Rules

| Layer | Can Depend On | Cannot Depend On |
|-------|---------------|------------------|
| Presentation | Domain, Core | Data |
| Domain | Core | Presentation, Data |
| Data | Domain, Core | Presentation |
| Core | Nothing | Any other layer |

---

## MVP Scope

### Core Features
| Feature | Description |
|---------|-------------|
| **Home** | Progress overview, entry points to practice/test |
| **Practice Mode** | Endless question cards with instant feedback |
| **Mock Test** | 45 questions, timed, mirrors real DKT format |
| **Results** | Pass/fail, score breakdown, questions to review |

### Technical Decisions
| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Data Source** | Supabase backend | Enables question updates without app releases |
| **Local Storage** | SQLDelight | KMP-compatible, offline-first support |
| **Offline Mode** | Full offline-first | Questions cached locally after sync |
| **DI** | Koin (modular) | Separated by layer for testability |

### Out of Scope (Post-MVP)
- Spaced repetition algorithm
- Confidence tagging
- Category filtering
- Weakness radar visualization
- Multiple Australian states

---

## Package Structure

```
shared/src/commonMain/kotlin/com/merkost/honq/
├── core/
│   ├── util/
│   │   ├── Result.kt
│   │   ├── CoroutineDispatchers.kt
│   │   └── DateTimeExt.kt
│   └── di/
│       └── CoreModule.kt
│
├── domain/
│   ├── model/
│   │   ├── Question.kt
│   │   ├── QuestionCategory.kt
│   │   ├── QuizSession.kt
│   │   ├── MockTestResult.kt
│   │   └── UserProgress.kt
│   ├── repository/
│   │   ├── QuestionRepository.kt
│   │   └── ProgressRepository.kt
│   ├── usecase/
│   │   ├── GetRandomQuestionsUseCase.kt
│   │   ├── GetMockTestQuestionsUseCase.kt
│   │   ├── RecordAnswerUseCase.kt
│   │   ├── SaveMockTestResultUseCase.kt
│   │   ├── GetUserProgressUseCase.kt
│   │   └── SyncQuestionsUseCase.kt
│   └── di/
│       └── DomainModule.kt
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── HonqDatabase.sq
│   │   │   └── DatabaseDriverFactory.kt
│   │   ├── datasource/
│   │   │   └── QuestionLocalDataSource.kt
│   │   └── mapper/
│   │       └── QuestionEntityMapper.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── SupabaseClient.kt
│   │   │   └── QuestionApi.kt
│   │   ├── dto/
│   │   │   └── QuestionDto.kt
│   │   └── mapper/
│   │       └── QuestionDtoMapper.kt
│   ├── repository/
│   │   ├── QuestionRepositoryImpl.kt
│   │   └── ProgressRepositoryImpl.kt
│   └── di/
│       └── DataModule.kt
│
└── presentation/
    ├── theme/
    │   ├── Color.kt
    │   ├── Type.kt
    │   ├── Shape.kt
    │   ├── Spacing.kt
    │   └── Theme.kt
    ├── components/
    │   ├── base/
    │   │   ├── HonqButton.kt
    │   │   ├── HonqCard.kt
    │   │   ├── HonqProgressBar.kt
    │   │   └── HonqScaffold.kt
    │   └── question/
    │       ├── QuestionCard.kt
    │       ├── AnswerOption.kt
    │       └── ExplanationCard.kt
    ├── screens/
    │   ├── home/
    │   │   ├── HomeScreen.kt
    │   │   ├── HomeViewModel.kt
    │   │   └── HomeUiState.kt
    │   ├── practice/
    │   │   ├── PracticeScreen.kt
    │   │   ├── PracticeViewModel.kt
    │   │   └── PracticeUiState.kt
    │   ├── mocktest/
    │   │   ├── MockTestScreen.kt
    │   │   ├── MockTestViewModel.kt
    │   │   └── MockTestUiState.kt
    │   └── results/
    │       ├── ResultsScreen.kt
    │       ├── ResultsViewModel.kt
    │       └── ResultsUiState.kt
    ├── navigation/
    │   ├── Screen.kt
    │   └── NavGraph.kt
    ├── di/
    │   └── PresentationModule.kt
    └── App.kt
```

---

## Implementation Phases

### Phase 1: Foundation Setup

#### 1.1 Add Dependencies
**File:** `gradle/libs.versions.toml`

```toml
[versions]
navigation = "2.8.0"
koin = "4.0.0"
sqldelight = "2.0.2"
ktor = "3.0.0"
kotlinx-serialization = "1.7.3"
kotlinx-datetime = "0.6.1"
coil = "3.0.4"
supabase = "3.0.2"

[libraries]
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

### Phase 2: Core Layer

#### 2.1 Result Wrapper
```kotlin
// core/util/Result.kt
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}
```

#### 2.2 Coroutine Dispatchers
```kotlin
// core/util/CoroutineDispatchers.kt
interface AppDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class AppDispatchersImpl : AppDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
```

#### 2.3 Core DI Module
```kotlin
// core/di/CoreModule.kt
val coreModule = module {
    single<AppDispatchers> { AppDispatchersImpl() }
    single { Json { ignoreUnknownKeys = true } }
}
```

---

### Phase 3: Domain Layer

#### 3.1 Domain Models
```kotlin
// domain/model/Question.kt
data class Question(
    val id: String,
    val text: String,
    val imageUrl: String?,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: QuestionCategory
)

// domain/model/QuestionCategory.kt
enum class QuestionCategory {
    ROAD_RULES,
    ROAD_SIGNS,
    SAFETY,
    ALCOHOL_DRUGS
}

// domain/model/QuizSession.kt
data class QuizSession(
    val questions: List<Question>,
    val answers: Map<String, Int> = emptyMap(),
    val currentIndex: Int = 0,
    val startTime: Instant? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isComplete: Boolean get() = currentIndex >= questions.size
    val progress: Float get() = if (questions.isEmpty()) 0f else currentIndex.toFloat() / questions.size
}

// domain/model/MockTestResult.kt
data class MockTestResult(
    val id: Long = 0,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeTaken: Duration,
    val completedAt: Instant
) {
    val passed: Boolean get() = correctAnswers >= (totalQuestions * 0.9).toInt()
    val scorePercentage: Int get() = ((correctAnswers.toFloat() / totalQuestions) * 100).toInt()
}

// domain/model/UserProgress.kt
data class UserProgress(
    val totalPracticed: Int,
    val correctAnswers: Int,
    val mockTestsTaken: Int,
    val mockTestsPassed: Int,
    val lastPracticeDate: Instant?
) {
    val practiceAccuracy: Float get() = if (totalPracticed == 0) 0f else correctAnswers.toFloat() / totalPracticed
    val mockTestPassRate: Float get() = if (mockTestsTaken == 0) 0f else mockTestsPassed.toFloat() / mockTestsTaken
}
```

#### 3.2 Repository Interfaces
```kotlin
// domain/repository/QuestionRepository.kt
interface QuestionRepository {
    suspend fun getRandomQuestions(count: Int): Result<List<Question>>
    suspend fun getMockTestQuestions(): Result<List<Question>>
    suspend fun syncQuestions(): Result<Unit>
    fun getLastSyncTime(): Instant?
}

// domain/repository/ProgressRepository.kt
interface ProgressRepository {
    suspend fun recordAnswer(questionId: String, wasCorrect: Boolean)
    suspend fun saveMockTestResult(result: MockTestResult)
    fun observeUserProgress(): Flow<UserProgress>
    fun observeMockTestResults(): Flow<List<MockTestResult>>
}
```

#### 3.3 Use Cases
```kotlin
// domain/usecase/GetRandomQuestionsUseCase.kt
class GetRandomQuestionsUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(count: Int): Result<List<Question>> =
        repository.getRandomQuestions(count)
}

// domain/usecase/GetMockTestQuestionsUseCase.kt
class GetMockTestQuestionsUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<List<Question>> =
        repository.getMockTestQuestions()
}

// domain/usecase/RecordAnswerUseCase.kt
class RecordAnswerUseCase(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(questionId: String, wasCorrect: Boolean) =
        repository.recordAnswer(questionId, wasCorrect)
}

// domain/usecase/SaveMockTestResultUseCase.kt
class SaveMockTestResultUseCase(
    private val repository: ProgressRepository
) {
    suspend operator fun invoke(result: MockTestResult) =
        repository.saveMockTestResult(result)
}

// domain/usecase/GetUserProgressUseCase.kt
class GetUserProgressUseCase(
    private val repository: ProgressRepository
) {
    operator fun invoke(): Flow<UserProgress> =
        repository.observeUserProgress()
}

// domain/usecase/SyncQuestionsUseCase.kt
class SyncQuestionsUseCase(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        repository.syncQuestions()
}
```

#### 3.4 Domain DI Module
```kotlin
// domain/di/DomainModule.kt
val domainModule = module {
    factory { GetRandomQuestionsUseCase(get()) }
    factory { GetMockTestQuestionsUseCase(get()) }
    factory { RecordAnswerUseCase(get()) }
    factory { SaveMockTestResultUseCase(get()) }
    factory { GetUserProgressUseCase(get()) }
    factory { SyncQuestionsUseCase(get()) }
}
```

---

### Phase 4: Data Layer

#### 4.1 DTOs
```kotlin
// data/remote/dto/QuestionDto.kt
@Serializable
data class QuestionDto(
    val id: String,
    val text: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val options: List<String>,
    @SerialName("correct_index") val correctIndex: Int,
    val explanation: String,
    val category: String,
    @SerialName("updated_at") val updatedAt: String
)
```

#### 4.2 Mappers
```kotlin
// data/remote/mapper/QuestionDtoMapper.kt
fun QuestionDto.toDomain(): Question = Question(
    id = id,
    text = text,
    imageUrl = imageUrl,
    options = options,
    correctIndex = correctIndex,
    explanation = explanation,
    category = QuestionCategory.valueOf(category.uppercase())
)

// data/local/mapper/QuestionEntityMapper.kt
fun QuestionEntity.toDomain(json: Json): Question = Question(
    id = id,
    text = text,
    imageUrl = imageUrl,
    options = json.decodeFromString(options),
    correctIndex = correctIndex.toInt(),
    explanation = explanation,
    category = QuestionCategory.valueOf(category.uppercase())
)

fun Question.toEntity(json: Json): QuestionEntity = QuestionEntity(
    id = id,
    text = text,
    imageUrl = imageUrl,
    options = json.encodeToString(options),
    correctIndex = correctIndex.toLong(),
    explanation = explanation,
    category = category.name.lowercase(),
    updatedAt = Clock.System.now().toString()
)
```

#### 4.3 Local Data Source
```kotlin
// data/local/datasource/QuestionLocalDataSource.kt
class QuestionLocalDataSource(
    private val database: HonqDatabase,
    private val json: Json
) {
    fun getRandomQuestions(count: Int): List<Question> =
        database.questionQueries
            .getRandomQuestions(count.toLong())
            .executeAsList()
            .map { it.toDomain(json) }

    fun getMockTestQuestions(): List<Question> =
        database.questionQueries
            .getMockTestQuestions()
            .executeAsList()
            .map { it.toDomain(json) }

    fun insertQuestions(questions: List<Question>) {
        database.questionQueries.transaction {
            questions.forEach { question ->
                val entity = question.toEntity(json)
                database.questionQueries.insertQuestion(
                    id = entity.id,
                    text = entity.text,
                    imageUrl = entity.imageUrl,
                    options = entity.options,
                    correctIndex = entity.correctIndex,
                    explanation = entity.explanation,
                    category = entity.category,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    fun recordAnswer(questionId: String, wasCorrect: Boolean) {
        database.answerQueries.insertAnswer(
            questionId = questionId,
            wasCorrect = if (wasCorrect) 1L else 0L,
            answeredAt = Clock.System.now().toString()
        )
    }

    fun getAnswerStats(): AnswerStats =
        database.answerQueries.getAnswerStats().executeAsOne()

    fun saveMockTestResult(result: MockTestResult) {
        database.mockTestQueries.insertMockTestResult(
            totalQuestions = result.totalQuestions.toLong(),
            correctAnswers = result.correctAnswers.toLong(),
            timeTakenSeconds = result.timeTaken.inWholeSeconds,
            passed = if (result.passed) 1L else 0L,
            completedAt = result.completedAt.toString()
        )
    }

    fun observeMockTestResults(): Flow<List<MockTestResult>> =
        database.mockTestQueries
            .getMockTestResults()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities -> entities.map { it.toDomain() } }
}
```

#### 4.4 Remote API
```kotlin
// data/remote/api/QuestionApi.kt
class QuestionApi(
    private val client: SupabaseClient
) {
    suspend fun fetchAllQuestions(): List<QuestionDto> =
        client.postgrest["questions"]
            .select()
            .decodeList()
}
```

#### 4.5 Repository Implementations
```kotlin
// data/repository/QuestionRepositoryImpl.kt
class QuestionRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val questionApi: QuestionApi,
    private val dispatchers: AppDispatchers
) : QuestionRepository {

    override suspend fun getRandomQuestions(count: Int): Result<List<Question>> =
        withContext(dispatchers.io) {
            runCatching { localDataSource.getRandomQuestions(count) }
                .fold(
                    onSuccess = { Result.Success(it) },
                    onFailure = { Result.Error(it) }
                )
        }

    override suspend fun getMockTestQuestions(): Result<List<Question>> =
        withContext(dispatchers.io) {
            runCatching { localDataSource.getMockTestQuestions() }
                .fold(
                    onSuccess = { Result.Success(it) },
                    onFailure = { Result.Error(it) }
                )
        }

    override suspend fun syncQuestions(): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                val remoteQuestions = questionApi.fetchAllQuestions()
                localDataSource.insertQuestions(remoteQuestions.map { it.toDomain() })
            }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(it) }
            )
        }

    override fun getLastSyncTime(): Instant? = null
}

// data/repository/ProgressRepositoryImpl.kt
class ProgressRepositoryImpl(
    private val localDataSource: QuestionLocalDataSource,
    private val dispatchers: AppDispatchers
) : ProgressRepository {

    override suspend fun recordAnswer(questionId: String, wasCorrect: Boolean) =
        withContext(dispatchers.io) {
            localDataSource.recordAnswer(questionId, wasCorrect)
        }

    override suspend fun saveMockTestResult(result: MockTestResult) =
        withContext(dispatchers.io) {
            localDataSource.saveMockTestResult(result)
        }

    override fun observeUserProgress(): Flow<UserProgress> =
        flow {
            val stats = localDataSource.getAnswerStats()
            val mockTests = localDataSource.observeMockTestResults().first()
            emit(
                UserProgress(
                    totalPracticed = stats.total?.toInt() ?: 0,
                    correctAnswers = stats.correct?.toInt() ?: 0,
                    mockTestsTaken = mockTests.size,
                    mockTestsPassed = mockTests.count { it.passed },
                    lastPracticeDate = null
                )
            )
        }.flowOn(dispatchers.io)

    override fun observeMockTestResults(): Flow<List<MockTestResult>> =
        localDataSource.observeMockTestResults()
}
```

#### 4.6 Data DI Module
```kotlin
// data/di/DataModule.kt
val dataModule = module {
    single { createSupabaseClient() }
    single { createDatabase(get()) }
    single { QuestionApi(get()) }
    single { QuestionLocalDataSource(get(), get()) }
    single<QuestionRepository> { QuestionRepositoryImpl(get(), get(), get()) }
    single<ProgressRepository> { ProgressRepositoryImpl(get(), get()) }
}
```

---

### Phase 5: Presentation Layer

#### 5.1 Design System

```kotlin
// presentation/theme/Color.kt
object HonqColors {
    val Background = Color(0xFF1C1C1E)
    val Surface = Color(0xFF2C2C2E)
    val SurfaceVariant = Color(0xFF3A3A3C)
    val Border = Color(0xFF3A3A3C)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF8E8E93)
    val TextMuted = Color(0xFF636366)
    val Amber = Color(0xFFFFD60A)
    val AmberDark = Color(0xFFCC9900)
    val Correct = Color(0xFF30D158)
    val CorrectSurface = Color(0xFF1A3D2A)
    val Incorrect = Color(0xFFFF453A)
    val IncorrectSurface = Color(0xFF3D1A1A)
}

// presentation/theme/Spacing.kt
object HonqSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object HonqSizing {
    val minTapTarget = 48.dp
    val buttonHeight = 56.dp
    val cardPadding = 24.dp
    val screenPadding = 24.dp
    val cornerRadius = 12.dp
    val progressBarHeight = 4.dp
}
```

#### 5.2 Reusable Base Components

```kotlin
// presentation/components/base/HonqButton.kt
enum class HonqButtonVariant { Primary, Secondary, Text }

@Composable
fun HonqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HonqButtonVariant = HonqButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false
)

// presentation/components/base/HonqCard.kt
@Composable
fun HonqCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)

// presentation/components/base/HonqProgressBar.kt
@Composable
fun HonqProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
)

// presentation/components/base/HonqScaffold.kt
@Composable
fun HonqScaffold(
    title: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
)
```

#### 5.3 Question Components

```kotlin
// presentation/components/question/AnswerOption.kt
enum class AnswerOptionState { Default, Selected, Correct, Incorrect, Disabled }

@Composable
fun AnswerOption(
    text: String,
    index: Int,
    state: AnswerOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// presentation/components/question/QuestionCard.kt
@Composable
fun QuestionCard(
    question: Question,
    selectedAnswer: Int?,
    answerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
)

// presentation/components/question/ExplanationCard.kt
@Composable
fun ExplanationCard(
    explanation: String,
    isCorrect: Boolean,
    modifier: Modifier = Modifier
)
```

#### 5.4 Screen UI States

```kotlin
// presentation/screens/home/HomeUiState.kt
data class HomeUiState(
    val progress: UserProgress = UserProgress(0, 0, 0, 0, null),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

// presentation/screens/practice/PracticeUiState.kt
data class PracticeUiState(
    val currentQuestion: Question? = null,
    val selectedAnswer: Int? = null,
    val answerRevealed: Boolean = false,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface PracticeEvent {
    data class AnswerSelected(val index: Int) : PracticeEvent
    data object NextQuestion : PracticeEvent
    data object Exit : PracticeEvent
}

// presentation/screens/mocktest/MockTestUiState.kt
data class MockTestUiState(
    val session: QuizSession = QuizSession(emptyList()),
    val selectedAnswer: Int? = null,
    val timeRemaining: Duration = 30.minutes,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface MockTestEvent {
    data class AnswerSelected(val index: Int) : MockTestEvent
    data object NextQuestion : MockTestEvent
    data object PreviousQuestion : MockTestEvent
    data object SubmitTest : MockTestEvent
}

// presentation/screens/results/ResultsUiState.kt
data class ResultsUiState(
    val result: MockTestResult? = null,
    val incorrectQuestions: List<Question> = emptyList()
)
```

#### 5.5 ViewModels

```kotlin
// presentation/screens/home/HomeViewModel.kt
class HomeViewModel(
    private val getUserProgress: GetUserProgressUseCase,
    private val syncQuestions: SyncQuestionsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeProgress()
        syncInBackground()
    }

    private fun observeProgress() {
        viewModelScope.launch {
            getUserProgress().collect { progress ->
                _uiState.update { it.copy(progress = progress, isLoading = false) }
            }
        }
    }

    private fun syncInBackground() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            syncQuestions()
                .onSuccess { _uiState.update { it.copy(isSyncing = false, syncError = null) } }
                .onError { e -> _uiState.update { it.copy(isSyncing = false, syncError = e.message) } }
        }
    }
}

// presentation/screens/practice/PracticeViewModel.kt
class PracticeViewModel(
    private val getRandomQuestions: GetRandomQuestionsUseCase,
    private val recordAnswer: RecordAnswerUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init { loadNextQuestion() }

    fun onEvent(event: PracticeEvent) {
        when (event) {
            is PracticeEvent.AnswerSelected -> handleAnswerSelected(event.index)
            PracticeEvent.NextQuestion -> loadNextQuestion()
            PracticeEvent.Exit -> Unit
        }
    }

    private fun handleAnswerSelected(index: Int) {
        val question = _uiState.value.currentQuestion ?: return
        val isCorrect = index == question.correctIndex

        viewModelScope.launch {
            recordAnswer(question.id, isCorrect)
        }

        _uiState.update {
            it.copy(
                selectedAnswer = index,
                answerRevealed = true,
                correctAnswers = if (isCorrect) it.correctAnswers + 1 else it.correctAnswers
            )
        }
    }

    private fun loadNextQuestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedAnswer = null, answerRevealed = false) }
            getRandomQuestions(1)
                .onSuccess { questions ->
                    _uiState.update {
                        it.copy(
                            currentQuestion = questions.firstOrNull(),
                            isLoading = false,
                            questionsAnswered = it.questionsAnswered + 1
                        )
                    }
                }
                .onError { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
```

#### 5.6 Navigation

```kotlin
// presentation/navigation/Screen.kt
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Practice : Screen("practice")
    data object MockTest : Screen("mocktest")
    data object Results : Screen("results/{score}/{total}") {
        fun createRoute(score: Int, total: Int) = "results/$score/$total"
    }
}

// presentation/navigation/NavGraph.kt
@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPractice = { navController.navigate(Screen.Practice.route) },
                onNavigateToMockTest = { navController.navigate(Screen.MockTest.route) }
            )
        }
        composable(Screen.Practice.route) {
            PracticeScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.MockTest.route) {
            MockTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResults = { score, total ->
                    navController.navigate(Screen.Results.createRoute(score, total))
                }
            )
        }
        composable(Screen.Results.route) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            ResultsScreen(
                score = score,
                total = total,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = { navController.navigate(Screen.MockTest.route) }
            )
        }
    }
}
```

#### 5.7 Presentation DI Module

```kotlin
// presentation/di/PresentationModule.kt
val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::PracticeViewModel)
    viewModelOf(::MockTestViewModel)
    viewModelOf(::ResultsViewModel)
}
```

---

### Phase 6: App Entry Point

```kotlin
// presentation/App.kt
@Composable
fun App() {
    HonqTheme {
        NavGraph()
    }
}

// Initialize Koin
fun initKoin() = startKoin {
    modules(
        coreModule,
        dataModule,
        domainModule,
        presentationModule
    )
}
```

---

## Koin Module Summary

| Module | Location | Provides |
|--------|----------|----------|
| `coreModule` | `core/di/CoreModule.kt` | Dispatchers, Json |
| `dataModule` | `data/di/DataModule.kt` | Database, API, DataSources, Repositories |
| `domainModule` | `domain/di/DomainModule.kt` | Use Cases |
| `presentationModule` | `presentation/di/PresentationModule.kt` | ViewModels |

---

## File Summary

| Layer | Files |
|-------|-------|
| Core | 3 files |
| Domain | 12 files |
| Data | 10 files |
| Presentation | 25+ files |
| **Total** | **~50 files** |

---

## Verification

### Build Verification
```bash
./gradlew :shared:build
./gradlew :androidApp:assembleDebug
```

### Architecture Verification
- [ ] Domain layer has no Android/iOS imports
- [ ] Domain layer has no data layer imports
- [ ] Presentation layer has no data layer imports
- [ ] All repositories accessed via interfaces
- [ ] All ViewModels use Use Cases, not Repositories directly

### Manual Testing Checklist
1. [ ] App launches to Home screen
2. [ ] Home shows progress stats (initially zero)
3. [ ] Sync happens in background without blocking UI
4. [ ] "Start Practice" navigates to Practice screen
5. [ ] Questions display with 4 answer options
6. [ ] Selecting answer shows instant feedback (green/red)
7. [ ] Explanation displays after answering
8. [ ] "Next Question" loads new question
9. [ ] "Take Mock Test" starts 45-question timed test
10. [ ] Timer counts down from 30 minutes
11. [ ] Completing test shows Results screen
12. [ ] Results show pass/fail, score, incorrect questions
13. [ ] Navigation back to Home works
14. [ ] Offline mode works when no network available
