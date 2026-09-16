package com.example.financialliteracyapp

import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.domain.economy.BotBrain
import org.junit.Assert.*
import org.junit.Test

/** Unit-тесты мозга ботов: WTP, ценовой скор, порог покупки. */
class BotBrainTest {

    @Test
    fun willingnessToPay_isPositive_forWorker() {
        val wtp = BotBrain.willingnessToPay(Balance.LEMONADE_BASE_PRICE.toDouble(), income = 800.0)
        assertTrue(wtp > 0)
        assertTrue(wtp <= Balance.LEMONADE_BASE_PRICE.toDouble())
    }

    @Test
    fun willingnessToPay_zero_forNoIncome() {
        assertEquals(0.0, BotBrain.willingnessToPay(Balance.LEMONADE_BASE_PRICE.toDouble(), income = 0.0), 0.0)
        assertEquals(0.0, BotBrain.willingnessToPay(Balance.LEMONADE_BASE_PRICE.toDouble(), income = -1.0), 0.0)
    }

    @Test
    fun priceScore_inBounds() {
        for (price in 1..30) {
            for (wtp in 1..30) {
                val s = BotBrain.priceScore(price.toDouble(), wtp.toDouble())
                assertTrue("score в [0,1] при price=$price wtp=$wtp", s in 0.0..1.0)
            }
        }
    }

    @Test
    fun priceScore_cheaperThanWtp_isPositive() {
        val s = BotBrain.priceScore(price = 5.0, wtp = 10.0)
        assertEquals(1.0, s, 0.0)
    }

    @Test
    fun decidePurchase_rejectsAboveWtp() {
        val offer = BotBrain.ShopOffer(shopId = 1, price = 30.0, stockUnits = 0)
        val chosen = BotBrain.decidePurchase(
            offers = listOf(offer),
            basePrice = 8.0,
            income = 800.0,
            budget = 200.0
        )
        // price 30 сильно выше WTP ~5-6 → скор ниже порога → покупать не будут
        assertNull(chosen)
    }

    @Test
    fun decidePurchase_picksBestWithinBudget() {
        val offers = listOf(
            BotBrain.ShopOffer(shopId = 1, price = 10.0, stockUnits = 5, distance01 = 0.5),
            BotBrain.ShopOffer(shopId = 2, price = 6.0, stockUnits = 5, distance01 = 0.5),
            BotBrain.ShopOffer(shopId = 3, price = 9.0, stockUnits = 5, distance01 = 0.5)
        )
        val chosen = BotBrain.decidePurchase(
            offers = offers,
            basePrice = 8.0,
            income = 800.0,
            budget = 200.0
        )
        assertEquals(2L, chosen) // дешёвый → лучший скор
    }

    @Test
    fun estimateBuyers_inBounds() {
        for (price in 1..30) {
            val buyers = BotBrain.estimateBuyers(price.toFloat())
            assertTrue("buyers в [0,100] при price=$price", buyers in 0..100)
        }
    }

    @Test
    fun estimateBuyers_fallsWithHigherPrice() {
        assertTrue(BotBrain.estimateBuyers(15f) < BotBrain.estimateBuyers(5f))
    }
}