package com.petcare.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_DARK_THEME        = booleanPreferencesKey("dark_theme")
        private val KEY_ONBOARDING_DONE   = booleanPreferencesKey("onboarding_done")
        private val KEY_TERMS_ACCEPTED    = booleanPreferencesKey("terms_accepted")
        private val KEY_USER_NAME         = stringPreferencesKey("user_name")
        /**
         * Contador de vagas extras desbloqueadas via rewarded ad (SPEC §18.4).
         * Começa em 0; cada anúncio assistido soma +5.
         * Chave nova ("extra_pet_slots_count") para evitar ClassCastException
         * em instalações que tinham a flag booleana anterior ("extra_pet_slots").
         */
        private val KEY_PET_SLOTS_COUNT   = intPreferencesKey("extra_pet_slots_count")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_DARK_THEME] ?: false }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_ONBOARDING_DONE] ?: false }

    val isTermsAccepted: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_TERMS_ACCEPTED] ?: false }

    val userName: Flow<String> = context.dataStore.data
        .map { it[KEY_USER_NAME] ?: "" }

    /**
     * Número de vagas extras acumuladas via rewarded ads (SPEC §18.4).
     * 0 = nenhum anúncio assistido; 5 = 1 anúncio; 10 = 2 anúncios; etc.
     */
    val extraSlotsCount: Flow<Int> = context.dataStore.data
        .map { it[KEY_PET_SLOTS_COUNT] ?: 0 }

    /**
     * Soma [bonus] ao contador atual de vagas extras e persiste no DataStore.
     * Chamada com [bonus] = 5 a cada rewarded ad concluído.
     */
    suspend fun addExtraSlots(bonus: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_PET_SLOTS_COUNT] ?: 0
            prefs[KEY_PET_SLOTS_COUNT] = current + bonus
        }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = dark }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setTermsAccepted(accepted: Boolean) {
        context.dataStore.edit { it[KEY_TERMS_ACCEPTED] = accepted }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }
}
