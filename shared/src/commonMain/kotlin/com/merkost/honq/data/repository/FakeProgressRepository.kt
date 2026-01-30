package com.merkost.honq.data.repository

import com.merkost.honq.domain.model.MockTestResult
import com.merkost.honq.domain.model.MockTestReviewAnswer
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.UserProgress
import com.merkost.honq.domain.model.WeakQuestion
import com.merkost.honq.domain.repository.MockTestAnswer
import com.merkost.honq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FakeProgressRepository(
    private val questionsProvider: () -> List<Question>
) : ProgressRepository {

    private val mockTestResults = MutableStateFlow(createMockTestResults())
    private val userProgress = MutableStateFlow(createUserProgress())

    private fun createUserProgress() = UserProgress(
        totalQuestions = 315,
        uniqueQuestionsAnswered = 247,
        totalPracticed = 892,
        correctAnswers = 731,
        mockTestsTaken = 8,
        mockTestsPassed = 6,
        lastPracticeDate = Clock.System.now()
    )

    private fun createMockTestResults(): List<MockTestResult> {
        val now = Clock.System.now()
        return listOf(
            MockTestResult(
                id = 1,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 42,
                timeTaken = 28.minutes + 14.seconds,
                completedAt = now
            ),
            MockTestResult(
                id = 2,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 40,
                timeTaken = 31.minutes + 47.seconds,
                completedAt = now - 1.days
            ),
            MockTestResult(
                id = 3,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 38,
                timeTaken = 34.minutes + 22.seconds,
                completedAt = now - 3.days
            ),
            MockTestResult(
                id = 4,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 41,
                timeTaken = 26.minutes + 55.seconds,
                completedAt = now - 5.days
            ),
            MockTestResult(
                id = 5,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 36,
                timeTaken = 38.minutes + 10.seconds,
                completedAt = now - 7.days
            ),
            MockTestResult(
                id = 6,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 43,
                timeTaken = 24.minutes + 30.seconds,
                completedAt = now - 10.days
            ),
            MockTestResult(
                id = 7,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 33,
                timeTaken = 40.minutes + 5.seconds,
                completedAt = now - 14.days
            ),
            MockTestResult(
                id = 8,
                questionSetId = "nsw_car",
                totalQuestions = 45,
                correctAnswers = 30,
                timeTaken = 42.minutes + 18.seconds,
                completedAt = now - 21.days
            )
        )
    }

    override suspend fun recordAnswer(questionId: String, wasCorrect: Boolean) {}

    override suspend fun saveMockTestResult(result: MockTestResult) {}

    override suspend fun saveMockTestResultWithAnswers(
        result: MockTestResult,
        answers: List<MockTestAnswer>
    ) {}

    override fun observeUserProgress(): Flow<UserProgress> = userProgress

    override fun observeMockTestResults(): Flow<List<MockTestResult>> = mockTestResults

    override suspend fun clearAllProgress() {}

    override suspend fun getWeakestQuestions(limit: Int): List<WeakQuestion> {
        val questions = questionsProvider()
        return questions.take(limit.coerceAtMost(5)).mapIndexed { index, question ->
            WeakQuestion(
                question = question,
                wrongCount = 5 - index,
                totalAttempts = 8 - index
            )
        }
    }

    override suspend fun getUnansweredQuestions(limit: Int): List<Question> {
        return questionsProvider().takeLast(limit.coerceAtMost(4))
    }

    override fun observeWeakestQuestionCount(): Flow<Int> = flowOf(5)

    override fun observeUnansweredQuestionCount(): Flow<Int> = flowOf(68)

    override suspend fun getMockTestIncorrectAnswers(mockTestResultId: Long): List<MockTestReviewAnswer> {
        val questions = questionsProvider()
        return questions.take(3).mapIndexed { index, question ->
            val wrongIndex = (question.correctIndex + 1) % question.options.size
            MockTestReviewAnswer(
                question = question,
                selectedAnswerIndex = wrongIndex,
                wasCorrect = false
            )
        } + questions.drop(3).take(5).map { question ->
            MockTestReviewAnswer(
                question = question,
                selectedAnswerIndex = question.correctIndex,
                wasCorrect = true
            )
        }
    }
}
