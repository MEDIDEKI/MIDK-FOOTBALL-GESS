package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameRecord(record: GameRecordEntity)

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllGameRecords(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    suspend fun getAllGameRecordsList(): List<GameRecordEntity>

    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    fun getGameStats(): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    suspend fun getGameStatsSync(): GameStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: GameStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerUsage(usage: PlayerUsageEntity)

    @Query("SELECT playerId FROM player_usages WHERE difficultyScore = :difficultyScore AND cycle = :cycle")
    suspend fun getUsedPlayerIds(difficultyScore: Int, cycle: Int): List<String>

    @Query("SELECT MAX(cycle) FROM player_usages WHERE difficultyScore = :difficultyScore")
    suspend fun getMaxCycleForDifficulty(difficultyScore: Int): Int?

    @Query("DELETE FROM game_records")
    suspend fun deleteAllGameRecords()

    @Query("DELETE FROM player_usages")
    suspend fun clearPlayerUsages()

    @Query("UPDATE game_stats SET gamesPlayed = 0, wins1v1 = 0, wins2v2 = 0, winsVsRobot = 0, lossesVsRobot = 0, totalQuestionsAsked = 0, playersGuessed = 0, cyclesCompleted = 0 WHERE id = 1")
    suspend fun resetStats()
}
