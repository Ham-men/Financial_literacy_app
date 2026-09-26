package com.example.financialliteracyapp.game

import android.content.Context
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.prefs.UserPrefs
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Пассивный доход нанятых сотрудников (v5): каждые AUTO_CASHIER_BOT_EVERY_MINUTES игровых минут,
 * пока магазин открыт, каждый магазин с нанятым сотрудником продаёт 1 единицу товара.
 *
 * Работает фоном независимо от того, какую сцену смотрит игрок, и добегает пропущенное
 * время после закрытия/перезапуска приложения (GameClock реконструирует минуту из elapsed).
 * Новый день (минута побежала назад) — продажи сбрасываются.
 */
object PassiveSales {

    @Volatile private var started = false
    private var scope: CoroutineScope? = null
    private var prefs: UserPrefs? = null

    /** Запускается один раз вместе с GameClock. */
    fun start(context: Context) {
        if (started) return
        started = true
        prefs = AppContainer.prefs(context)
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope!!.launch {
            val repo = AppContainer.repo(context)
            val prefs = this@PassiveSales.prefs!!
            var first = true
            var lastProcessed = GameRules.WORK_DAY_START_MINUTE

            GameClock.minute.collect { minute ->
                if (first) {
                    first = false
                    lastProcessed = prefs.lastPassiveMinute.first().let { it.ifNegative(minute) }
                    if (lastProcessed != minute) {
                        processTick(lastProcessed, minute, repo, prefs)
                        lastProcessed = minute
                        prefs.setLastPassiveMinute(lastProcessed)
                    }
                    return@collect
                }

                if (minute < lastProcessed) {
                    // Наступил новый день (10:00) — продажи за предыдущий день завершены
                    lastProcessed = minute
                    prefs.setLastPassiveMinute(lastProcessed)
                    return@collect
                }

                processTick(lastProcessed, minute, repo, prefs)
                lastProcessed = minute
                prefs.setLastPassiveMinute(lastProcessed)
            }
        }
    }

    private suspend fun processTick(
        fromMinute: Int,
        toMinute: Int,
        repo: com.example.financialliteracyapp.data.GameRepository,
        prefs: UserPrefs
    ) {
        val hired = prefs.hiredBuildings.first()
        if (hired.isEmpty()) return
        val step = GameRules.AUTO_CASHIER_BOT_EVERY_MINUTES

        // Первая граница продажи: ближайший кратный шагу момент СТРОГО ПОСЛЕ fromMinute.
        // Иначе при сдвиге минуты на 1 (живая игра) продажа никогда не сработает.
        var tick = ((fromMinute / step) + 1) * step
        while (tick <= toMinute) {
            if (GameRules.isShopOpen(tick)) {
                for (buildingId in hired) {
                    repo.passiveSaleInBuilding(buildingId)
                }
            }
            tick += step
        }
    }

    private fun Int.ifNegative(fallback: Int): Int = if (this >= 0) this else fallback
}