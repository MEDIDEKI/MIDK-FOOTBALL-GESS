package com.example.engine

data class QuestionCategory(
    val titleFr: String,
    val icon: String,
    val questions: List<String>
)

class QuestionEngine {

    val standardCategories: List<QuestionCategory> = listOf(
        QuestionCategory(
            titleFr = "Poste",
            icon = "⚡",
            questions = listOf(
                "Est-il attaquant ?",
                "Est-il milieu de terrain ?",
                "Est-il défenseur ?",
                "Est-il gardien de but ?"
            )
        ),
        QuestionCategory(
            titleFr = "Nationalité",
            icon = "🌍",
            questions = listOf(
                "Est-il européen ?",
                "Est-il sud-américain ?",
                "Est-il africain ?",
                "Est-il français ?",
                "Est-il brésilien ?",
                "Est-il argentin ?",
                "Est-il espagnol ?",
                "Est-il italien ?",
                "Est-il allemand ?",
                "Est-il anglais ?",
                "Est-il néerlandais ?",
                "Est-il portugais ?"
            )
        ),
        QuestionCategory(
            titleFr = "Statut & Pied",
            icon = "👟",
            questions = listOf(
                "Est-il encore en activité (actif) ?",
                "Est-il retraité ?",
                "Est-il gaucher ?"
            )
        ),
        QuestionCategory(
            titleFr = "Palmarès",
            icon = "🏆",
            questions = listOf(
                "A-t-il remporté la Coupe du Monde ?",
                "A-t-il remporté la Ligue des Champions (LDC) ?",
                "A-t-il remporté au moins un Ballon d'Or ?"
            )
        ),
        QuestionCategory(
            titleFr = "Clubs majeurs",
            icon = "🏟️",
            questions = listOf(
                "A-t-il joué au Real Madrid ?",
                "A-t-il joué au FC Barcelona ?",
                "A-t-il joué au Bayern Munich ?",
                "A-t-il joué à Manchester United ?",
                "A-t-il joué au Paris Saint-Germain (PSG) ?",
                "A-t-il joué à la Juventus ?",
                "A-t-il joué à l'AC Milan ?",
                "A-t-il joué à Chelsea ?",
                "A-t-il joué à Arsenal ?",
                "A-t-il joué à Liverpool ?",
                "A-t-il joué à Manchester City ?"
            )
        )
    )

    fun getAllSuggestedQuestions(): List<String> {
        return standardCategories.flatMap { it.questions }
    }

    /**
     * Checks if a question has essentially already been asked in the game history.
     */
    fun isDuplicateQuestion(existingQuestions: List<String>, newQuestion: String): Boolean {
        val normalizedNew = normalizeQuestion(newQuestion)
        return existingQuestions.any { normalizeQuestion(it) == normalizedNew }
    }

    fun normalizeQuestion(q: String): String {
        return q.lowercase()
            .replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace("'", " ")
            .replace("’", " ")
            .replace("-", " ")
            .trim()
            .replace("\\s+".toRegex(), " ")
    }
}
