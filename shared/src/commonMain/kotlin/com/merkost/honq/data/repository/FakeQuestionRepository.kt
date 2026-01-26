package com.merkost.honq.data.repository

import com.merkost.honq.core.util.Result
import com.merkost.honq.domain.model.Question
import com.merkost.honq.domain.model.QuestionCategory
import com.merkost.honq.domain.repository.QuestionRepository
import kotlin.time.Instant

class FakeQuestionRepository : QuestionRepository {

    private val sampleQuestions = listOf(
        Question(
            id = "1",
            text = "When approaching a roundabout, you must:",
            imageUrl = null,
            options = listOf(
                "Speed up to enter quickly",
                "Give way to vehicles already in the roundabout",
                "Always stop before entering",
                "Sound your horn to warn other drivers"
            ),
            correctIndex = 1,
            explanation = "You must give way to all vehicles already in the roundabout before entering.",
            category = QuestionCategory.ROAD_RULES
        ),
        Question(
            id = "2",
            text = "What is the maximum speed limit in a school zone during school hours?",
            imageUrl = null,
            options = listOf("50 km/h", "40 km/h", "60 km/h", "30 km/h"),
            correctIndex = 1,
            explanation = "The speed limit in school zones is 40 km/h during school zone hours.",
            category = QuestionCategory.ROAD_SIGNS
        ),
        Question(
            id = "3",
            text = "When can you use a mobile phone while driving?",
            imageUrl = null,
            options = listOf(
                "When stopped at traffic lights",
                "Never while the vehicle is moving or stationary but not parked",
                "When driving under 40 km/h",
                "When using hands-free only"
            ),
            correctIndex = 1,
            explanation = "You cannot use a hand-held mobile phone while driving, even when stopped at traffic lights.",
            category = QuestionCategory.ROAD_RULES
        ),
        Question(
            id = "4",
            text = "What does a yellow traffic light mean?",
            imageUrl = null,
            options = listOf(
                "Speed up to get through",
                "Stop if it is safe to do so",
                "Proceed with caution",
                "Give way to pedestrians"
            ),
            correctIndex = 1,
            explanation = "A yellow light means stop if it is safe to do so.",
            category = QuestionCategory.ROAD_SIGNS
        ),
        Question(
            id = "5",
            text = "What is the blood alcohol limit for learner and P1 drivers?",
            imageUrl = null,
            options = listOf("0.02", "0.05", "Zero (0.00)", "0.01"),
            correctIndex = 2,
            explanation = "Learner and P1 provisional drivers must have a zero blood alcohol concentration.",
            category = QuestionCategory.ALCOHOL_DRUGS
        ),
        Question(
            id = "6",
            text = "When must you indicate before changing lanes?",
            imageUrl = null,
            options = listOf(
                "Only in heavy traffic",
                "At least 5 seconds before changing",
                "Long enough to warn other road users",
                "Only if other vehicles are nearby"
            ),
            correctIndex = 2,
            explanation = "You must signal long enough to give sufficient warning to other road users.",
            category = QuestionCategory.ROAD_RULES
        ),
        Question(
            id = "7",
            text = "What should you do when an emergency vehicle approaches with flashing lights?",
            imageUrl = null,
            options = listOf(
                "Speed up to get out of the way",
                "Stop immediately wherever you are",
                "Move left and stop if safe",
                "Continue driving normally"
            ),
            correctIndex = 2,
            explanation = "You must move to the left side of the road and stop if safe to allow emergency vehicles to pass.",
            category = QuestionCategory.SAFETY
        ),
        Question(
            id = "8",
            text = "What is the minimum following distance in good conditions?",
            imageUrl = null,
            options = listOf("1 second", "2 seconds", "3 seconds", "4 seconds"),
            correctIndex = 2,
            explanation = "You should maintain at least a 3-second gap from the vehicle in front.",
            category = QuestionCategory.SAFETY
        ),
        Question(
            id = "9",
            text = "When parking on a hill facing uphill with a kerb, you should:",
            imageUrl = null,
            options = listOf(
                "Turn wheels towards the kerb",
                "Turn wheels away from the kerb",
                "Keep wheels straight",
                "It doesn't matter"
            ),
            correctIndex = 1,
            explanation = "When facing uphill with a kerb, turn your wheels away from the kerb.",
            category = QuestionCategory.ROAD_RULES
        ),
        Question(
            id = "10",
            text = "At a T-intersection without signs, who gives way?",
            imageUrl = null,
            options = listOf(
                "Vehicle on the continuing road",
                "Vehicle on the terminating road",
                "Whichever arrives first",
                "Vehicle turning left"
            ),
            correctIndex = 1,
            explanation = "At a T-intersection, vehicles on the terminating road must give way.",
            category = QuestionCategory.ROAD_RULES
        ),
        Question(
            id = "11",
            text = "How far must you park from a fire hydrant?",
            imageUrl = null,
            options = listOf("1 metre", "2 metres", "3 metres", "No specific distance"),
            correctIndex = 0,
            explanation = "You must not park within 1 metre of a fire hydrant.",
            category = QuestionCategory.ROAD_RULES
        ),
        Question(
            id = "12",
            text = "What does a broken white line in the centre of the road mean?",
            imageUrl = null,
            options = listOf(
                "No overtaking allowed",
                "You may cross it to overtake if safe",
                "The road is one way",
                "Lane ends ahead"
            ),
            correctIndex = 1,
            explanation = "A broken white line means you may cross it to overtake if it is safe.",
            category = QuestionCategory.ROAD_SIGNS
        ),
        Question(
            id = "13",
            text = "A stop sign requires you to:",
            imageUrl = null,
            options = listOf(
                "Slow down and give way",
                "Come to a complete stop",
                "Stop only if traffic is approaching",
                "Give way to the right"
            ),
            correctIndex = 1,
            explanation = "At a stop sign, you must come to a complete stop.",
            category = QuestionCategory.ROAD_SIGNS
        ),
        Question(
            id = "14",
            text = "When driving in fog, you should:",
            imageUrl = null,
            options = listOf(
                "Use high beam headlights",
                "Use low beam headlights",
                "Use parking lights only",
                "Flash your lights continuously"
            ),
            correctIndex = 1,
            explanation = "Use low beam headlights in fog. High beams reflect off fog and reduce visibility.",
            category = QuestionCategory.SAFETY
        ),
        Question(
            id = "15",
            text = "What should you do if your vehicle starts to skid?",
            imageUrl = null,
            options = listOf(
                "Brake hard immediately",
                "Accelerate to regain control",
                "Ease off the accelerator and steer in the direction you want to go",
                "Turn the steering wheel in the opposite direction"
            ),
            correctIndex = 2,
            explanation = "Ease off the accelerator and steer gently in the direction you want to go.",
            category = QuestionCategory.SAFETY
        )
    )

    override suspend fun getRandomQuestions(count: Int): Result<List<Question>> {
        return Result.Success(sampleQuestions.shuffled().take(count))
    }

    override suspend fun getMockTestQuestions(): Result<List<Question>> {
        return Result.Success(sampleQuestions.shuffled())
    }

    override suspend fun syncQuestions(): Result<Unit> {
        return Result.Success(Unit)
    }

    override fun getLastSyncTime(): Instant? = null
}
