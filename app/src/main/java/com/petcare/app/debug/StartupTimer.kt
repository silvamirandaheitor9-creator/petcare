package com.petcare.app.debug

import android.os.SystemClock
import androidx.compose.runtime.mutableStateListOf

/**
 * DEBUG TEMPORÁRIO — instrumentação de tempo de startup para localizar onde
 * os segundos de tela parada estão sendo gastos e confirmar a duração real
 * do assentamento da mola do mascote. Mostra marcas de tempo (ms desde o
 * carregamento da classe Application) diretamente na tela da splash, sem
 * depender de logcat/adb.
 *
 * Remover assim que os números forem confirmados.
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
