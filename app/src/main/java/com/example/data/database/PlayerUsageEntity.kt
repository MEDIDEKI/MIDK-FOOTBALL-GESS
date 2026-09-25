package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_usages")
data class PlayerUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: String,
    val difficultyScore: Int,
    val cycle: Int,
    val timestamp: Long = System.currentTimeMillis()
)
