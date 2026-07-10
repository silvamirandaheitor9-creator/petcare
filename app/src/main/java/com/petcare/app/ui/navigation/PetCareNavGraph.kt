package com.petcare.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.petcare.app.ui.screen.SplashScreen
import com.petcare.app.ui.screen.onboarding.OnboardingScreen
import com.petcare.app.ui.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Onboarding: Screen("onboarding")
    object Main      : Screen("main")
}

@Composable
fun PetCareNavGraph() {
    val navController   = rememberNavController()
    val appViewModel: AppViewModel = hiltViewModel()
    val isOnboardingDone by appViewModel.isOnboardingDone.collectAsState()
    val isReady          by appViewModel.isReady.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                isReady   = isReady,
                onNavigate = {
                    val dest = if (isOnboardingDone) Screen.Main.route else Screen.Onboarding.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Onboarding.route) {
            // SPEC seção 5 — tarefa 1: pager + pegadas + Próximo/Pular + gesto voltar
            // Tarefa 4 adicionará a lógica completa de aceite dos Termos.
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Main.route) {
            // TODO: Seção 6–15 — app principal (próximas tarefas)
            SplashScreen(isReady = true, onNavigate = {})
        }
    }
}
