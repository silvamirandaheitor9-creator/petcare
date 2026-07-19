package com.petcare.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.petcare.app.ui.screen.SplashScreen
import com.petcare.app.ui.screen.main.MainScreen
import com.petcare.app.ui.screen.main.diary.DiaryAddEntryScreen
import com.petcare.app.ui.screen.main.pets.NewPetScreen
import com.petcare.app.ui.screen.main.pets.PetDetailScreen
import com.petcare.app.ui.screen.main.pets.PetPhotoEditorScreen
import com.petcare.app.ui.screen.main.reminders.NewReminderScreen
import com.petcare.app.ui.screen.onboarding.OnboardingScreen
import com.petcare.app.ui.viewmodel.AppViewModel
import com.petcare.app.ui.viewmodel.DiaryViewModel
import com.petcare.app.ui.viewmodel.NewPetViewModel
import com.petcare.app.ui.viewmodel.NewReminderViewModel
import com.petcare.app.ui.viewmodel.PetDetailViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Splash     : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main       : Screen("main")

    // Tela de nova entrada do Diário — galeria → prévia + legenda + pet → salvar.
    object DiaryAddEntry : Screen("diary_add_entry/{imageUri}") {
        fun createRoute(imageUri: Uri): String =
            "diary_add_entry/${URLEncoder.encode(imageUri.toString(), "UTF-8")}"
    }

    // Tela cheia normal do NavGraph — formulário "Novo Pet" (SPEC §11 — parte 1).
    object NewPet : Screen("new_pet")

    // Tela cheia normal do NavGraph — cortar a foto de perfil do pet
    // (SPEC §11 — parte 2), empilhada por cima do formulário "Novo Pet".
    object PetPhotoEditor : Screen("pet_photo_editor/{imageUri}") {
        fun createRoute(imageUri: Uri): String =
            "pet_photo_editor/${URLEncoder.encode(imageUri.toString(), "UTF-8")}"
    }

    // Tela cheia — Novo Lembrete / Editar Lembrete (SPEC §10 — Parte 1).
    // reminderId = -1 → criar novo; > 0 → editar existente.
    object NewReminder : Screen("new_reminder/{reminderId}") {
        fun createRoute(reminderId: Long = -1L): String = "new_reminder/$reminderId"
    }

    // Tela cheia — Detalhe do pet com sub-abas de saúde (SPEC §12 — Parte 1).
    object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: Long): String = "pet_detail/$petId"
    }
}

@Composable
fun PetCareNavGraph() {
    val navController = rememberNavController()
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

        // Seção 6 — casca de navegação global (5 abas)
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToDiaryPhotoEditor = { imageUri ->
                    navController.navigate(Screen.DiaryAddEntry.createRoute(imageUri))
                },
                onNavigateToNewPet = {
                    navController.navigate(Screen.NewPet.route)
                },
                onNavigateToNewReminder = { reminderId ->
                    navController.navigate(Screen.NewReminder.createRoute(reminderId))
                },
                onNavigateToPetDetail = { petId ->
                    navController.navigate(Screen.PetDetail.createRoute(petId))
                },
            )
        }

        // Formulário "Novo Pet" — tela cheia normal.
        composable(Screen.NewPet.route) { entry ->
            val newPetViewModel: NewPetViewModel = hiltViewModel(entry)
            NewPetScreen(
                viewModel = newPetViewModel,
                onDismiss = { navController.popBackStack() },
                onSaved   = { navController.popBackStack() },
                onNavigateToPhotoEditor = { imageUri ->
                    navController.navigate(Screen.PetPhotoEditor.createRoute(imageUri))
                },
            )
        }

        // Editor de foto do pet (crop/girar) — empilhado sobre "Novo Pet".
        composable(
            route     = Screen.PetPhotoEditor.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri").orEmpty()
            val imageUri   = Uri.parse(URLDecoder.decode(encodedUri, "UTF-8"))
            val newPetEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.NewPet.route)
            }
            val newPetViewModel: NewPetViewModel = hiltViewModel(newPetEntry)

            PetPhotoEditorScreen(
                imageUri  = imageUri,
                onDismiss = { navController.popBackStack() },
                onSave    = { photoPath ->
                    newPetViewModel.setPhotoPath(photoPath)
                    navController.popBackStack()
                },
            )
        }

        // Nova entrada do Diário — galeria → prévia + legenda + seletor de pet → salvar.
        composable(
            route     = Screen.DiaryAddEntry.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encodedUri   = backStackEntry.arguments?.getString("imageUri").orEmpty()
            val imageUri     = Uri.parse(URLDecoder.decode(encodedUri, "UTF-8"))
            val diaryViewModel: DiaryViewModel = hiltViewModel()
            val pets by diaryViewModel.pets.collectAsState()

            DiaryAddEntryScreen(
                imageUri  = imageUri,
                pets      = pets,
                onDismiss = { navController.popBackStack() },
                onSave    = { petId, photoPath, caption ->
                    diaryViewModel.addEntry(petId = petId, photoPath = photoPath, caption = caption)
                    navController.popBackStack()
                },
            )
        }

        // Novo Lembrete / Editar Lembrete (SPEC §10 — Parte 1).
        composable(
            route     = Screen.NewReminder.route,
            arguments = listOf(
                navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L }
            ),
        ) { backStackEntry ->
            val reminderId           = backStackEntry.arguments?.getLong("reminderId") ?: -1L
            val newReminderViewModel: NewReminderViewModel = hiltViewModel(backStackEntry)

            NewReminderScreen(
                reminderId = reminderId,
                viewModel  = newReminderViewModel,
                onDismiss  = { navController.popBackStack() },
                onSaved    = { navController.popBackStack() },
            )
        }

        // Detalhe do pet — sub-abas de saúde (SPEC §12 — Parte 1).
        composable(
            route     = Screen.PetDetail.route,
            arguments = listOf(navArgument("petId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val petDetailViewModel: PetDetailViewModel = hiltViewModel(backStackEntry)
            PetDetailScreen(
                viewModel   = petDetailViewModel,
                onBack      = { navController.popBackStack() },
                onDeletePet = { navController.popBackStack() },
            )
        }
    }
}
