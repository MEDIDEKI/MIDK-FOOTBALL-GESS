package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DatabaseExplorerScreen
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocalNetworkScreen
import com.example.ui.screens.RevealSecretScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.FootballGuessTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FootballGuessTheme {
                val gameViewModel: GameViewModel = viewModel()
                FootballGuessApp(viewModel = gameViewModel)
            }
        }
    }
}

@Composable
fun FootballGuessApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(viewModel = viewModel)
            }
            AppScreen.SETUP -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                SetupScreen(viewModel = viewModel)
            }
            AppScreen.REVEAL_SECRET -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                RevealSecretScreen(viewModel = viewModel)
            }
            AppScreen.GAMEPLAY -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                GamePlayScreen(viewModel = viewModel)
            }
            AppScreen.STATS -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                StatsScreen(viewModel = viewModel)
            }
            AppScreen.HISTORY -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                HistoryScreen(viewModel = viewModel)
            }
            AppScreen.EXPLORER -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                DatabaseExplorerScreen(viewModel = viewModel)
            }
            AppScreen.SETTINGS -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                SettingsScreen(viewModel = viewModel)
            }
            AppScreen.LOCAL_NETWORK -> {
                BackHandler { viewModel.navigateTo(AppScreen.HOME) }
                LocalNetworkScreen(viewModel = viewModel)
            }
        }
    }
}
