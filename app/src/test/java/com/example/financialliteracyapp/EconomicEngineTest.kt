package com.example.financialliteracyapp

import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.data.local.entity.ShopEntity
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.domain.economy.EconomicEngine
import org.junit.Assert.*
import org.junit.Test

/** Unit-тесты экономического движка: боты не в минус, лимонад не продаётся из пустоты. */
class EconomicEngineTest {

    @Test
    fun botWallet_neverNegative_afterDay() {
        val shop = ShopEntity(price = 8, costPrice = 3, stock = 100)
        val bots = List(20) { i -> BotEntity(id = (i + 1).toLong(), salary = 800, wallet = 200) }

        val (updated, result) = EconomicEngine.simulateBotPurchases(shop, bots)

        updated.forEach { bot ->
            assertTrue("бот ${bot.id} не должен уйти в минус", bot.wallet >= 0)
        }
        assertTrue("должны были быть продажи", result.sold > 0)
        assertTrue("выручка ≥ 0", result.revenue >= 0)
    }

    @Test
    fun soldNeverExceedsStock() {
        val shop = ShopEntity(price = 8, costPrice = 3, stock = 3)
        val bots = List(20) { i -> BotEntity(id = (i + 1).toLong(), salary = 800, wallet = 200) }

        val (_, result) = EconomicEngine.simulateBotPurchases(shop, bots)

        assertTrue("sold ≤ stock", result.sold <= 3)
        assertTrue("lostNoStock + sold = посетители", result.lostNoStock + result.sold + result.lostNoMoney + result.lostHighPrice == result.visitors)
    }

    @Test
    fun botWithoutMoney_cannotBuy() {
        val shop = ShopEntity(price = 8, costPrice = 3, stock = 100)
        val bots = List(20) { i -> BotEntity(id = (i + 1).toLong(), salary = 800, wallet = 0) }

        val (updated, result) = EconomicEngine.simulateBotPurchases(shop, bots)

        assertEquals(0, result.sold)
        assertEquals(20, result.lostNoMoney)
        updated.forEach { assertTrue(it.wallet >= 0) }
    }

    @Test
    fun noStock_nothingSold() {
        val shop = ShopEntity(price = 8, costPrice = 3, stock = 0)
        val bots = List(20) { i -> BotEntity(id = (i + 1).toLong(), salary = 800, wallet = 200) }

        val (_, result) = EconomicEngine.simulateBotPurchases(shop, bots)

        assertEquals(0, result.sold)
        assertEquals(20, result.lostNoStock)
    }

    @Test
    fun resultsAccounting_consistent() {
        val shop = ShopEntity(price = 8, costPrice = 3, stock = 100)
        val bots = List(20) { i -> BotEntity(id = (i + 1).toLong(), salary = 800, wallet = 200) }

        val (_, result) = EconomicEngine.simulateBotPurchases(shop, bots)

        assertEquals(result.sold * 8, result.revenue)
        assertEquals(result.sold * 3, result.cost)
        assertEquals(Balance.SHOP_RENT_MARKET, result.rent)
        val expectedTax = (result.revenue * Balance.TAX_RATE).toInt()
        assertEquals(expectedTax, result.tax)
        assertEquals(result.revenue - result.cost - result.rent - result.tax, result.profit)
    }

    @Test
    fun simulateDays_runs_30DaysWithoutBreaking() {
        val shop = ShopEntity(price = 8, costPrice = 3, stock = 100)
        val bots = List(20) { i -> BotEntity(id = (i + 1).toLong(), salary = 800, wallet = 200) }

        val results = EconomicEngine.simulateDays(shop, bots, days = 30)

        assertEquals(30, results.size)
        results.forEach { res ->
            assertTrue("sold ≥ 0", res.sold >= 0)
            assertTrue("посетители > 0", res.visitors == 20)
        }
    }
}