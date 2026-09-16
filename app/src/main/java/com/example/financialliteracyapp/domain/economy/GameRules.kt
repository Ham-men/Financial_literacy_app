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
        "lot_rent",
        "lot_sale",
        "goal_withdraw"
    )

    /** Доход обязан иметь источник; расход не ограничен. */
    fun isIncomeWithSource(kind: String, category: String): Boolean =
        kind != "INCOME" || category in KNOWN_INCOME_SOURCES
}