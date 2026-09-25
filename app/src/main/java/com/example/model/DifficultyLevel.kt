package com.example.model

enum class DifficultyLevel(
    val score: Int,
    val titleFr: String,
    val titleEn: String,
    val emoji: String,
    val colorHex: Long,
    val descriptionFr: String
) {
    TRES_FACILE(
        score = 1,
        titleFr = "Très facile",
        titleEn = "Very Easy",
        emoji = "🟢",
        colorHex = 0xFF10B981,
        descriptionFr = "Les plus grandes superstars et légendes mondiales incontournables (ex: Messi, Ronaldo, Zidane)"
    ),
    FACILE(
        score = 2,
        titleFr = "Facile",
        titleEn = "Easy",
        emoji = "🔵",
        colorHex = 0xFF3B82F6,
        descriptionFr = "Joueurs très connus du grand public international (ex: Kane, Drogba, Pirlo, Buffon)"
    ),
    MOYEN(
        score = 3,
        titleFr = "Moyen",
        titleEn = "Medium",
        emoji = "🟡",
        colorHex = 0xFFF59E0B,
        descriptionFr = "Joueurs renommés avec belle carrière en club ou sélection (ex: Kroos, Forlán, Sneijder)"
    ),
    DIFFICILE(
        score = 4,
        titleFr = "Difficile",
        titleEn = "Hard",
        emoji = "🟠",
        colorHex = 0xFFF97316,
        descriptionFr = "Grands talents, piliers d'équipes prestigieuses ou époques ciblées (ex: Vieira, Puyol, Ziyech)"
    ),
    EXPERT(
        score = 5,
        titleFr = "Expert",
        titleEn = "Expert",
        emoji = "🔴",
        colorHex = 0xFFEF4444,
        descriptionFr = "Légendes historiques anciennes et spécialistes du football (ex: Di Stéfano, Puskás, Madjer)"
    );

    companion object {
        fun fromScore(score: Int): DifficultyLevel {
            return entries.find { it.score == score } ?: MOYEN
        }

        fun fromString(str: String): DifficultyLevel {
            val normalized = str.lowercase().trim()
            return when {
                normalized.contains("très facile") || normalized.contains("tres facile") || normalized.contains("very easy") -> TRES_FACILE
                normalized.contains("facile") || normalized.contains("easy") -> FACILE
                normalized.contains("moyen") || normalized.contains("medium") -> MOYEN
                normalized.contains("difficile") || normalized.contains("hard") -> DIFFICILE
                normalized.contains("expert") -> EXPERT
                else -> MOYEN
            }
        }
    }
}
