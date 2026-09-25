package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val id: Int = 1,
    val gamesPlayed: Int = 0,
    val wins1v1: Int = 0,
    val wins2v2: Int = 0,
    val winsVsRobot: Int = 0,
    val lossesVsRobot: Int = 0,
    val totalQuestionsAsked: Int = 0,
    val playersGuessed: Int = 0,
    val cyclesCompleted: Int = 0
) {
    val totalWins: Int
        get() = wins1v1 + wins2v2 + winsVsRobot

    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((totalWins * 100) / gamesPlayed) else 0
}
