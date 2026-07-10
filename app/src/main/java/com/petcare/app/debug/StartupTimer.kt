package com.petcare.app.debug

import android.os.SystemClock
import androidx.compose.runtime.mutableStateListOf

/**
 * DEBUG TEMPORÁRIO — instrumentação de tempo de startup para localizar onde
 * os ~6,5s da splash estão sendo gastos (mascote/texto não aparecem
 * visualmente). Mostra marcas de tempo (ms desde o carregamento da classe
 * Application) diretamente na tela da splash, sem depender de logcat/adb.
 *
 * Remover assim que a causa raiz for confirmada e corrigida.
 */
object StartupTimer {
    val processStartElapsed: Long = SystemClock.elapsedRealtime()

    val marks = mutableStateListOf<Pair<String, Long>>()

    @Synchronized
    fun mark(label: String) {
        val elapsed = SystemClock.elapsedRealtime() - processStartElapsed
        marks.add(label to elapsed)
        android.util.Log.d("PetCareStartupTiming", "$label: +${elapsed}ms")
    }
}
