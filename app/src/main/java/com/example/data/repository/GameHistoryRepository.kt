package com.example.data.repository

import com.example.data.database.GameDao
import com.example.data.database.GameRecordEntity
import com.example.data.database.GameStatsEntity
import com.example.model.GameMode
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class GameHistoryRepository(private val gameDao: GameDao) {

    val allGameRecords: Flow<List<GameRecordEntity>> = gameDao.getAllGameRecords()
    val statsFlow: Flow<GameStatsEntity?> = gameDao.getGameStats()

    suspend fun recordCompletedGame(
        gameMode: GameMode,
        difficultyScore: Int,
        team1Name: String,
        team2Name: String,
        secretPlayer1Name: String,
        secretPlayer2Name: String,
        score1: Int,
        score2: Int,
        winnerName: String,
        totalQuestions: Int,
        totalRounds: Int,
        userWon: Boolean
    ) {
        val record = GameRecordEntity(
            gameMode = gameMode.name,
            difficultyScore = difficultyScore,
            team1Name = team1Name,
            team2Name = team2Name,
            secretPlayer1Name = secretPlayer1Name,
            secretPlayer2Name = secretPlayer2Name,
            score1 = score1,
            score2 = score2,
            winnerName = winnerName,
            totalQuestions = totalQuestions,
            totalRounds = totalRounds
        )
        gameDao.insertGameRecord(record)

        // Update stats
        val currentStats = gameDao.getGameStatsSync() ?: GameStatsEntity()
        val newGamesPlayed = currentStats.gamesPlayed + 1
        val newWins1v1 = if (gameMode == GameMode.ONE_VS_ONE && userWon) currentStats.wins1v1 + 1 else currentStats.wins1v1
        val newWins2v2 = if (gameMode == GameMode.TWO_VS_TWO && userWon) currentStats.wins2v2 + 1 else currentStats.wins2v2
        val newWinsRobot = if (gameMode == GameMode.VS_ROBOT && userWon) currentStats.winsVsRobot + 1 else currentStats.winsVsRobot
        val newLossesRobot = if (gameMode == GameMode.VS_ROBOT && !userWon) currentStats.lossesVsRobot + 1 else currentStats.lossesVsRobot
        val newTotalQuestions = currentStats.totalQuestionsAsked + totalQuestions
        val newGuessed = currentStats.playersGuessed + (if (userWon) 1 else 0)

        val updatedStats = currentStats.copy(
            gamesPlayed = newGamesPlayed,
            wins1v1 = newWins1v1,
            wins2v2 = newWins2v2,
            winsVsRobot = newWinsRobot,
            lossesVsRobot = newLossesRobot,
            totalQuestionsAsked = newTotalQuestions,
            playersGuessed = newGuessed
        )
        gameDao.insertOrUpdateStats(updatedStats)
    }

    suspend fun resetAllStats() {
        gameDao.deleteAllGameRecords()
        gameDao.resetStats()
        gameDao.clearPlayerUsages()
    }

    suspend fun exportDataAsJson(): String {
        val records = gameDao.getAllGameRecordsList()
        val stats = gameDao.getGameStatsSync() ?: GameStatsEntity()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val statsObj = JSONObject()
        statsObj.put("gamesPlayed", stats.gamesPlayed)
        statsObj.put("wins1v1", stats.wins1v1)
        statsObj.put("wins2v2", stats.wins2v2)
        statsObj.put("winsVsRobot", stats.winsVsRobot)
        statsObj.put("lossesVsRobot", stats.lossesVsRobot)
        statsObj.put("totalQuestionsAsked", stats.totalQuestionsAsked)
        statsObj.put("playersGuessed", stats.playersGuessed)
        statsObj.put("cyclesCompleted", stats.cyclesCompleted)
        root.put("stats", statsObj)

        val recordsArr = JSONArray()
        for (r in records) {
            val rObj = JSONObject()
            rObj.put("timestamp", r.timestamp)
            rObj.put("gameMode", r.gameMode)
            rObj.put("difficultyScore", r.difficultyScore)
            rObj.put("team1Name", r.team1Name)
            rObj.put("team2Name", r.team2Name)
            rObj.put("secretPlayer1Name", r.secretPlayer1Name)
            rObj.put("secretPlayer2Name", r.secretPlayer2Name)
            rObj.put("score1", r.score1)
            rObj.put("score2", r.score2)
            rObj.put("winnerName", r.winnerName)
            rObj.put("totalQuestions", r.totalQuestions)
            rObj.put("totalRounds", r.totalRounds)
            recordsArr.put(rObj)
        }
        root.put("records", recordsArr)

        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("stats")) {
                val sObj = root.getJSONObject("stats")
                val stats = GameStatsEntity(
                    id = 1,
                    gamesPlayed = sObj.optInt("gamesPlayed", 0),
                    wins1v1 = sObj.optInt("wins1v1", 0),
                    wins2v2 = sObj.optInt("wins2v2", 0),
                    winsVsRobot = sObj.optInt("winsVsRobot", 0),
                    lossesVsRobot = sObj.optInt("lossesVsRobot", 0),
                    totalQuestionsAsked = sObj.optInt("totalQuestionsAsked", 0),
                    playersGuessed = sObj.optInt("playersGuessed", 0),
                    cyclesCompleted = sObj.optInt("cyclesCompleted", 0)
                )
                gameDao.insertOrUpdateStats(stats)
            }

            if (root.has("records")) {
                val recordsArr = root.getJSONArray("records")
                for (i in 0 until recordsArr.length()) {
                    val rObj = recordsArr.getJSONObject(i)
                    val record = GameRecordEntity(
                        timestamp = rObj.optLong("timestamp", System.currentTimeMillis()),
                        gameMode = rObj.optString("gameMode", "ONE_VS_ONE"),
                        difficultyScore = rObj.optInt("difficultyScore", 3),
                        team1Name = rObj.optString("team1Name", "Joueur 1"),
                        team2Name = rObj.optString("team2Name", "Joueur 2"),
                        secretPlayer1Name = rObj.optString("secretPlayer1Name", ""),
                        secretPlayer2Name = rObj.optString("secretPlayer2Name", ""),
                        score1 = rObj.optInt("score1", 0),
                        score2 = rObj.optInt("score2", 0),
                        winnerName = rObj.optString("winnerName", ""),
                        totalQuestions = rObj.optInt("totalQuestions", 0),
                        totalRounds = rObj.optInt("totalRounds", 1)
                    )
                    gameDao.insertGameRecord(record)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
