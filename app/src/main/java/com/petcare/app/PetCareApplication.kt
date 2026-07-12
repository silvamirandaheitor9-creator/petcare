package com.petcare.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.gms.ads.MobileAds
import com.petcare.app.data.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PetCareApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Registra os canais de notificação obrigatórios no Android 8+ (API 26+).
        // Sem isso o sistema descarta todas as notificações silenciosamente.
        NotificationChannels.createChannels(this)
        // MobileAds.initialize() faz I/O de disco síncrono na primeira
        // execução e pode levar segundos — se chamado direto aqui, ele
        // bloqueia o main thread ANTES da MainActivity/Compose existirem,
        // deixando a tela presa no windowBackground nativo (laranja) por
        // vários segundos, sem nenhum frame do Compose sendo desenhado.
        // Por isso a inicialização roda em background, fora do caminho
        // síncrono de startup do app.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            MobileAds.initialize(this@PetCareApplication)
        }
    }
}
