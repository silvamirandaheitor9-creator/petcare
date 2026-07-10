package com.petcare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.petcare.app.debug.StartupTimer
import com.petcare.app.ui.navigation.PetCareNavGraph
import com.petcare.app.ui.theme.PetCareTheme
import com.petcare.app.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        StartupTimer.mark("MainActivity.onCreate start")
        super.onCreate(savedInstanceState)
        StartupTimer.mark("MainActivity.onCreate: super.onCreate() done")
        enableEdgeToEdge()
        setContent {
            StartupTimer.mark("setContent: composition start")
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            StartupTimer.mark("setContent: hiltViewModel + collectAsState done")

            PetCareTheme(darkTheme = isDarkTheme) {
                PetCareNavGraph()
            }
        }
        StartupTimer.mark("MainActivity.onCreate end (setContent() call returned)")
    }
}
