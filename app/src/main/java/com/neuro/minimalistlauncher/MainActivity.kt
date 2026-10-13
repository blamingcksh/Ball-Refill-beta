package com.neuro.minimalistlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neuro.minimalistlauncher.ui.home.HomeScreen
import com.neuro.minimalistlauncher.ui.home.LauncherViewModel
import com.neuro.minimalistlauncher.ui.theme.NeuroMinimalLauncherTheme
import com.neuro.minimalistlauncher.ui.theme.PureBlack

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        val app = application as NeuroMinimalApplication
        LauncherViewModel.Factory(
            appRepository = app.appRepository,
            preferences = app.preferences,
            archiveDao = app.database.dailyArchiveDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            NeuroMinimalLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PureBlack
                ) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDailyRollover()
    }
}
