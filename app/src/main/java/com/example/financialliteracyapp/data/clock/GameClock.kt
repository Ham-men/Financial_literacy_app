package com.example.financialliteracyapp.data.clock

import android.content.Context
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.prefs.UserPrefs
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Игровые часы: 1 игровая минута = 1 реальная секунда.
 * День начинается в 10:00 (WORK_DAY_START), рабочий день до 18:00,
 * в 23:00 время останавливается — нужно лечь спать (новый день в 10:00).
 */
object GameClock {

    private val _minute = MutableStateFlow(GameRules.WORK_DAY_START_MINUTE)
    val minute: StateFlow<Int> = _minute.asStateFlow()

    @Volatile private var started = false
    private var scope: CoroutineScope? = null
    private var prefs: UserPrefs? = null
    @Volatile private var demoMode = false

    /** Демо-режим: часы идут в 5 раз быстрее (1 игр.мин = 0.2 сек). */
    fun setDemoMode(on: Boolean) {
        demoMode = on
    }

    /** Запускает один раз: восстанавливает время из DataStore и тикает 1 игр.мин/сек. */
    fun start(context: Context) {
        if (started) return
        started = true
        prefs = AppContainer.prefs(context)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope!!.launch {
            val savedMinute = prefs!!.gameMinute.first()
            val savedEpoch = prefs!!.gameClockEpoch.first()
            demoMode = prefs!!.demoMode.first()
            // 1 игровая минута = 1 реальная секунда: добегаем до сохранённого времени, пока игра была закрыта.
            val elapsed = ((System.currentTimeMillis() - savedEpoch) / 1_000L).toInt().coerceAtLeast(0)
            _minute.value = (savedMinute + elapsed).coerceAtMost(GameRules.BED_TIME_MINUTE)
            var saveCounter = 0
            while (true) {
                delay(if (demoMode) 200 else 1_000)
                val cur = _minute.value
                if (cur >= GameRules.BED_TIME_MINUTE) continue // 23:00 — время остановилось
                _minute.value = cur + 1
                saveCounter++
                if (saveCounter % 300 == 0) { // экономии ради — раз в 5 реальных минут
                    prefs!!.setGameClock(_minute.value, System.currentTimeMillis())
                }
            }
        }
    }

    /** Новый день: время сбрасывается на 10:00. Вызывается при «лечь спать» / «Следующий день». */
    suspend fun newDay() {
        _minute.value = GameRules.WORK_DAY_START_MINUTE
        prefs?.setGameClock(GameRules.WORK_DAY_START_MINUTE, System.currentTimeMillis())
    }

    /** Чит для теста: установить время дня вручную. */
    suspend fun setMinute(minute: Int) {
        val clamped = minute.coerceIn(0, GameRules.BED_TIME_MINUTE)
        _minute.value = clamped
        prefs?.setGameClock(clamped, System.currentTimeMillis())
    }
}