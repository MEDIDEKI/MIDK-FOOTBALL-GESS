package com.example.model

enum class RobotLevel(
    val titleFr: String,
    val descriptionFr: String
) {
    FACILE(
        titleFr = "Facile",
        descriptionFr = "Pose des questions simples et met du temps à deviner"
    ),
    NORMAL(
        titleFr = "Normal",
        descriptionFr = "Pose des questions équilibrées sur le poste et la nationalité"
    ),
    DIFFICILE(
        titleFr = "Difficile",
        descriptionFr = "Optimise ses questions pour éliminer rapidement les candidats"
    ),
    EXPERT(
        titleFr = "Expert",
        descriptionFr = "Stratégie de découpage binaire optimale et déduction chirurgicale"
    )
}
