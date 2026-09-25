package com.example.financialliteracyapp.domain.economy

/**
 * Чистые правила экономики: единый источник инвариантов.
 * Используются репозиторием и покрываются unit-тестами.
 */
object GameRules {

    // --- Правило: план ≤ бюджет, кошелёк не уходит в минус ---
    data class Allocation(
        val need: Int,
        val want: Int,
        val save: Int,
        val cashRemainder: Int
    ) {
        val planned: Int get() = need + want + save
    }

    /** Решает распределение по 3 банкам так, что need+want+save ≤ total, остаток ≥ 0. */
    fun solvePlan(total: Int, needPlan: Int, wantPlan: Int, savePlan: Int): Allocation {
        val n = needPlan.coerceIn(0, total)
        val w = wantPlan.coerceIn(0, total - n)
        val s = savePlan.coerceIn(0, total - n - w)
        return Allocation(n, w, s, (total - n - w - s).coerceAtLeast(0))
    }

    /** Правило «нельзя уйти в минус»: вернёт остаток, если хватает, иначе null. */
    fun spend(cash: Int, cost: Int): Int? = if (cash >= cost) cash - cost else null

    /** Монеты для мешка: сколько целых монет номинала 50 и остаток. */
    fun bagCoins(cash: Int, denom: Int = 50): Pair<Int, Int> {
        val safe = cash.coerceAtLeast(0)
        return (safe / denom) to (safe % denom)
    }

    /** Процент по копилке за день: возвращается в копилку. */
    fun saveInterest(save: Int, percent: Int = 5): Int =
        save.coerceAtLeast(0) * percent / 100

    // --- Правило: уровни развлечений (игрушки Финни) ---
    const val XP_PER_LEVEL = 5        // 5 опыта на уровень
    const val MAX_PLAY_LEVEL = 5      // до уровня 5 включительно

    /** Уровень развлечений из опыта: 5 опыта = 1 уровень. */
    fun playLevel(xp: Int): Int = (xp / XP_PER_LEVEL).coerceIn(1, MAX_PLAY_LEVEL)

    /** Закончено ли до следующего уровня: прогресс 0..1. */
    fun playProgress(xp: Int): Float =
        (xp % XP_PER_LEVEL).toFloat() / XP_PER_LEVEL

    /** Процент по копилке: базовый 5% + 1% за уровень развлечений (уровень 5 → 9%). */
    fun playInterestPercent(level: Int): Int =
        Balance.SAVE_INTEREST_PERCENT + (level.coerceIn(1, MAX_PLAY_LEVEL) - 1)

    // --- Правило: копилка (цель) не уходит в минус и не превышает цель ---
    fun capGoalDeposit(current: Int, target: Int, amount: Int): Int =
        (current + amount).coerceAtMost(target)

    fun capGoalWithdraw(current: Int, amount: Int): Int =
        (current - amount).coerceAtLeast(0)

    // --- Правило: рост Финни: 1=Малыш, 2=Подросток, 3=Хозяин ларька ---
    const val MAX_GROWTH_LEVEL = 3

    fun clampLevel(level: Int): Int = level.coerceIn(1, MAX_GROWTH_LEVEL)

    fun growthStageName(level: Int): String = when (clampLevel(level)) {
        2 -> "Подросток"
        3 -> "Хозяин ларька"
        else -> "Малыш"
    }

    // --- Правило: начисление всегда с источником (категория дохода из белого списка) ---
    val KNOWN_INCOME_SOURCES = setOf(
        "start_gift",
        "daily_allowance",
        "quest_reward",
        "sales",
        "shop_sale",
        "bot_sales",
        "lot_sale",
        "goal_withdraw",
        "save_interest"
    )

    /** Доход обязан иметь источник; расход не ограничен. */
    fun isIncomeWithSource(kind: String, category: String): Boolean =
        kind != "INCOME" || category in KNOWN_INCOME_SOURCES

    // --- Правило: игровое время. 1 игровая минута = 1 реальная секунда ---
    const val WORK_DAY_START_MINUTE = 600   // 10:00
    const val WORK_DAY_END_MINUTE = 1080    // 18:00
    const val BED_TIME_MINUTE = 1380        // 23:00 — время останавливается

    /** 18:00–23:00 — магазины закрыты, боты в них не ходят. */
    fun isShopOpen(gameMinute: Int): Boolean =
        gameMinute in WORK_DAY_START_MINUTE until WORK_DAY_END_MINUTE

    /** Спать можно с 18:00 до 23:00 включительно (в 23:00 время останавливается). */
    fun canSleep(gameMinute: Int): Boolean =
        gameMinute >= WORK_DAY_END_MINUTE && gameMinute <= BED_TIME_MINUTE

    /** Время остановилось в 23:00 — пора ложиться спать. */
    fun isBedtime(gameMinute: Int): Boolean = gameMinute >= BED_TIME_MINUTE

    /** "10:00" из номера минуты дня. */
    fun timeLabel(gameMinute: Int): String {
        val m = gameMinute.coerceIn(0, 1439)
        return "%02d:%02d".format(m / 60, m % 60)
    }

    /** Дата дня 1 = 01.01.2020. */
    fun dateForDay(day: Int): String =
        java.time.LocalDate.of(2020, 1, 1).plusDays((day - 1).toLong())
            .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}