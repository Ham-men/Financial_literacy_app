package com.example.financialliteracyapp.domain.economy

/** Все константы баланса из «сюжет 2.txt», часть 3. Единый источник для UI и движка. */
object Balance {
    const val START_CAPITAL = 500
    const val TAX_RATE = 0.13

    const val FEED_COST = 20
    const val HEAL_COST = 30
    const val PLAY_COST = 10
    const val PET_FOOD_HUNGER_GAIN = 25
    const val PET_HEAL_MOOD_GAIN = 30
    const val PET_PLAY_MOOD_GAIN = 20
    const val PET_SLEEP_ENERGY_GAIN = 40
    const val PET_DECAY_PER_HOUR = 5

    const val LEMONADE_COST = 3
    const val LEMONADE_BASE_PRICE = 8
    const val SHOP_RENT_MARKET = 20

    const val CASHIER_SALARY = 80

    const val LOT_PRICE = 300
    const val LOT_SELL_PRICE = 200

    const val COIN_DENOM = 50
    const val SAVE_INTEREST_PERCENT = 5

    const val PURCHASE_THRESHOLD = 0.30
    const val REF_INCOME = 1500.0

    val SUPPLIERS = listOf(
        Supplier("magpie", 2.0, quality = 2, risk = "high"),
        Supplier("beaver", 3.0, quality = 5, risk = "low"),
        Supplier("fox", 2.5, quality = 4, risk = "medium")
    )

    data class Supplier(
        val id: String,
        val pricePerUnit: Double,
        val quality: Int,
        val risk: String
    )
}
