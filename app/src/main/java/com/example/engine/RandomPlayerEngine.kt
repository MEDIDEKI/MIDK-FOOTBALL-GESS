package com.example.engine

import com.example.data.repository.PlayerRepository
import com.example.model.DifficultyLevel
import com.example.model.Player
import kotlin.random.Random

data class PlayerSelectionResult(
    val player1: Player,
    val player2: Player,
    val cycleIncremented: Boolean,
    val newCycle: Int
)

class RandomPlayerEngine(
    private val playerRepository: PlayerRepository,
    private val random: Random = Random.Default
) {

    /**
     * Selects two distinct players of EXACTLY the specified difficulty level.
     * Guarantees:
     * 1. Same difficulty for both players.
     * 2. Player 1 != Player 2.
     * 3. Independent attributes (no bias on club, generation, continent, position, etc.).
     * 4. Anti-repetition: prioritizes players not yet used in current cycle.
     */
    suspend fun selectPairForDifficulty(
        difficulty: DifficultyLevel
    ): PlayerSelectionResult {
        val allDifficultyPlayers = playerRepository.getPlayersByDifficulty(difficulty.score)
        require(allDifficultyPlayers.size >= 2) {
            "Pas assez de joueurs pour la difficulté ${difficulty.titleFr} (minimum 2 requis)"
        }

        var currentCycle = playerRepository.getCurrentCycle(difficulty.score)
        var usedIds = playerRepository.getUsedPlayerIds(difficulty.score, currentCycle)
        var eligible = allDifficultyPlayers.filter { it.id !in usedIds }
        var cycleIncremented = false

        // If not enough unused players remain in this cycle, increment cycle
        if (eligible.size < 2) {
            currentCycle += 1
            cycleIncremented = true
            eligible = allDifficultyPlayers
        }

        // Shuffle eligible candidates independently
        val shuffled = eligible.shuffled(random)
        val p1 = shuffled[0]

        // Select p2 from remaining candidates (never equal to p1)
        val p2Candidates = shuffled.filter { it.id != p1.id }
        val p2 = if (p2Candidates.isNotEmpty()) {
            p2Candidates[0]
        } else {
            allDifficultyPlayers.filter { it.id != p1.id }.shuffled(random)[0]
        }

        // Record usage for anti-repetition tracking
        playerRepository.recordPlayerUsage(p1, currentCycle)
        playerRepository.recordPlayerUsage(p2, currentCycle)

        return PlayerSelectionResult(
            player1 = p1,
            player2 = p2,
            cycleIncremented = cycleIncremented,
            newCycle = currentCycle
        )
    }

    /**
     * Pure selection for unit testing without database dependency.
     */
    fun selectPairFromList(
        pool: List<Player>,
        difficultyScore: Int,
        usedIds: Set<String> = emptySet(),
        customRandom: Random = random
    ): Pair<Player, Player> {
        val eligibleDifficulty = pool.filter { it.difficultyScore == difficultyScore }
        require(eligibleDifficulty.size >= 2) {
            "Au moins 2 joueurs requis pour ce niveau"
        }

        var unused = eligibleDifficulty.filter { it.id !in usedIds }
        if (unused.size < 2) {
            unused = eligibleDifficulty
        }

        val shuffled = unused.shuffled(customRandom)
        val p1 = shuffled[0]
        val remaining = shuffled.filter { it.id != p1.id }
        val p2 = if (remaining.isNotEmpty()) {
            remaining[0]
        } else {
            eligibleDifficulty.filter { it.id != p1.id }.shuffled(customRandom)[0]
        }

        return Pair(p1, p2)
    }
}
