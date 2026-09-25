package com.example.engine

import com.example.model.Player
import com.example.model.RobotLevel
import com.example.model.ValidationStatus
import kotlin.random.Random

sealed class RobotAction {
    data class Ask(val questionText: String) : RobotAction()
    data class Guess(val predictedPlayerName: String) : RobotAction()
}

class RobotEngine(
    val robotSecretPlayer: Player,
    initialCandidates: List<Player>,
    private val validator: AnswerValidator = AnswerValidator(),
    private val random: Random = Random.Default
) {
    private var candidates: List<Player> = initialCandidates.toList()
    private val askedQuestions: MutableSet<String> = mutableSetOf()

    fun getRemainingCandidatesCount(): Int = candidates.size

    fun getCandidates(): List<Player> = candidates

    /**
     * Answers a human player's question about the robot's secret player.
     * Evaluates truthfully against robotSecretPlayer.
     */
    fun answerQuestion(questionText: String): String {
        // Try answering with "OUI"
        val testYes = validator.validateAnswer(robotSecretPlayer, questionText, "OUI")
        if (testYes.status == ValidationStatus.CORRECT) {
            return "OUI"
        }
        val testNo = validator.validateAnswer(robotSecretPlayer, questionText, "NON")
        if (testNo.status == ValidationStatus.CORRECT) {
            return "NON"
        }

        // Fallback for custom questions: check if question mentions any text matching player name or attributes
        val lowerQ = questionText.lowercase()
        if (robotSecretPlayer.nationality?.lowercase()?.let { lowerQ.contains(it) } == true) {
            return "OUI"
        }
        if (lowerQ.contains(robotSecretPlayer.position.lowercase())) {
            return "OUI"
        }

        return "NON"
    }

    /**
     * Decides robot's action on its turn: ask a strategic question or guess!
     */
    fun decideAction(level: RobotLevel): RobotAction {
        if (candidates.size == 1) {
            return RobotAction.Guess(candidates[0].name)
        }

        // Higher levels can make an aggressive guess when candidates are down to 2 or 3
        if (candidates.size in 2..3) {
            val shouldGuess = when (level) {
                RobotLevel.EXPERT -> random.nextFloat() < 0.75f
                RobotLevel.DIFFICILE -> random.nextFloat() < 0.50f
                RobotLevel.NORMAL -> random.nextFloat() < 0.25f
                RobotLevel.FACILE -> random.nextFloat() < 0.10f
            }
            if (shouldGuess) {
                val candidate = candidates.shuffled(random)[0]
                return RobotAction.Guess(candidate.name)
            }
        }

        // Generate pool of potential questions
        val potentialQuestions = listOf(
            "Est-il attaquant ?",
            "Est-il milieu de terrain ?",
            "Est-il défenseur ?",
            "Est-il gardien de but ?",
            "Est-il européen ?",
            "Est-il sud-américain ?",
            "Est-il africain ?",
            "Est-il brésilien ?",
            "Est-il argentin ?",
            "Est-il français ?",
            "Est-il espagnol ?",
            "Est-il italien ?",
            "Est-il allemand ?",
            "Est-il anglais ?",
            "Est-il encore en activité ?",
            "Est-il retraité ?",
            "A-t-il remporté la Coupe du Monde ?",
            "A-t-il remporté la Ligue des Champions (LDC) ?",
            "A-t-il remporté au moins un Ballon d'Or ?"
        ).filter { it !in askedQuestions }

        if (potentialQuestions.isEmpty()) {
            val pick = candidates.shuffled(random)[0]
            return RobotAction.Guess(pick.name)
        }

        val bestQuestion = when (level) {
            RobotLevel.FACILE -> potentialQuestions.shuffled(random)[0]
            RobotLevel.NORMAL -> {
                // Pick question that splits roughly between 20% and 80%
                potentialQuestions.shuffled(random).maxByOrNull { q -> scoreQuestionSplit(q) } ?: potentialQuestions[0]
            }
            RobotLevel.DIFFICILE, RobotLevel.EXPERT -> {
                // Optimal binary split (closest to 50% split)
                potentialQuestions.maxByOrNull { q -> scoreQuestionSplit(q) } ?: potentialQuestions[0]
            }
        }

        askedQuestions.add(bestQuestion)
        return RobotAction.Ask(bestQuestion)
    }

    /**
     * Scores how close a question is to splitting the candidate pool in half (ideal entropy = 1.0).
     */
    private fun scoreQuestionSplit(question: String): Double {
        var yesCount = 0
        for (candidate in candidates) {
            val res = validator.validateAnswer(candidate, question, "OUI")
            if (res.status == ValidationStatus.CORRECT) {
                yesCount++
            }
        }
        val ratio = yesCount.toDouble() / candidates.size.coerceAtLeast(1)
        // Highest score is when ratio is closest to 0.5 (ideal binary split)
        return 1.0 - kotlin.math.abs(0.5 - ratio) * 2.0
    }

    /**
     * Updates the robot's candidate list based on human's answer.
     */
    fun processAnswer(questionText: String, answer: String) {
        val remaining = candidates.filter { candidate ->
            val result = validator.validateAnswer(candidate, questionText, answer)
            // Keep if answer is consistent or if test is unknown/ambiguous
            result.status != ValidationStatus.LIKELY_INCORRECT
        }

        // Avoid empty candidate set if human answered unexpectedly
        if (remaining.isNotEmpty()) {
            candidates = remaining
        }
    }
}
