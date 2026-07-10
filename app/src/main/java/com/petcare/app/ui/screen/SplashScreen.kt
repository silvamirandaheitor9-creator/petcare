package com.petcare.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.petcare.app.ui.theme.OrangeGradEnd
import com.petcare.app.ui.theme.OrangeGradStart

// Placeholder — seção 4 (Splash animada) será implementada na tarefa dedicada
@Composable
fun SplashScreen(
    isReady: Boolean,
    onNavigate: () -> Unit,
) {
    LaunchedEffect(isReady) {
        if (isReady) onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrangeGradStart),
        contentAlignment = Alignment.Center,
    ) {
        // Animação completa será adicionada na seção 4
    }
}
