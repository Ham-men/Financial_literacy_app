package com.example.financialliteracyapp.domain.economy

import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.data.local.entity.ShopEntity
import kotlin.math.min
import kotlin.random.Random

/**
 * Экономический движок MVP. Часть 2 из сюжет 2.txt.
 * Шаги: расходы ботов → пересчёт магазина → агрегаты (упрощено до 1 района).
 */
object EconomicEngine {

    data class DayResult(
        val sold: Int,
        val revenue: Int,
        val cost: Int,
        val rent: Int,
        val tax: Int,
        val profit: Int,
        val visitors: Int,
        val lostNoMoney: Int,
        val lostHighPrice: Int,
        val lostNoStock: Int
    )

    /**
     * Симуляция 20 ботов WORKER за 1 день.
     * Каждый бот решает покупать 1 лимонад, если хватает денег и score >= порога.
     */
    fun simulateBotPurchases(
        shop: ShopEntity,
        bots: List<BotEntity>,
        basePrice: Double = Balance.LEMONADE_BASE_PRICE.toDouble()
    ): Pair<List<BotEntity>, DayResult> {
        var stock = shop.stock
        var visitors = 0
        var sold = 0
        var lostHighPrice = 0
        var lostNoMoney = 0
        var lostNoStock = 0
        val updatedBots = bots.map { it.copy() }.toMutableList()

        for (i in bots.indices) {
            val bot = updatedBots[i]
            visitors++
            if (stock <= 0) {
                lostNoStock++
                // лояльность падает, если пришёл и нет товара
                updatedBots[i] = bot.copy(loyalty = (bot.loyalty * 0.5f).coerceIn(0f, 1f))
                continue
            }
            if (bot.wallet < shop.price) {
                lostNoMoney++
                continue
            }
            val offer = BotBrain.ShopOffer(
                shopId = shop.id,
                price = shop.price.toDouble(),
                quality01 = 0.5,
                distance01 = 0.5 + Random.nextDouble(-0.1, 0.1),
                loyalty01 = bot.loyalty.toDouble(),
                stockUnits = stock,
                brandLevel01 = 0.2
            )
            val score = BotBrain.shopScore(offer, basePrice, bot.salary.toDouble())
            val budget = bot.wallet.toDouble() // упрощено: без savings*0.2
            val chosen = BotBrain.decidePurchase(listOf(offer), basePrice, bot.salary.toDouble(), budget)
            if (chosen == null || score < Balance.PURCHASE_THRESHOLD) {
                lostHighPrice++
                continue
            }
            // покупка
            stock--
            sold++
            val newWallet = (bot.wallet - shop.price).coerceAtLeast(0)
            val newLoyalty = min(1f, bot.loyalty + 0.05f)
            updatedBots[i] = bot.copy(wallet = newWallet, loyalty = newLoyalty)
        }

        val revenue = sold * shop.price
        val cost = sold * shop.costPrice
        val rent = Balance.SHOP_RENT_MARKET
        val tax = (revenue * Balance.TAX_RATE).toInt()
        val profit = revenue - cost - rent - tax

        return updatedBots to DayResult(
            sold = sold,
            revenue = revenue,
            cost = cost,
            rent = rent,
            tax = tax,
            profit = profit,
            visitors = visitors,
            lostNoMoney = lostNoMoney,
            lostHighPrice = lostHighPrice,
            lostNoStock = lostNoStock
        )
    }

    /** Проверка, что экономика не ломается за N дней (для тестов). */
    fun simulateDays(shop: ShopEntity, bots: List<BotEntity>, days: Int = 30): List<DayResult> {
        var curShop = shop
        var curBots = bots
        val results = mutableListOf<DayResult>()
        repeat(days) {
            val (newBots, res) = simulateBotPurchases(curShop, curBots)
            // пополняем ботов зарплатой и товар (упрощённо)
            curBots = newBots.map { it.copy(wallet = it.wallet + 100) }
            curShop = curShop.copy(
                stock = (curShop.stock - res.sold + 20).coerceAtLeast(0),
                soldToday = res.sold,
                revenueToday = res.revenue
            )
            results.add(res)
        }
        return results
    }
}
