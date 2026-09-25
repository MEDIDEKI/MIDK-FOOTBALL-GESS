package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.Player
import com.example.model.QuestionItem
import com.example.model.ValidationStatus
import com.example.ui.components.PlayerCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val uiNotice by viewModel.uiNotice.collectAsState()
    val pendingRobotQuestion by viewModel.pendingRobotQuestion.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showAskDialog by remember { mutableStateOf(false) }
    var showGuessDialog by remember { mutableStateOf(false) }
    var showPeekSheet by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiNotice) {
        uiNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${state.gameMode.titleFr} • Manche ${state.currentRound}/${state.maxRounds}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Niveau ${state.difficulty.titleFr}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQuitDialog = true },
                        modifier = Modifier.testTag("game_quit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Quitter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            GameBottomActionBar(
                onAskClick = { showAskDialog = true },
                onGuessClick = { showGuessDialog = true },
                onPeekClick = { showPeekSheet = true },
                onPassClick = { viewModel.passTurn() },
                isRobotTurn = state.gameMode == GameMode.VS_ROBOT && state.currentTurnIndex == 1
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Scoreboard Header Card
            ScoreboardCard(
                team1Name = state.team1Name,
                team2Name = state.team2Name,
                score1 = state.score1,
                score2 = state.score2,
                questionsCount = state.questionsHistory.size,
                currentTurnIndex = state.currentTurnIndex
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Turn & Status Banner
            TurnBanner(
                currentTurnName = state.currentTurnName,
                isRobotTurn = state.gameMode == GameMode.VS_ROBOT && state.currentTurnIndex == 1,
                isRobotThinking = state.isRobotThinking,
                pendingRobotQuestion = pendingRobotQuestion,
                onAnswerRobot = { answer -> viewModel.submitHumanAnswerToRobot(answer) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Questions History Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HISTORIQUE DES QUESTIONS (${state.questionsHistory.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Questions List
            if (state.questionsHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = "No questions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucune question posée pour l'instant.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Cliquez sur 'Poser une question' pour commencer !",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.questionsHistory, key = { it.id }) { item ->
                        QuestionHistoryItem(item)
                    }
                }
            }
        }
    }

    // Modal dialogs & sheets
    if (showAskDialog) {
        AskQuestionDialog(
            viewModel = viewModel,
            onDismiss = { showAskDialog = false }
        )
    }

    if (showGuessDialog) {
        GuessPlayerDialog(
            viewModel = viewModel,
            onDismiss = { showGuessDialog = false }
        )
    }

    if (showPeekSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPeekSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VOTRE JOUEUR SECRET (${state.currentTurnName})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(12.dp))

                state.ownSecretPlayer?.let { player ->
                    PlayerCard(player = player, isSecretReveal = true)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showPeekSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cacher et fermer")
                }
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quitter la partie ?") },
            text = { Text("Voulez-vous abandonner ce match et revenir à l'accueil ?") },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitDialog = false
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Round / Game Over Dialog
    if (state.isRoundOver) {
        RoundEndDialog(
            state = state,
            onNextRound = { viewModel.nextRound() },
            onHome = { viewModel.navigateTo(AppScreen.HOME) }
        )
    }
}

@Composable
private fun ScoreboardCard(
    team1Name: String,
    team2Name: String,
    score1: Int,
    score2: Int,
    questionsCount: Int,
    currentTurnIndex: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Team 1
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = team1Name,
                        fontWeight = if (currentTurnIndex == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (currentTurnIndex == 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$score1",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // VS separator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$questionsCount Qs",
                        fontSize = 10.sp,
                        color = GoldAccent
                    )
                }

                // Team 2
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = team2Name,
                        fontWeight = if (currentTurnIndex == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (currentTurnIndex == 1) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$score2",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnBanner(
    currentTurnName: String,
    isRobotTurn: Boolean,
    isRobotThinking: Boolean,
    pendingRobotQuestion: String?,
    onAnswerRobot: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isRobotTurn) GoldAccent.copy(alpha = 0.15f) else EmeraldPrimary.copy(alpha = 0.15f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = if (isRobotTurn) "🤖" else "🎯", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tour de : $currentTurnName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isRobotTurn) GoldAccent else EmeraldPrimary
                )
            }

            if (isRobotThinking) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Le robot réfléchit à sa question...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (pendingRobotQuestion != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Le robot vous demande : \"$pendingRobotQuestion\"",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onAnswerRobot("OUI") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("robot_answer_oui_button")
                    ) {
                        Text("OUI", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAnswerRobot("NON") },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("robot_answer_non_button")
                    ) {
                        Text("NON", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionHistoryItem(item: QuestionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "De : ${item.askedByTeamName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Answer Chip
                val isYes = item.answer == "OUI"
                Surface(
                    color = if (isYes) EmeraldPrimary.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.answer,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (isYes) EmeraldPrimary else ErrorRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Warning note if LIKELY_INCORRECT
            if (item.validationStatus == ValidationStatus.LIKELY_INCORRECT) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚠️", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Réponse probablement incorrecte d'après les données vérifiées.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBottomActionBar(
    onAskClick: () -> Unit,
    onGuessClick: () -> Unit,
    onPeekClick: () -> Unit,
    onPassClick: () -> Unit,
    isRobotTurn: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAskClick,
                enabled = !isRobotTurn,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("action_ask_question_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Question", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onGuessClick,
                enabled = !isRobotTurn,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("action_guess_player_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("🎯 Deviner", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            IconButton(
                onClick = onPeekClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("action_peek_secret_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Mon joueur",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onPassClick,
                enabled = !isRobotTurn,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("action_pass_turn_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Passer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AskQuestionDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val state = viewModel.gameState.collectAsState().value
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var customQuestionText by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf<String?>(null) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val categories = viewModel.questionEngine.standardCategories
    val isVsRobot = state.gameMode == GameMode.VS_ROBOT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Poser une question", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedQuestion == null) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedCategoryIndex,
                        edgePadding = 0.dp
                    ) {
                        categories.forEachIndexed { index, cat ->
                            Tab(
                                selected = selectedCategoryIndex == index,
                                onClick = { selectedCategoryIndex = index },
                                text = { Text("${cat.icon} ${cat.titleFr}", fontSize = 11.sp) }
                            )
                        }
                        Tab(
                            selected = selectedCategoryIndex == categories.size,
                            onClick = { selectedCategoryIndex = categories.size },
                            text = { Text("✍️ Libre", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedCategoryIndex < categories.size) {
                        val currentCategory = categories[selectedCategoryIndex]
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(currentCategory.questions) { q ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedQuestion = q
                                            if (isVsRobot) {
                                                // Robot answers automatically!
                                                selectedAnswer = viewModel.answerForRobot(q)
                                            }
                                        },
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = q,
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        // Custom Question tab
                        OutlinedTextField(
                            value = customQuestionText,
                            onValueChange = { customQuestionText = it },
                            label = { Text("Votre question personnalisée") },
                            placeholder = { Text("Ex: A-t-il marqué en finale de C1 ?") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_question_input"),
                            minLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (customQuestionText.isNotBlank()) {
                                    selectedQuestion = customQuestionText.trim()
                                    if (isVsRobot) {
                                        selectedAnswer = viewModel.answerForRobot(customQuestionText.trim())
                                    }
                                }
                            },
                            enabled = customQuestionText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_custom_question_button")
                        ) {
                            Text("Choisir cette question")
                        }
                    }
                } else {
                    // Question selected -> Prompt for opponent's response
                    Text(
                        text = "Question posée :",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedQuestion!!,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isVsRobot) "Réponse du Robot :" else "Réponse de ${state.opponentTurnName} :",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isVsRobot) {
                        Text(
                            text = "Robot répond : ${selectedAnswer ?: "OUI"}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (selectedAnswer == "OUI") EmeraldPrimary else ErrorRed
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { selectedAnswer = "OUI" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("answer_oui_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAnswer == "OUI") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("OUI", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedAnswer = "NON" },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("answer_non_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedAnswer == "NON") ErrorRed else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("NON", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedQuestion != null && selectedAnswer != null) {
                Button(
                    onClick = {
                        viewModel.askQuestion(selectedQuestion!!, selectedAnswer!!)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("validate_question_action_button")
                ) {
                    Text("Valider la question")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun GuessPlayerDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val state = viewModel.gameState.collectAsState().value
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var showConfirmPrompt by remember { mutableStateOf(false) }

    val pool = remember(state.difficulty) {
        viewModel.playerRepository.getPlayersByDifficulty(state.difficulty.score)
    }

    val filteredPool = remember(searchQuery, pool) {
        if (searchQuery.isBlank()) pool
        else pool.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.nationality?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    if (showConfirmPrompt && selectedPlayer != null) {
        AlertDialog(
            onDismissRequest = { showConfirmPrompt = false },
            title = { Text("Confirmer votre proposition ?") },
            text = {
                Text(
                    text = "Pensez-vous que le joueur secret de ${state.opponentTurnName} est :\n\n" +
                            "👉 ${selectedPlayer!!.name} (${selectedPlayer!!.nationality ?: ""})\n\n" +
                            if (state.penaltyOnWrongGuess) "⚠️ Une mauvaise réponse entraîne une pénalité !" else ""
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmPrompt = false
                        viewModel.submitGuess(selectedPlayer!!.name)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("confirm_guess_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Oui, c'est lui !", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPrompt = false }) {
                    Text("Annuler")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🎯 Deviner le joueur secret") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choisissez parmi les joueurs éligibles de niveau ${state.difficulty.titleFr} :",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher un joueur...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_guess_player_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(filteredPool, key = { it.id }) { player ->
                            val isSelected = selectedPlayer?.id == player.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedPlayer = player
                                    },
                                color = if (isSelected) GoldAccent.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = player.countryFlag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = player.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${player.position} • ${player.status}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Text("✓", color = GoldAccent, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfirmPrompt = true },
                    enabled = selectedPlayer != null,
                    modifier = Modifier.testTag("select_guess_player_button")
                ) {
                    Text("Proposer")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
private fun RoundEndDialog(
    state: com.example.model.GameState,
    onNextRound: () -> Unit,
    onHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismiss without choice */ },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (state.isGameOver) "🏆 MATCH TERMINÉ !" else "🎉 MANCHE TERMINÉE !",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = GoldAccent
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.isGameOver) "Vainqueur du match : ${state.matchWinnerName}"
                    else "Vainqueur de la manche : ${state.roundWinnerName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Score : ${state.team1Name} ${state.score1} - ${state.score2} ${state.team2Name}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reveal secret players
                Text(
                    text = "Révélation des joueurs secrets :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                state.secretPlayer1?.let { p1 ->
                    Text(
                        text = "Joueur de ${state.team1Name} : ${p1.name} (${p1.countryFlag} ${p1.nationality ?: ""})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                state.secretPlayer2?.let { p2 ->
                    Text(
                        text = "Joueur de ${state.team2Name} : ${p2.name} (${p2.countryFlag} ${p2.nationality ?: ""})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            if (!state.isGameOver) {
                Button(
                    onClick = onNextRound,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Manche suivante ▶")
                }
            } else {
                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Menu Principal")
                }
            }
        },
        dismissButton = {
            if (!state.isGameOver) {
                TextButton(onClick = onHome) {
                    Text("Quitter")
                }
            }
        }
    )
}
