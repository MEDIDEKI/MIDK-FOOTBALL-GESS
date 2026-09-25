package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.FootballGuessDatabase
import com.example.data.database.GameRecordEntity
import com.example.data.database.GameStatsEntity
import com.example.data.repository.DatabaseValidationResult
import com.example.data.repository.GameHistoryRepository
import com.example.data.repository.PlayerRepository
import com.example.engine.AnswerValidator
import com.example.engine.QuestionEngine
import com.example.engine.RandomPlayerEngine
import com.example.engine.RobotAction
import com.example.engine.RobotEngine
import com.example.model.DifficultyLevel
import com.example.model.GameMode
import com.example.model.GameState
import com.example.model.Player
import com.example.model.QuestionItem
import com.example.model.RobotLevel
import com.example.model.ValidationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    HOME,
    SETUP,
    REVEAL_SECRET,
    GAMEPLAY,
    STATS,
    HISTORY,
    EXPLORER,
    SETTINGS,
    LOCAL_NETWORK
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FootballGuessDatabase.getInstance(application)
    private val gameDao = db.gameDao()
    val playerRepository = PlayerRepository(application, gameDao)
    val historyRepository = GameHistoryRepository(gameDao)
    val randomEngine = RandomPlayerEngine(playerRepository)
    val questionEngine = QuestionEngine()
    val answerValidator = AnswerValidator()

    private var robotEngine: RobotEngine? = null

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _databaseValidation = MutableStateFlow(playerRepository.validateDatabase())
    val databaseValidation: StateFlow<DatabaseValidationResult> = _databaseValidation.asStateFlow()

    val statsFlow: StateFlow<GameStatsEntity?> = historyRepository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val historyFlow: StateFlow<List<GameRecordEntity>> = historyRepository.allGameRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _validationAlertsEnabled = MutableStateFlow(true)
    val validationAlertsEnabled: StateFlow<Boolean> = _validationAlertsEnabled.asStateFlow()

    // Transient UI message / toast alert
    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    // Robot pending question that requires human answer
    private val _pendingRobotQuestion = MutableStateFlow<String?>(null)
    val pendingRobotQuestion: StateFlow<String?> = _pendingRobotQuestion.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun clearNotice() {
        _uiNotice.value = null
    }

    fun showNotice(msg: String) {
        _uiNotice.value = msg
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
    }

    fun setValidationAlertsEnabled(enabled: Boolean) {
        _validationAlertsEnabled.value = enabled
    }

    fun startSetup(mode: GameMode) {
        val (defT1, defT2) = when (mode) {
            GameMode.ONE_VS_ONE -> Pair("Joueur 1", "Joueur 2")
            GameMode.TWO_VS_TWO -> Pair("Équipe A", "Équipe B")
            GameMode.VS_ROBOT -> Pair("Joueur", "Robot IA")
        }
        _gameState.value = GameState(
            gameMode = mode,
            team1Name = defT1,
            team2Name = defT2
        )
        _currentScreen.value = AppScreen.SETUP
    }

    fun updateSetup(
        team1Name: String,
        team2Name: String,
        difficulty: DifficultyLevel,
        robotLevel: RobotLevel,
        maxRounds: Int,
        penaltyOnWrongGuess: Boolean
    ) {
        _gameState.value = _gameState.value.copy(
            team1Name = team1Name.ifBlank { "Joueur 1" },
            team2Name = team2Name.ifBlank { "Joueur 2" },
            difficulty = difficulty,
            robotLevel = robotLevel,
            maxRounds = maxRounds,
            penaltyOnWrongGuess = penaltyOnWrongGuess
        )
    }

    fun startMatch() {
        viewModelScope.launch {
            val diff = _gameState.value.difficulty
            val selection = randomEngine.selectPairForDifficulty(diff)

            // Setup Robot if robot mode
            if (_gameState.value.gameMode == GameMode.VS_ROBOT) {
                val candidatePool = playerRepository.getPlayersByDifficulty(diff.score)
                robotEngine = RobotEngine(
                    robotSecretPlayer = selection.player2,
                    initialCandidates = candidatePool,
                    validator = answerValidator
                )
            } else {
                robotEngine = null
            }

            _gameState.value = _gameState.value.copy(
                secretPlayer1 = selection.player1,
                secretPlayer2 = selection.player2,
                currentRound = 1,
                score1 = 0,
                score2 = 0,
                currentTurnIndex = 0,
                isGameOver = false,
                isRoundOver = false,
                roundWinnerName = null,
                matchWinnerName = null,
                questionsHistory = emptyList(),
                isRevealingPlayer1 = false,
                isRevealingPlayer2 = false,
                hasPlayer1SeenSecret = false,
                hasPlayer2SeenSecret = _gameState.value.gameMode == GameMode.VS_ROBOT,
                isRobotThinking = false
            )
            _pendingRobotQuestion.value = null
            _currentScreen.value = AppScreen.REVEAL_SECRET
        }
    }

    fun revealSecretPlayer1(reveal: Boolean) {
        _gameState.value = _gameState.value.copy(
            isRevealingPlayer1 = reveal,
            hasPlayer1SeenSecret = true
        )
    }

    fun revealSecretPlayer2(reveal: Boolean) {
        _gameState.value = _gameState.value.copy(
            isRevealingPlayer2 = reveal,
            hasPlayer2SeenSecret = true
        )
    }

    fun finishRevealAndBegin() {
        _gameState.value = _gameState.value.copy(
            isRevealingPlayer1 = false,
            isRevealingPlayer2 = false
        )
        _currentScreen.value = AppScreen.GAMEPLAY
    }

    /**
     * Human asks a question to opponent (or robot).
     */
    fun askQuestion(questionText: String, answer: String) {
        val state = _gameState.value
        val askingTeamIndex = state.currentTurnIndex
        val askingTeamName = state.currentTurnName

        // The answer relates to the opponent's secret player
        val targetSecretPlayer = state.targetSecretPlayer ?: return

        // AI Validation: verify opponent answer against verified target player data
        val valResult = answerValidator.validateAnswer(targetSecretPlayer, questionText, answer)

        val item = QuestionItem(
            id = UUID.randomUUID().toString(),
            questionText = questionText.trim(),
            answer = answer.uppercase().trim(),
            askedByTeamIndex = askingTeamIndex,
            askedByTeamName = askingTeamName,
            validationStatus = valResult.status,
            validationNote = valResult.reason
        )

        val newHistory = listOf(item) + state.questionsHistory

        // Check turn switch
        val nextTurnIndex = if (askingTeamIndex == 0) 1 else 0
        _gameState.value = state.copy(
            questionsHistory = newHistory,
            currentTurnIndex = nextTurnIndex
        )

        if (_validationAlertsEnabled.value && valResult.status == ValidationStatus.LIKELY_INCORRECT) {
            showNotice("⚠️ Réponse probablement incorrecte d'après les données vérifiées !")
        }

        // If next turn is Robot, trigger robot action
        if (state.gameMode == GameMode.VS_ROBOT && nextTurnIndex == 1 && !state.isRoundOver) {
            triggerRobotTurn()
        }
    }

    /**
     * Evaluates question directly for Robot when human asks Robot
     */
    fun answerForRobot(questionText: String): String {
        return robotEngine?.answerQuestion(questionText) ?: "NON"
    }

    private fun triggerRobotTurn() {
        val robot = robotEngine ?: return
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(isRobotThinking = true)
            delay(1200) // Realistic thoughtful hesitation
            _gameState.value = _gameState.value.copy(isRobotThinking = false)

            when (val action = robot.decideAction(_gameState.value.robotLevel)) {
                is RobotAction.Guess -> {
                    // Robot attempts a guess!
                    submitGuess(action.predictedPlayerName, isRobotGuess = true)
                }
                is RobotAction.Ask -> {
                    _pendingRobotQuestion.value = action.questionText
                }
            }
        }
    }

    /**
     * Human answers Robot's pending question
     */
    fun submitHumanAnswerToRobot(answer: String) {
        val qText = _pendingRobotQuestion.value ?: return
        _pendingRobotQuestion.value = null
        val state = _gameState.value

        // Human's secret player is player 1
        val humanSecret = state.secretPlayer1 ?: return

        // Update robot's candidate pool
        robotEngine?.processAnswer(qText, answer)

        // Validation for the human's answer to robot
        val valResult = answerValidator.validateAnswer(humanSecret, qText, answer)

        val item = QuestionItem(
            id = UUID.randomUUID().toString(),
            questionText = qText,
            answer = answer.uppercase().trim(),
            askedByTeamIndex = 1,
            askedByTeamName = state.team2Name,
            validationStatus = valResult.status,
            validationNote = valResult.reason
        )

        _gameState.value = state.copy(
            questionsHistory = listOf(item) + state.questionsHistory,
            currentTurnIndex = 0 // Pass back to human
        )

        if (_validationAlertsEnabled.value && valResult.status == ValidationStatus.LIKELY_INCORRECT) {
            showNotice("⚠️ Votre réponse semble en contradiction avec votre joueur secret !")
        }
    }

    /**
     * Submits a guess for the opponent's secret player.
     */
    fun submitGuess(guessedPlayerName: String, isRobotGuess: Boolean = false) {
        val state = _gameState.value
        val guessingTeamIndex = state.currentTurnIndex
        val guessingTeamName = state.currentTurnName
        val targetSecret = state.targetSecretPlayer ?: return

        val isCorrect = guessedPlayerName.trim().equals(targetSecret.name.trim(), ignoreCase = true)

        if (isCorrect) {
            // Correct guess!
            val newScore1 = if (guessingTeamIndex == 0) state.score1 + 1 else state.score1
            val newScore2 = if (guessingTeamIndex == 1) state.score2 + 1 else state.score2

            val isMatchOver = state.currentRound >= state.maxRounds
            val matchWinner = if (isMatchOver) {
                if (newScore1 > newScore2) state.team1Name
                else if (newScore2 > newScore1) state.team2Name
                else "Égalité parfaite"
            } else null

            _gameState.value = state.copy(
                score1 = newScore1,
                score2 = newScore2,
                isRoundOver = true,
                isGameOver = isMatchOver,
                roundWinnerName = guessingTeamName,
                matchWinnerName = matchWinner
            )

            // If match is over, record in Room
            if (isMatchOver) {
                viewModelScope.launch {
                    val userWon = if (state.gameMode == GameMode.VS_ROBOT) newScore1 > newScore2 else true
                    historyRepository.recordCompletedGame(
                        gameMode = state.gameMode,
                        difficultyScore = state.difficulty.score,
                        team1Name = state.team1Name,
                        team2Name = state.team2Name,
                        secretPlayer1Name = state.secretPlayer1?.name ?: "",
                        secretPlayer2Name = state.secretPlayer2?.name ?: "",
                        score1 = newScore1,
                        score2 = newScore2,
                        winnerName = matchWinner ?: "Égalité",
                        totalQuestions = state.questionsHistory.size,
                        totalRounds = state.currentRound,
                        userWon = userWon
                    )
                }
            }
        } else {
            // Wrong guess!
            val penalty = state.penaltyOnWrongGuess
            val penalizedScore1 = if (penalty && guessingTeamIndex == 0) (state.score1 - 1).coerceAtLeast(0) else state.score1
            val penalizedScore2 = if (penalty && guessingTeamIndex == 1) (state.score2 - 1).coerceAtLeast(0) else state.score2

            val nextTurnIndex = if (guessingTeamIndex == 0) 1 else 0
            _gameState.value = state.copy(
                score1 = penalizedScore1,
                score2 = penalizedScore2,
                currentTurnIndex = nextTurnIndex
            )

            showNotice("❌ Mauvaise proposition pour $guessingTeamName ! (Ce n'est pas $guessedPlayerName)")

            if (state.gameMode == GameMode.VS_ROBOT && nextTurnIndex == 1) {
                triggerRobotTurn()
            }
        }
    }

    fun passTurn() {
        val state = _gameState.value
        val nextTurnIndex = if (state.currentTurnIndex == 0) 1 else 0
        _gameState.value = state.copy(currentTurnIndex = nextTurnIndex)

        if (state.gameMode == GameMode.VS_ROBOT && nextTurnIndex == 1 && !state.isRoundOver) {
            triggerRobotTurn()
        }
    }

    fun nextRound() {
        viewModelScope.launch {
            val state = _gameState.value
            val diff = state.difficulty
            val selection = randomEngine.selectPairForDifficulty(diff)

            if (state.gameMode == GameMode.VS_ROBOT) {
                val candidatePool = playerRepository.getPlayersByDifficulty(diff.score)
                robotEngine = RobotEngine(
                    robotSecretPlayer = selection.player2,
                    initialCandidates = candidatePool,
                    validator = answerValidator
                )
            }

            _gameState.value = state.copy(
                currentRound = state.currentRound + 1,
                secretPlayer1 = selection.player1,
                secretPlayer2 = selection.player2,
                currentTurnIndex = 0,
                isRoundOver = false,
                roundWinnerName = null,
                questionsHistory = emptyList(),
                isRevealingPlayer1 = false,
                isRevealingPlayer2 = false,
                hasPlayer1SeenSecret = false,
                hasPlayer2SeenSecret = state.gameMode == GameMode.VS_ROBOT
            )
            _pendingRobotQuestion.value = null
            _currentScreen.value = AppScreen.REVEAL_SECRET
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            historyRepository.resetAllStats()
            showNotice("Données et statistiques réinitialisées avec succès.")
        }
    }

    suspend fun exportJson(): String {
        return historyRepository.exportDataAsJson()
    }

    suspend fun importJson(json: String): Boolean {
        val success = historyRepository.importDataFromJson(json)
        if (success) {
            showNotice("Données importées avec succès !")
        } else {
            showNotice("Échec de l'importation : format JSON invalide.")
        }
        return success
    }
}
