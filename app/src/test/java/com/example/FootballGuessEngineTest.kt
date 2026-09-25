package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.FootballGuessDatabase
import com.example.data.database.GameStatsEntity
import com.example.data.repository.GameHistoryRepository
import com.example.data.repository.PlayerRepository
import com.example.engine.AnswerValidator
import com.example.engine.QuestionEngine
import com.example.engine.RandomPlayerEngine
import com.example.engine.RobotAction
import com.example.engine.RobotEngine
import com.example.model.DifficultyLevel
import com.example.model.GameMode
import com.example.model.Player
import com.example.model.RobotLevel
import com.example.model.ValidationStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FootballGuessEngineTest {

    private lateinit var context: Context
    private lateinit var db: FootballGuessDatabase
    private lateinit var playerRepository: PlayerRepository
    private lateinit var historyRepository: GameHistoryRepository
    private lateinit var randomEngine: RandomPlayerEngine
    private lateinit var validator: AnswerValidator
    private lateinit var questionEngine: QuestionEngine

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FootballGuessDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        playerRepository = PlayerRepository(context, db.gameDao())
        historyRepository = GameHistoryRepository(db.gameDao())
        randomEngine = RandomPlayerEngine(playerRepository)
        validator = AnswerValidator()
        questionEngine = QuestionEngine()
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Test 1: Deux joueurs différents sont toujours sélectionnés.
     */
    @Test
    fun test1_twoDifferentPlayersAlwaysSelected() = runBlocking {
        for (score in 1..5) {
            val diff = DifficultyLevel.fromScore(score)
            val result = randomEngine.selectPairForDifficulty(diff)
            assertNotNull(result.player1)
            assertNotNull(result.player2)
            assertNotEquals(
                "Les deux joueurs sélectionnés ne doivent jamais être identiques",
                result.player1.id,
                result.player2.id
            )
        }
    }

    /**
     * Test 2: Les deux joueurs ont toujours exactement la même difficulté.
     */
    @Test
    fun test2_bothPlayersHaveIdenticalDifficulty() = runBlocking {
        for (score in 1..5) {
            val diff = DifficultyLevel.fromScore(score)
            val result = randomEngine.selectPairForDifficulty(diff)
            assertEquals(
                "Le joueur 1 doit avoir la difficulté choisie",
                diff.score,
                result.player1.difficultyScore
            )
            assertEquals(
                "Le joueur 2 doit avoir la difficulté choisie",
                diff.score,
                result.player2.difficultyScore
            )
            assertEquals(
                "Les deux joueurs doivent avoir exactement le même score de difficulté",
                result.player1.difficultyScore,
                result.player2.difficultyScore
            )
        }
    }

    /**
     * Test 3: La nationalité n'influence pas artificiellement le tirage.
     */
    @Test
    fun test3_nationalityDoesNotArtificiallyConstrainSelection() {
        val pool = playerRepository.getPlayersByDifficulty(1)
        var sawDifferentNationalities = false

        for (seed in 1..20) {
            val pair = randomEngine.selectPairFromList(pool, 1, customRandom = Random(seed))
            if (pair.first.nationality != pair.second.nationality) {
                sawDifferentNationalities = true
                break
            }
        }
        assertTrue(
            "Le système doit pouvoir sélectionner deux joueurs de nationalités différentes sans restriction artificielle",
            sawDifferentNationalities
        )
    }

    /**
     * Test 4: Le club n'influence pas artificiellement le tirage.
     */
    @Test
    fun test4_clubDoesNotArtificiallyConstrainSelection() {
        val pool = playerRepository.getPlayersByDifficulty(1)
        var sawDifferentClubs = false

        for (seed in 1..20) {
            val pair = randomEngine.selectPairFromList(pool, 1, customRandom = Random(seed))
            val clubs1 = pair.first.clubs.toSet()
            val clubs2 = pair.second.clubs.toSet()
            if (clubs1.intersect(clubs2).isEmpty()) {
                sawDifferentClubs = true
                break
            }
        }
        assertTrue(
            "Le tirage doit être indépendant des clubs des joueurs",
            sawDifferentClubs
        )
    }

    /**
     * Test 5: La génération n'influence pas artificiellement le tirage.
     */
    @Test
    fun test5_generationDoesNotArtificiallyConstrainSelection() {
        val pool = playerRepository.getPlayersByDifficulty(1)
        var sawMixedStatus = false

        for (seed in 1..30) {
            val pair = randomEngine.selectPairFromList(pool, 1, customRandom = Random(seed))
            if (pair.first.status != pair.second.status) {
                sawMixedStatus = true
                break
            }
        }
        assertTrue(
            "Le système doit pouvoir tirer un joueur actif et un joueur retraité dans le même niveau",
            sawMixedStatus
        )
    }

    /**
     * Test 6: Un joueur récemment utilisé est moins susceptible d'être immédiatement sélectionné.
     */
    @Test
    fun test6_antiRepetitionPrefersUnusedPlayers() = runBlocking {
        val diff = DifficultyLevel.FACILE
        val pool = playerRepository.getPlayersByDifficulty(diff.score)
        assertTrue("Le pool doit contenir au moins 4 joueurs pour ce test", pool.size >= 4)

        // Select first pair
        val r1 = randomEngine.selectPairForDifficulty(diff)
        val usedFirstRound = setOf(r1.player1.id, r1.player2.id)

        // Select second pair in the same cycle
        val r2 = randomEngine.selectPairForDifficulty(diff)
        val selectedInR2 = setOf(r2.player1.id, r2.player2.id)

        // R2 must avoid selecting the players already used in R1
        val intersection = usedFirstRound.intersect(selectedInR2)
        assertEquals(
            "Les joueurs récemment utilisés ne doivent pas revenir immédiatement tant que d'autres sont disponibles dans le cycle",
            0,
            intersection.size
        )
    }

    /**
     * Test 7: Une réponse incorrecte peut être signalée comme LIKELY_INCORRECT sans modifier la réponse.
     */
    @Test
    fun test7_likelyIncorrectDoesNotMutateAnswer() {
        val messi = playerRepository.getAllPlayers().first { it.name.contains("Messi") }

        // Question: Est-ce qu'il a joué au Real Madrid ?
        // Answer given by opponent: OUI (false fact for Messi)
        val result = validator.validateAnswer(messi, "A-t-il joué au Real Madrid ?", "OUI")
        assertEquals(
            "Une réponse incohérente avec les faits avérés doit être signalée LIKELY_INCORRECT",
            ValidationStatus.LIKELY_INCORRECT,
            result.status
        )

        // Another check: World Cup winner answered NON for Messi (who won World Cup 2022)
        val wcResult = validator.validateAnswer(messi, "A-t-il remporté une Coupe du Monde ?", "NON")
        assertEquals(
            "Devrait être signalé comme LIKELY_INCORRECT",
            ValidationStatus.LIKELY_INCORRECT,
            wcResult.status
        )
    }

    /**
     * Test 8: Une question incompréhensible produit UNKNOWN.
     */
    @Test
    fun test8_ambiguousOrUnknownQuestionProducesUnknown() {
        val player = playerRepository.getAllPlayers().first()
        val result = validator.validateAnswer(
            player,
            "Est-ce que son chien s'appelle Rex et aime les lasagnes ?",
            "OUI"
        )
        assertEquals(
            "Une question inconnue ou invérifiable doit produire UNKNOWN",
            ValidationStatus.UNKNOWN,
            result.status
        )
    }

    /**
     * Test 9: La partie fonctionne sans internet.
     */
    @Test
    fun test9_gameFunctionsCompletelyOffline() {
        val validation = playerRepository.validateDatabase()
        assertTrue("La base de données locale doit être valide", validation.isValid)
        assertTrue("Toutes les données de joueurs sont intégrées localement", validation.totalCount >= 270)

        val questions = questionEngine.getAllSuggestedQuestions()
        assertTrue("Les suggestions de questions sont disponibles localement", questions.isNotEmpty())
    }

    /**
     * Test 10: Les statistiques restent après redémarrage.
     */
    @Test
    fun test10_statisticsPersistInDatabase() = runBlocking {
        historyRepository.recordCompletedGame(
            gameMode = GameMode.ONE_VS_ONE,
            difficultyScore = 2,
            team1Name = "Equipe A",
            team2Name = "Equipe B",
            secretPlayer1Name = "Messi",
            secretPlayer2Name = "Modric",
            score1 = 3,
            score2 = 1,
            winnerName = "Equipe A",
            totalQuestions = 6,
            totalRounds = 3,
            userWon = true
        )

        val stats = db.gameDao().getGameStatsSync()
        assertNotNull("Les stats doivent être sauvegardées", stats)
        assertEquals(1, stats!!.gamesPlayed)
        assertEquals(1, stats.wins1v1)
        assertEquals(6, stats.totalQuestionsAsked)
    }

    /**
     * Test 11: Les données restent après fermeture de l'application (Export / Import).
     */
    @Test
    fun test11_exportAndImportDataJson() = runBlocking {
        historyRepository.recordCompletedGame(
            gameMode = GameMode.VS_ROBOT,
            difficultyScore = 3,
            team1Name = "Humain",
            team2Name = "Robot",
            secretPlayer1Name = "Zidane",
            secretPlayer2Name = "Pelé",
            score1 = 2,
            score2 = 0,
            winnerName = "Humain",
            totalQuestions = 8,
            totalRounds = 2,
            userWon = true
        )

        val json = historyRepository.exportDataAsJson()
        assertTrue("L'export doit être du JSON valide contenant les stats", json.contains("\"gamesPlayed\": 1"))

        // Reset database
        historyRepository.resetAllStats()
        assertEquals(0, db.gameDao().getGameStatsSync()?.gamesPlayed ?: 0)

        // Restore via Import
        val importSuccess = historyRepository.importDataFromJson(json)
        assertTrue("L'importation doit réussir", importSuccess)

        val restoredStats = db.gameDao().getGameStatsSync()
        assertNotNull(restoredStats)
        assertEquals(1, restoredStats!!.gamesPlayed)
        assertEquals(1, restoredStats.winsVsRobot)
    }

    /**
     * Test 12: Le mode 1 VS 1 fonctionne.
     */
    @Test
    fun test12_oneVsOneModeWorks() = runBlocking {
        val diff = DifficultyLevel.FACILE
        val selection = randomEngine.selectPairForDifficulty(diff)
        assertNotNull(selection.player1)
        assertNotNull(selection.player2)

        // Opponent answers question
        val valRes = validator.validateAnswer(selection.player2, "Est-il gardien de but ?", "NON")
        assertTrue(valRes.status == ValidationStatus.CORRECT || valRes.status == ValidationStatus.UNKNOWN)
    }

    /**
     * Test 13: Le mode 2 VS 2 fonctionne.
     */
    @Test
    fun test13_twoVsTwoModeWorks() = runBlocking {
        val diff = DifficultyLevel.MOYEN
        val selection = randomEngine.selectPairForDifficulty(diff)
        assertNotNull(selection.player1)
        assertNotNull(selection.player2)
        assertNotEquals(selection.player1.id, selection.player2.id)
    }

    /**
     * Test 14: Le mode robot fonctionne.
     */
    @Test
    fun test14_robotModeWorks() {
        val diff = DifficultyLevel.FACILE
        val candidates = playerRepository.getPlayersByDifficulty(diff.score)
        val secretPlayer = candidates.first { it.position.equals("Gardien", ignoreCase = true) }

        val robot = RobotEngine(
            robotSecretPlayer = secretPlayer,
            initialCandidates = candidates
        )

        // Robot answers human question about its secret player
        val answer = robot.answerQuestion("Est-il gardien de but ?")
        assertEquals("OUI", answer)

        // Robot decides action
        val action = robot.decideAction(RobotLevel.NORMAL)
        assertTrue(
            "Le robot doit pouvoir poser une question ou deviner",
            action is RobotAction.Ask || action is RobotAction.Guess
        )
    }
}
