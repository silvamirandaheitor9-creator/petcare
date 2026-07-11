package com.petcare.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.petcare.app.ui.screen.SplashScreen
import com.petcare.app.ui.screen.main.MainScreen
import com.petcare.app.ui.screen.main.diary.DiaryPhotoEditorScreen
import com.petcare.app.ui.screen.onboarding.OnboardingScreen
import com.petcare.app.ui.viewmodel.AppViewModel
import com.petcare.app.ui.viewmodel.DiaryViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Splash     : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main       : Screen("main")

    // Tela cheia normal do NavGraph (não é mais Dialog — bug fix SPEC 9.8-9.11:
    // Dialog escondia a Row de botões atrás da barra de gestos do sistema).
    object DiaryPhotoEditor : Screen("diary_photo_editor/{imageUri}") {
        fun createRoute(imageUri: Uri): String =
            "diary_photo_editor/${URLEncoder.encode(imageUri.toString(), "UTF-8")}"
    }
}

@Composable
fun PetCareNavGraph() {
    val navController  = rememberNavController()
    val appViewModel: AppViewModel = hiltViewModel()
    val isOnboardingDone by appViewModel.isOnboardingDone.collectAsState()
    val isReady          by appViewModel.isReady.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                isReady    = isReady,
                onNavigate = {
                    val dest = if (isOnboardingDone) Screen.Main.route else Screen.Onboarding.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        // Seção 6 — casca de navegação global (5 abas + FAB do Mel)
        // O conteúdo real de cada aba virá nas seções 7-14.
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToDiaryPhotoEditor = { imageUri ->
                    navController.navigate(Screen.DiaryPhotoEditor.createRoute(imageUri))
                },
            )
        }

        // Editor de fotos do Diário — tela cheia normal (bug fix SPEC 9.8-9.11:
        // era um Dialog, cuja janela separada escondia a Row de botões atrás da
        // barra de gestos do sistema mesmo com systemBarsPadding aplicado).
        composable(
            route = Screen.DiaryPhotoEditor.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri").orEmpty()
            val imageUri = Uri.parse(URLDecoder.decode(encodedUri, "UTF-8"))
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            val pets by diaryViewModel.pets.collectAsState()

            DiaryPhotoEditorScreen(
                imageUri = imageUri,
                pets = pets,
                onDismiss = { navController.popBackStack() },
                onSave = { petId, photoPath, caption ->
                    diaryViewModel.addEntry(petId = petId, photoPath = photoPath, caption = caption)
                    navController.popBackStack()
                },
            )
        }
    }
}
