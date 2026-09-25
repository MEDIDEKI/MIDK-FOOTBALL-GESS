package com.example.model

enum class GameMode(
    val titleFr: String,
    val titleEn: String,
    val subtitleFr: String,
    val emoji: String
) {
    ONE_VS_ONE(
        titleFr = "1 contre 1",
        titleEn = "1 vs 1",
        subtitleFr = "Duel face à face sur le même appareil",
        emoji = "⚔️"
    ),
    TWO_VS_TWO(
        titleFr = "2 contre 2",
        titleEn = "2 vs 2",
        subtitleFr = "Équipe A vs Équipe B",
        emoji = "👥"
    ),
    VS_ROBOT(
        titleFr = "Joueur vs Robot",
        titleEn = "Player vs AI",
        subtitleFr = "Affronte une IA avec questionnement stratégique",
        emoji = "🤖"
    )
}
