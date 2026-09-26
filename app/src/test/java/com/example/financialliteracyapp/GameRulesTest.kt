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
        assertTrue(GameRules.isIncomeWithSource(kind = "INCOME", category = "lot_sale"))
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
        val used = setOf("start_gift", "daily_allowance", "quest_reward", "shop_sale", "bot_sales", "goal_withdraw", "lot_sale", "save_interest")
        assertTrue(GameRules.KNOWN_INCOME_SOURCES.containsAll(used))
    }

    // --- Правило 6: мешок раскладывается монетами по 50 + остаток ---

    @Test
    fun bagCoins_splitsInto50AndRemainder() {
        assertEquals(6 to 0, GameRules.bagCoins(300))
        assertEquals(1 to 20, GameRules.bagCoins(70))
        assertEquals(0 to 5, GameRules.bagCoins(5))
        assertEquals(0 to 0, GameRules.bagCoins(0))
    }

    @Test
    fun bagCoins_neverNegative() {
        for (cash in -50..500 step 7) {
            val (full, rem) = GameRules.bagCoins(cash)
            assertTrue(full >= 0)
            assertTrue(rem >= 0)
            assertTrue(rem < 50)
            assertEquals(cash.coerceAtLeast(0), full * 50 + rem)
        }
    }

    // --- Правило 7: проценты по копилке ---

    @Test
    fun saveInterest_accruesOnPiggyBank() {
        assertEquals(5, GameRules.saveInterest(100))
        assertEquals(2, GameRules.saveInterest(50))
        assertEquals(15, GameRules.saveInterest(300))
        assertEquals(0, GameRules.saveInterest(0))
    }

    @Test
    fun saveInterest_neverNegative() {
        for (save in -50..500 step 13) {
            assertTrue(GameRules.saveInterest(save) >= 0)
        }
    }

    // --- Правило 8: уровни развлечений (игрушки → опыт → уровень → ставка копилки) ---

    @Test
    fun playLevel_progressesBy5Xp() {
        assertEquals(1, GameRules.playLevel(0))
        assertEquals(1, GameRules.playLevel(4))
        assertEquals(1, GameRules.playLevel(5))   // 1 уровень = 5 опыта
        assertEquals(2, GameRules.playLevel(10))  // 2 уровень = 10 опыта
        assertEquals(3, GameRules.playLevel(15))
        assertEquals(4, GameRules.playLevel(20))
        assertEquals(5, GameRules.playLevel(25))
        assertEquals(5, GameRules.playLevel(999))
    }

    @Test
    fun playInterestPercent_risesWithLevel() {
        assertEquals(5, GameRules.playInterestPercent(1))
        assertEquals(6, GameRules.playInterestPercent(2))
        assertEquals(9, GameRules.playInterestPercent(5))
        // Вне границ уровень зажимается
        assertEquals(5, GameRules.playInterestPercent(0))
        assertEquals(9, GameRules.playInterestPercent(99))
    }

    @Test
    fun playLevel_savesMaxAtLevel5() {
        // 5 уровней × 5 опыта = 25 опыта максимум для копилки
        assertEquals(25, GameRules.MAX_PLAY_LEVEL * GameRules.XP_PER_LEVEL)
        val interestAtTop = GameRules.saveInterest(save = 100, percent = GameRules.playInterestPercent(GameRules.MAX_PLAY_LEVEL))
        assertEquals(9, interestAtTop)
    }

    @Test
    fun playProgress_isWithin0to1() {
        assertEquals(0f, GameRules.playProgress(0))
        assertEquals(0.6f, GameRules.playProgress(8), 0.001f)
        assertEquals(0f, GameRules.playProgress(10)) // 10 % 5 = 0
    }

    // --- Правило 9: игровое время (1 игр.мин = 1 реал.сек) ---

    @Test
    fun shopOpen_workingHours10to18() {
        // 10:00 = 600, 18:00 = 1080
        assertEquals(600, GameRules.WORK_DAY_START_MINUTE)
        assertEquals(1080, GameRules.WORK_DAY_END_MINUTE)
        assertFalse(GameRules.isShopOpen(599))
        assertTrue(GameRules.isShopOpen(600))
        assertTrue(GameRules.isShopOpen(900))
        assertTrue(GameRules.isShopOpen(1079))
        assertFalse(GameRules.isShopOpen(1080))  // ровно 18:00 — магазин закрывается
        assertFalse(GameRules.isShopOpen(1380))
    }

    @Test
    fun canSleep_window18to23() {
        // 23:00 = 1380
        assertEquals(1380, GameRules.BED_TIME_MINUTE)
        assertFalse(GameRules.canSleep(0))
        assertFalse(GameRules.canSleep(600))
        assertFalse(GameRules.canSleep(1079))
        assertTrue(GameRules.canSleep(1080))   // с 18:00 можно спать
        assertTrue(GameRules.canSleep(1200))
        assertTrue(GameRules.canSleep(1380))   // до 23:00 включительно
    }

    @Test
    fun isBedtime_after23() {
        assertFalse(GameRules.isBedtime(1379))
        assertTrue(GameRules.isBedtime(1380))
        assertTrue(GameRules.isBedtime(9999))  // часы остановлены на 23:00
    }

    @Test
    fun timeLabel_formats24h() {
        assertEquals("10:00", GameRules.timeLabel(600))
        assertEquals("11:59", GameRules.timeLabel(719))
        assertEquals("18:00", GameRules.timeLabel(1080))
        assertEquals("23:00", GameRules.timeLabel(1380))
        assertEquals("00:00", GameRules.timeLabel(0))
        assertEquals("12:34", GameRules.timeLabel(754))
    }

    @Test
    fun dateForDay_startsAt01012020() {
        assertEquals("01.01.2020", GameRules.dateForDay(1))
        assertEquals("02.01.2020", GameRules.dateForDay(2))
        assertEquals("31.12.2020", GameRules.dateForDay(366))
    }

    // --- Правило: «успешный день» (план-факт) ---

    @Test
    fun daySuccess_true_whenSavedAndStayedInPlan() {
        assertTrue(
            GameRules.daySuccess(
                needPlan = 200, wantPlan = 60, savePlan = 50,
                needFact = 150, wantFact = 60, saveFact = 50
            )
        )
    }

    @Test
    fun daySuccess_false_whenPlanMissed() {
        // потрачено больше запланированного в «желаемое»
        assertFalse(
            GameRules.daySuccess(
                needPlan = 200, wantPlan = 60, savePlan = 50,
                needFact = 150, wantFact = 80, saveFact = 50
            )
        )
    }

    @Test
    fun daySuccess_false_whenNothingSaved() {
        // в копилку ничего не отложено — день без накопления
        assertFalse(
            GameRules.daySuccess(
                needPlan = 200, wantPlan = 0, savePlan = 0,
                needFact = 150, wantFact = 0, saveFact = 0
            )
        )
    }

    @Test
    fun daySuccess_ignoresUnusedPlan() {
        // банка не запланирована вовсе — по ней план не оценивается
        assertTrue(
            GameRules.daySuccess(
                needPlan = 100, wantPlan = 0, savePlan = 40,
                needFact = 100, wantFact = 0, saveFact = 40
            )
        )
    }

    // --- Правило: рост Финни по привычкам ---

    @Test
    fun growthLevel_1_whenNoHabits() {
        assertEquals(1, GameRules.growthLevel(0, 0, 0))
        assertEquals(1, GameRules.growthLevel(199, 2, 3))
    }

    @Test
    fun growthLevel_2_whenHabitsAndSavings() {
        assertEquals(2, GameRules.growthLevel(200, 2, 3))
        assertEquals(2, GameRules.growthLevel(799, 4, 6))
    }

    @Test
    fun growthLevel_3_whenTopHabitsAndSavings() {
        assertEquals(3, GameRules.growthLevel(800, 4, 6))
        assertEquals(3, GameRules.growthLevel(1500, 10, 10))
    }

    @Test
    fun isMandatoryCategory_foodAndHeal() {
        assertTrue(GameRules.isMandatoryCategory("pet_food"))
        assertTrue(GameRules.isMandatoryCategory("pet_heal"))
        assertFalse(GameRules.isMandatoryCategory("shop_purchase"))
        assertFalse(GameRules.isMandatoryCategory("salary"))
    }
}