package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DifficultyLevel
import com.example.model.Player
import com.example.ui.components.PlayerCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseExplorerScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val allPlayers = remember { viewModel.playerRepository.getAllPlayers() }
    val validation by viewModel.databaseValidation.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficultyFilter by remember { mutableIntStateOf(0) } // 0 = all
    var selectedPositionFilter by remember { mutableStateOf("Tous") }
    var selectedPlayerDetail by remember { mutableStateOf<Player?>(null) }

    val filteredPlayers = remember(searchQuery, selectedDifficultyFilter, selectedPositionFilter, allPlayers) {
        allPlayers.filter { player ->
            val matchesSearch = searchQuery.isBlank() ||
                    player.name.contains(searchQuery, ignoreCase = true) ||
                    (player.nationality?.contains(searchQuery, ignoreCase = true) == true) ||
                    player.clubs.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesDiff = selectedDifficultyFilter == 0 || player.difficultyScore == selectedDifficultyFilter

            val matchesPos = selectedPositionFilter == "Tous" ||
                    player.position.equals(selectedPositionFilter, ignoreCase = true)

            matchesSearch && matchesDiff && matchesPos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Base des Joueurs (${allPlayers.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("explorer_back_button")
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Validation status badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (validation.isValid) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (validation.isValid) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (validation.isValid) EmeraldPrimary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (validation.isValid) "Base validée : ${validation.totalCount} joueurs certifiés sans doublons"
                        else "Attention : ${validation.issues.size} avertissement(s) de validation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (validation.isValid) EmeraldPrimary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher un joueur, un club, un pays...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explorer_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Difficulty filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedDifficultyFilter == 0,
                        onClick = { selectedDifficultyFilter = 0 },
                        label = { Text("Toutes diff.", fontSize = 11.sp) }
                    )
                }
                items(DifficultyLevel.entries) { diff ->
                    val isSelected = selectedDifficultyFilter == diff.score
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDifficultyFilter = if (isSelected) 0 else diff.score },
                        label = { Text("${diff.emoji} ${diff.titleFr}", fontSize = 11.sp) }
                    )
                }
            }

            // Position filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Tous", "Attaquant", "Milieu", "Défenseur", "Gardien").forEach { pos ->
                    item {
                        FilterChip(
                            selected = selectedPositionFilter == pos,
                            onClick = { selectedPositionFilter = pos },
                            label = { Text(pos, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${filteredPlayers.size} joueur(s) trouvé(s)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Players list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPlayers, key = { it.id }) { player ->
                    PlayerRowItem(
                        player = player,
                        onClick = { selectedPlayerDetail = player }
                    )
                }
            }
        }
    }

    // Detail bottom sheet
    if (selectedPlayerDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPlayerDetail = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerCard(player = selectedPlayerDetail!!)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PlayerRowItem(
    player: Player,
    onClick: () -> Unit
) {
    val diffLevel = DifficultyLevel.fromScore(player.difficultyScore)
    val diffColor = Color(diffLevel.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(diffColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.countryFlag, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${player.position} • ${player.nationality ?: ""}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = diffColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = diffLevel.titleFr,
                    color = diffColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
