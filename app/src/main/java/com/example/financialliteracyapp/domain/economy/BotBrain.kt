package com.example.financialliteracyapp.domain.economy

import kotlin.math.max

/**
 * Мозги ботов v1 (MVP): только тип WORKER.
 * Формулы из «сюжет 2.txt», ч.1: WTP, priceScore, shopScore, decidePurchase.
 */
object BotBrain {

    // Веса для Рабочего: price 0.40, quality 0.05, distance 0.20, loyalty 0.10, stock 0.20, brand 0.05
    private const val W_PRICE = 0.40
    private const val W_QUALITY = 0.05
    private const val W_DISTANCE = 0.20
    private const val W_LOYALTY = 0.10
    private const val W_STOCK = 0.20
    private const val W_BRAND = 0.05

    fun willingnessToPay(basePrice: Double, income: Double): Double {
        if (income <= 0) return 0.0
        return basePrice * Math.pow(income / Balance.REF_INCOME, 0.7)
    }

    fun priceScore(price: Double, wtp: Double, priceSensitivity: Double = 1.0): Double {
        if (wtp <= 0) return 0.0
        return (1.0 - priceSensitivity * (price / wtp - 1.0)).coerceIn(0.0, 1.0)
    }

    data class ShopOffer(
        val shopId: Long,
        val price: Double,
        val quality01: Double = 0.5,
        val distance01: Double = 0.5, // 0 = далеко, 1 = рядом
        val loyalty01: Double = 0.0,
        val stockUnits: Int = 0,
        val brandLevel01: Double = 0.0 // 0..1
    )

    fun shopScore(offer: ShopOffer, basePrice: Double, income: Double): Double {
        val wtp = willingnessToPay(basePrice, income)
        val pScore = priceScore(offer.price, wtp)
        val qScore = (offer.quality01 * 0.8).coerceIn(0.0, 1.0)
        val dScore = offer.distance01.coerceIn(0.0, 1.0)
        val lScore = offer.loyalty01.coerceIn(0.0, 1.0)
        val sScore = max(0.0, max(0, offer.stockUnits).toDouble() / 5.0).coerceIn(0.0, 1.0)
        val bScore = offer.brandLevel01.coerceIn(0.0, 1.0)
        return W_PRICE * pScore + W_QUALITY * qScore + W_DISTANCE * dScore +
                W_LOYALTY * lScore + W_STOCK * sScore + W_BRAND * bScore
    }

    /** Возвращает id лучшего магазина или null, если все ниже порога. */
    fun decidePurchase(offers: List<ShopOffer>, basePrice: Double, income: Double, budget: Double): Long? {
        return offers
            .map { it to shopScore(it, basePrice, income) }
            .filter { (offer, score) -> score >= Balance.PURCHASE_THRESHOLD && offer.price <= budget }
            .maxByOrNull { it.second }
            ?.first?.shopId
    }

    /** Прогноз спроса для ползунка «Ценник» (заглушка v1, ML будет в v1.0). */
    fun estimateBuyers(price: Float): Int =
        (50 * (10f / price.coerceAtLeast(1f))).toInt().coerceIn(0, 100)
}
