package com.example.model

data class GameState(
    val gameMode: GameMode = GameMode.ONE_VS_ONE,
    val difficulty: DifficultyLevel = DifficultyLevel.MOYEN,
    val robotLevel: RobotLevel = RobotLevel.NORMAL,
    val team1Name: String = "Joueur 1",
    val team2Name: String = "Joueur 2",
    val secretPlayer1: Player? = null,
    val secretPlayer2: Player? = null,
    val currentTurnIndex: Int = 0, // 0 for team 1, 1 for team 2
    val score1: Int = 0,
    val score2: Int = 0,
    val currentRound: Int = 1,
    val maxRounds: Int = 3,
    val penaltyOnWrongGuess: Boolean = true,
    val isGameOver: Boolean = false,
    val isRoundOver: Boolean = false,
    val roundWinnerName: String? = null,
    val matchWinnerName: String? = null,
    val questionsHistory: List<QuestionItem> = emptyList(),
    val isRevealingPlayer1: Boolean = false,
    val isRevealingPlayer2: Boolean = false,
    val hasPlayer1SeenSecret: Boolean = false,
    val hasPlayer2SeenSecret: Boolean = false,
    val isRobotThinking: Boolean = false
) {
    val currentTurnName: String
        get() = if (currentTurnIndex == 0) team1Name else team2Name

    val opponentTurnName: String
        get() = if (currentTurnIndex == 0) team2Name else team1Name

    // The secret player that the current team is trying to guess (opponent's secret player)
    val targetSecretPlayer: Player?
        get() = if (currentTurnIndex == 0) secretPlayer2 else secretPlayer1

    // The current team's own secret player
    val ownSecretPlayer: Player?
        get() = if (currentTurnIndex == 0) secretPlayer1 else secretPlayer2
}
