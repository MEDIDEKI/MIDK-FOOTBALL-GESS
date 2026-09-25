package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String,
    val difficultyScore: Int,
    val team1Name: String,
    val team2Name: String,
    val secretPlayer1Name: String,
    val secretPlayer2Name: String,
    val score1: Int,
    val score2: Int,
    val winnerName: String,
    val totalQuestions: Int,
    val totalRounds: Int
)
