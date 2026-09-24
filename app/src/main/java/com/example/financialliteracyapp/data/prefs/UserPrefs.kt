package com.example.financialliteracyapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

/** DataStore: имя питомца, звук, сложность, онбординг. План День 2. */
class UserPrefs(private val context: Context) {

    val petName: Flow<String> = context.dataStore.data.map { it[PET_NAME] ?: "Енот Копилкин" }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND] ?: true }
    val difficulty: Flow<Int> = context.dataStore.data.map { it[DIFFICULTY] ?: 1 }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING] ?: false }
    val currentDay: Flow<Int> = context.dataStore.data.map { it[DAY] ?: 1 }
    val cashierHired: Flow<Boolean> = context.dataStore.data.map { it[CASHIER] ?: false }
    val lotPurchased: Flow<Boolean> = context.dataStore.data.map { it[LOT_PURCHASED] ?: false }
    val lotType: Flow<Int> = context.dataStore.data.map { it[LOT_TYPE] ?: 0 }
    val lotPlotId: Flow<String> = context.dataStore.data.map { it[LOT_PLOT_ID] ?: "" }

    suspend fun setPetName(name: String) {
        context.dataStore.edit { it[PET_NAME] = name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND] = enabled }
    }

    suspend fun setDifficulty(level: Int) {
        context.dataStore.edit { it[DIFFICULTY] = level }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[ONBOARDING] = done }
    }

    suspend fun setCurrentDay(day: Int) {
        context.dataStore.edit { it[DAY] = day }
    }

    suspend fun setCashierHired(hired: Boolean) {
        context.dataStore.edit { it[CASHIER] = hired }
    }

    suspend fun setLotPurchased(purchased: Boolean) {
        context.dataStore.edit { it[LOT_PURCHASED] = purchased }
    }

    suspend fun setLotType(type: Int) {
        context.dataStore.edit { it[LOT_TYPE] = type }
    }

    suspend fun setLotPlotId(plotId: String) {
        context.dataStore.edit { it[LOT_PLOT_ID] = plotId }
    }

    companion object {
        private val PET_NAME = stringPreferencesKey("pet_name")
        private val SOUND = booleanPreferencesKey("sound")
        private val DIFFICULTY = intPreferencesKey("difficulty")
        private val ONBOARDING = booleanPreferencesKey("onboarding_done")
        private val DAY = intPreferencesKey("current_day")
        private val CASHIER = booleanPreferencesKey("cashier_hired")
        private val LOT_PURCHASED = booleanPreferencesKey("lot_purchased")
        private val LOT_TYPE = intPreferencesKey("lot_type")
        private val LOT_PLOT_ID = stringPreferencesKey("lot_plot_id")
    }
}
