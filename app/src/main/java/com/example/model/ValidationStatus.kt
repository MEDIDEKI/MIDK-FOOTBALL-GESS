package com.example.model

enum class ValidationStatus(
    val labelFr: String,
    val alertMessageFr: String?,
    val badgeEmoji: String
) {
    CORRECT(
        labelFr = "Cohérent",
        alertMessageFr = null,
        badgeEmoji = "✅"
    ),
    LIKELY_INCORRECT(
        labelFr = "Probablement incorrect",
        alertMessageFr = "⚠️ Réponse probablement incorrecte d'après les données vérifiées.",
        badgeEmoji = "⚠️"
    ),
    UNKNOWN(
        labelFr = "Vérification incertaine",
        alertMessageFr = "ℹ️ Vérification incertaine ou question libre.",
        badgeEmoji = "ℹ️"
    )
}
