package com.example.financialliteracyapp

import com.example.financialliteracyapp.domain.economy.GameRules
import org.junit.Assert.*
import org.junit.Test

/** Unit-тесты Этапа 2: план ≤ бюджет, нет минуса, копилка, рост, источник дохода. */
class GameRulesTest {

    // --- Правило 1: план ≤ бюджет ---

    @Test
    fun plan_notExceedsBudget_whenInBounds() {
        val a = GameRules.solvePlan(total = 500, needPlan = 200, wantPlan = 150, savePlan = 100)
        assertEquals(200, a.need)
        assertEquals(150, a.want)
        assertEquals(100, a.save)
        assertEquals(450, a.planned)
        assertEquals(50, a.cashRemainder)
    }

    @Test
    fun plan_keepsRemainderPositive() {
        val a = GameRules.solvePlan(total = 500, needPlan = 100, wantPlan = 100, savePlan = 100)
        assertEquals(300, a.planned)
        assertEquals(200, a.cashRemainder)
    }

    @Test
    fun plan_clampsOverBudget_totalNeverNegative() {
        val a = GameRules.solvePlan(total = 500, needPlan = 1000, wantPlan = 1000, savePlan = 1000)
        assertTrue("план ≤ бюджет", a.planned <= 500)
        assertEquals(500, a.planned)
        assertEquals(0, a.cashRemainder)
    }

    @Test
    fun plan_clampsNegativeInputsToZero() {
        val a = GameRules.solvePlan(total = 500, needPlan = -50, wantPlan = 100, savePlan = 100)
        assertEquals(0, a.need)
        assertEquals(100, a.want)
        assertEquals(100, a.save)
        assertTrue(a.cashRemainder >= 0)
    }

    @Test
    fun plan_respectsPriority_orderIsStable() {
        // want/save обрезаются, если need съел весь бюджет
        val a = GameRules.solvePlan(total = 50, needPlan = 40, wantPlan = 40, savePlan = 40)
        assertEquals(40, a.need)
        assertEquals(10, a.want)
        assertEquals(0, a.save)
        assertEquals(0, a.cashRemainder)
    }

    @Test
    fun plan_regression_exhaustiveNoNegative() {
        for (total in 0..100 step 7) {
            for (n in 0..150 step 13) {
                for (w in 0..150 step 17) {
                    for (s in 0..150 step 19) {
                        val a = GameRules.solvePlan(total, n, w, s)
                        assertTrue("need не может быть отрицательным", a.need >= 0)
                        assertTrue("want не может быть отрицательным", a.want >= 0)
                        assertTrue("save не может быть отрицательным", a.save >= 0)
                        assertTrue("план ≤ бюджет", a.planned <= total.coerceAtLeast(0))
                        assertTrue("остаток ≥ 0", a.cashRemainder >= 0)
                    }
                }
            }
        }
    }

    // --- Правило 2: нельзя уйти в минус ---

    @Test
    fun spend_allowsWhenAffordable() {
        assertEquals(90, GameRules.spend(100, 10))
        assertEquals(0, GameRules.spend(100, 100))
    }

    @Test
    fun spend_rejectsWhenInsufficient() {
        assertNull(GameRules.spend(100, 101))
        assertNull(GameRules.spend(0, 1))
        assertNull(GameRules.spend(10, 11))
    }

    @Test
    fun spend_neverReturnsNegative() {
        for (cash in 0..50) {
            for (cost in 0..100) {
                val rest = GameRules.spend(cash, cost) ?: continue
                assertTrue("остаток ≥ 0 при cash=$cash cost=$cost", rest >= 0)
            }
        }
    }

    // --- Правило 3: копилка (цель) ---

    @Test
    fun goalDeposit_capsAtTarget() {
        assertEquals(300, GameRules.capGoalDeposit(current = 200, target = 300, amount = 200))
        assertEquals(300, GameRules.capGoalDeposit(current = 300, target = 300, amount = 50))
        assertEquals(150, GameRules.capGoalDeposit(current = 100, target = 300, amount = 50))
    }

    @Test
    fun goalDeposit_neverExceedsTarget() {
        for (current in 0..500 step 11) {
            for (amount in 0..500 step 7) {
                val res = GameRules.capGoalDeposit(current, 300, amount)
                assertTrue("копилка ≤ цель", res <= 300)
                assertTrue("копилка ≥ 0", res >= 0)
            }
        }
    }

    @Test
    fun goalWithdraw_neverNegative() {
        assertEquals(250, GameRules.capGoalWithdraw(current = 300, amount = 50))
        assertEquals(0, GameRules.capGoalWithdraw(current = 300, amount = 500))
        assertEquals(0, GameRules.capGoalWithdraw(current = 50, amount = 60))
    }

    @Test
    fun goalWithdraw_rejectsOverWithdraw_viaSpendRule() {
        // снятие больше накопленного невозможно
        assertNull(GameRules.spend(cash = 300, cost = 400))
    }

    // --- Правило 4: рост Финни (1=Малыш → 2=Подросток → 3=Хозяин ларька) ---

    @Test
    fun growthStage_mapping() {
        assertEquals("Малыш", GameRules.growthStageName(1))
        assertEquals("Подросток", GameRules.growthStageName(2))
        assertEquals("Хозяин ларька", GameRules.growthStageName(3))
    }

    @Test
    fun growthStage_clampsOutOfBounds() {
        assertEquals(1, GameRules.clampLevel(0))
        assertEquals(1, GameRules.clampLevel(-5))
        assertEquals(GameRules.MAX_GROWTH_LEVEL, GameRules.clampLevel(99))
        assertEquals(2, GameRules.clampLevel(2))
    }

    @Test
    fun growthStage_neverUnknownForAnyInt() {
        for (level in -50..100) {
            val name = GameRules.growthStageName(level)
            assertTrue(name in setOf("Малыш", "Подросток", "Хозяин ларька"))
        }
    }

    // --- Правило 5: начисление с источником ---

    @Test
    fun income_requiresSource() {
        assertTrue(GameRules.isIncomeWithSource(kind = "INCOME", category = "daily_allowance"))
        assertTrue(GameRules.isIncomeWithSource(kind = "INCOME", category = "quest_reward"))
        assertTrue(GameRules.isIncomeWithSource(kind = "INCOME", category = "lot_rent"))
        // доход без известного источника — нарушение
        assertFalse(GameRules.isIncomeWithSource(kind = "INCOME", category = ""))
        assertFalse(GameRules.isIncomeWithSource(kind = "INCOME", category = "mystery_money"))
    }

    @Test
    fun expense_sourceNotRestricted() {
        assertTrue(GameRules.isIncomeWithSource(kind = "EXPENSE", category = "anything"))
        assertTrue(GameRules.isIncomeWithSource(kind = "EXPENSE", category = ""))
    }

    @Test
    fun knownIncomeSources_containAllUsedInApp() {
        val used = setOf("start_gift", "daily_allowance", "quest_reward", "shop_sale", "bot_sales", "goal_withdraw", "lot_rent", "lot_sale")
        assertTrue(GameRules.KNOWN_INCOME_SOURCES.containsAll(used))
    }
}