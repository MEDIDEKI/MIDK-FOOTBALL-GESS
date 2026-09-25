package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DifficultyLevel
import com.example.model.GameMode
import com.example.model.RobotLevel
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()

    var team1Name by remember { mutableStateOf(state.team1Name) }
    var team2Name by remember { mutableStateOf(state.team2Name) }
    var selectedDifficulty by remember { mutableStateOf(state.difficulty) }
    var selectedRobotLevel by remember { mutableStateOf(state.robotLevel) }
    var selectedRounds by remember { mutableStateOf(state.maxRounds) }
    var penaltyOnWrongGuess by remember { mutableStateOf(state.penaltyOnWrongGuess) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuration • ${state.gameMode.titleFr}",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("setup_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Player / Team Names Section
            Text(
                text = "NOMS DES PARTICIPANTS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = team1Name,
                onValueChange = { team1Name = it },
                label = {
                    Text(if (state.gameMode == GameMode.TWO_VS_TWO) "Nom Équipe A" else "Joueur 1")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("team1_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = team2Name,
                onValueChange = { team2Name = it },
                enabled = state.gameMode != GameMode.VS_ROBOT,
                label = {
                    Text(
                        when (state.gameMode) {
                            GameMode.TWO_VS_TWO -> "Nom Équipe B"
                            GameMode.VS_ROBOT -> "Robot IA"
                            GameMode.ONE_VS_ONE -> "Joueur 2"
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("team2_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shared Difficulty Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DIFFICULTÉ COMMUNE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(Identique pour les deux camps)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DifficultyLevel.entries.forEach { diff ->
                val isSelected = diff == selectedDifficulty
                val diffColor = Color(diff.colorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) diffColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedDifficulty = diff }
                        .testTag("difficulty_${diff.score}_button"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) diffColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = diff.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = diff.titleFr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSelected) diffColor else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Niveau ${diff.score}/5",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = diff.descriptionFr,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Text(text = "✓", color = diffColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            }

            // Robot Level (only if Robot mode)
            if (state.gameMode == GameMode.VS_ROBOT) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "NIVEAU DE L'INTELLIGENCE ARTIFICIELLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RobotLevel.entries.forEach { level ->
                        val isSelected = level == selectedRobotLevel
                        Button(
                            onClick = { selectedRobotLevel = level },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("robot_level_${level.name}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = level.titleFr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Number of rounds
            Text(
                text = "NOMBRE DE MANCHES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(1, 3, 5).forEach { rounds ->
                    val isSelected = rounds == selectedRounds
                    Button(
                        onClick = { selectedRounds = rounds },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rounds_${rounds}_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "$rounds ${if (rounds == 1) "Manche" else "Manches"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Penalty Rule toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pénalité sur mauvaise réponse",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Déduit 1 point en cas de fausse devinette",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = penaltyOnWrongGuess,
                    onCheckedChange = { penaltyOnWrongGuess = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldPrimary,
                        checkedTrackColor = EmeraldPrimary.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Start Button
            Button(
                onClick = {
                    viewModel.updateSetup(
                        team1Name = team1Name,
                        team2Name = team2Name,
                        difficulty = selectedDifficulty,
                        robotLevel = selectedRobotLevel,
                        maxRounds = selectedRounds,
                        penaltyOnWrongGuess = penaltyOnWrongGuess
                    )
                    viewModel.startMatch()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("launch_match_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LANCER LE MATCH",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
