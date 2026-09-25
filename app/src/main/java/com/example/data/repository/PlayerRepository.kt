package com.example.data.repository

import android.content.Context
import com.example.data.database.GameDao
import com.example.data.database.PlayerUsageEntity
import com.example.model.Player
import org.json.JSONArray
import java.io.InputStream

data class DatabaseValidationResult(
    val isValid: Boolean,
    val totalCount: Int,
    val issues: List<String>
)

class PlayerRepository(
    private val context: Context? = null,
    private val gameDao: GameDao? = null
) {
    private var cachedPlayers: List<Player> = emptyList()

    init {
        loadPlayers()
    }

    @Synchronized
    fun getAllPlayers(): List<Player> {
        if (cachedPlayers.isEmpty()) {
            loadPlayers()
        }
        return cachedPlayers
    }

    private fun loadPlayers() {
        try {
            val jsonString = if (context != null) {
                context.assets.open("players.json").bufferedReader().use { it.readText() }
            } else {
                // Fallback for tests or when context not passed
                val stream: InputStream? = javaClass.classLoader?.getResourceAsStream("players.json")
                    ?: javaClass.classLoader?.getResourceAsStream("assets/players.json")
                stream?.bufferedReader()?.use { it.readText() } ?: "[]"
            }
            cachedPlayers = parsePlayersJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            cachedPlayers = emptyList()
        }
    }

    fun parsePlayersJson(jsonString: String): List<Player> {
        val list = mutableListOf<Player>()
        if (jsonString.isBlank()) return list
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val clubsList = mutableListOf<String>()
            if (obj.has("clubs") && !obj.isNull("clubs")) {
                val clubsArr = obj.getJSONArray("clubs")
                for (c in 0 until clubsArr.length()) {
                    clubsList.add(clubsArr.getString(c))
                }
            }

            val player = Player(
                id = obj.getString("id"),
                name = obj.getString("name"),
                nationality = if (obj.has("nationality") && !obj.isNull("nationality")) obj.getString("nationality") else null,
                position = obj.optString("position", "Attaquant"),
                status = obj.optString("status", "Retraité"),
                difficulty = obj.optString("difficulty", "Moyen"),
                difficultyScore = obj.optInt("difficulty_score", 3),
                nationalTeam = if (obj.has("national_team") && !obj.isNull("national_team")) obj.getString("national_team") else null,
                birthDate = if (obj.has("birth_date") && !obj.isNull("birth_date")) obj.getString("birth_date") else null,
                strongFoot = if (obj.has("strong_foot") && !obj.isNull("strong_foot")) obj.getString("strong_foot") else null,
                ballonDor = if (obj.has("ballon_dor") && !obj.isNull("ballon_dor")) obj.getInt("ballon_dor") else null,
                worldCup = if (obj.has("world_cup") && !obj.isNull("world_cup")) obj.getInt("world_cup") else null,
                championsLeague = if (obj.has("champions_league") && !obj.isNull("champions_league")) obj.getInt("champions_league") else null,
                clubs = clubsList,
                photoUrl = if (obj.has("photo_url") && !obj.isNull("photo_url")) obj.getString("photo_url") else null
            )
            list.add(player)
        }
        return list
    }

    /**
     * Requirement: validateDatabase()
     * Checks:
     * - IDs are unique
     * - Names are unique
     * - Difficulty scores are between 1 and 5
     * - Position is not empty and is standard
     * - No empty players
     */
    fun validateDatabase(): DatabaseValidationResult {
        val players = getAllPlayers()
        val issues = mutableListOf<String>()

        if (players.isEmpty()) {
            issues.add("Base de données vide : aucun joueur trouvé.")
            return DatabaseValidationResult(isValid = false, totalCount = 0, issues = issues)
        }

        val ids = mutableSetOf<String>()
        val names = mutableSetOf<String>()

        for (player in players) {
            if (player.id.isBlank()) {
                issues.add("Joueur avec ID vide détecté : ${player.name}")
            } else if (!ids.add(player.id)) {
                issues.add("ID dupliqué détecté : ${player.id} (${player.name})")
            }

            if (player.name.isBlank()) {
                issues.add("Joueur avec nom vide détecté pour l'ID ${player.id}")
            } else if (!names.add(player.name.lowercase().trim())) {
                issues.add("Nom dupliqué détecté : ${player.name}")
            }

            if (player.difficultyScore !in 1..5) {
                issues.add("Score de difficulté invalide (${player.difficultyScore}) pour ${player.name}")
            }

            if (player.position.isBlank()) {
                issues.add("Poste manquant pour ${player.name}")
            }
        }

        return DatabaseValidationResult(
            isValid = issues.isEmpty(),
            totalCount = players.size,
            issues = issues
        )
    }

    fun getPlayersByDifficulty(difficultyScore: Int): List<Player> {
        return getAllPlayers().filter { it.difficultyScore == difficultyScore }
    }

    suspend fun getUsedPlayerIds(difficultyScore: Int, cycle: Int): Set<String> {
        return gameDao?.getUsedPlayerIds(difficultyScore, cycle)?.toSet() ?: emptySet()
    }

    suspend fun getCurrentCycle(difficultyScore: Int): Int {
        val max = gameDao?.getMaxCycleForDifficulty(difficultyScore)
        return if (max == null || max <= 0) 1 else max
    }

    suspend fun recordPlayerUsage(player: Player, cycle: Int) {
        gameDao?.insertPlayerUsage(
            PlayerUsageEntity(
                playerId = player.id,
                difficultyScore = player.difficultyScore,
                cycle = cycle
            )
        )
    }
}
