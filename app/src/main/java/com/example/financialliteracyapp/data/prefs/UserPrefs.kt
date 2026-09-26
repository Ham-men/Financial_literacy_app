package com.example.financialliteracyapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

/** DataStore: имя питомца, звук, сложность, онбординг. План День 2. */
class UserPrefs(private val context: Context) {

    val petName: Flow<String> = context.dataStore.data.map { it[PET_NAME] ?: "Енот Копилкин" }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND] ?: true }
    val difficulty: Flow<Int> = context.dataStore.data.map { it[DIFFICULTY] ?: 1 }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING] ?: false }
    val tutorialDone: Flow<Boolean> = context.dataStore.data.map { it[TUTORIAL] ?: false }
    val currentDay: Flow<Int> = context.dataStore.data.map { it[DAY] ?: 1 }
    val cashierHired: Flow<Boolean> = context.dataStore.data.map { it[CASHIER] ?: false }
    /** Здания, в которых нанят сотрудник (1 на здание). Хранятся id-строки. */
    val hiredBuildings: Flow<Set<Long>> = context.dataStore.data.map { prefs ->
        (prefs[HIRED_BUILDINGS] ?: emptySet()).mapNotNull { it.toLongOrNull() }.toSet()
    }
    val gameMinute: Flow<Int> = context.dataStore.data.map { it[GAME_MINUTE] ?: GameRules.WORK_DAY_START_MINUTE }
    val gameClockEpoch: Flow<Long> = context.dataStore.data.map { it[GAME_CLOCK_EPOCH] ?: System.currentTimeMillis() }
    /** Последняя обработанная минута пассивного дохода нанятых сотрудников (-1 = ещё не было). */
    val lastPassiveMinute: Flow<Int> = context.dataStore.data.map { it[LAST_PASSIVE_MINUTE] ?: -1 }
    /** Демо-режим для презентации: ускоренные часы, свободный сон, ежедневная карманная сумма. */
    val demoMode: Flow<Boolean> = context.dataStore.data.map { it[DEMO] ?: false }

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

    suspend fun setTutorialDone(done: Boolean) {
        context.dataStore.edit { it[TUTORIAL] = done }
    }

    suspend fun setCurrentDay(day: Int) {
        context.dataStore.edit { it[DAY] = day }
    }

    suspend fun setCashierHired(hired: Boolean) {
        context.dataStore.edit { it[CASHIER] = hired }
    }

    /** Найм/увольнение сотрудника в конкретном здании (1 на здание). */
    suspend fun setBuildingHired(buildingId: Long, hired: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[HIRED_BUILDINGS] ?: emptySet()).toMutableSet()
            if (hired) current.add(buildingId.toString()) else current.remove(buildingId.toString())
            prefs[HIRED_BUILDINGS] = current
        }
    }

    suspend fun setGameClock(minute: Int, epochMs: Long) {
        context.dataStore.edit {
            it[GAME_MINUTE] = minute
            it[GAME_CLOCK_EPOCH] = epochMs
        }
    }

    suspend fun setLastPassiveMinute(minute: Int) {
        context.dataStore.edit { it[LAST_PASSIVE_MINUTE] = minute }
    }

    suspend fun setDemoMode(on: Boolean) {
        context.dataStore.edit { it[DEMO] = on }
    }

    /** Полный сброс настроек (новый профиль). */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private val PET_NAME = stringPreferencesKey("pet_name")
        private val SOUND = booleanPreferencesKey("sound")
        private val DIFFICULTY = intPreferencesKey("difficulty")
        private val ONBOARDING = booleanPreferencesKey("onboarding_done")
        private val TUTORIAL = booleanPreferencesKey("tutorial_done")
        private val DAY = intPreferencesKey("current_day")
        private val CASHIER = booleanPreferencesKey("cashier_hired")
        private val HIRED_BUILDINGS = stringSetPreferencesKey("hired_buildings")
        private val GAME_MINUTE = intPreferencesKey("game_minute")
        private val GAME_CLOCK_EPOCH = longPreferencesKey("game_clock_epoch")
        private val LAST_PASSIVE_MINUTE = intPreferencesKey("last_passive_minute")
        private val DEMO = booleanPreferencesKey("demo_mode")
    }
}
