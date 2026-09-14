package com.example.financialliteracyapp.data

import com.example.financialliteracyapp.data.local.AppDatabase
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.ShopEntity
import com.example.financialliteracyapp.data.local.entity.TransactionEntity
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.domain.economy.Balance

/** Репозиторий MVP: сиды + базовые операции. План День 2. */
class GameRepository(private val db: AppDatabase) {

    fun observePet() = db.petDao().observe()
    fun observeWallet() = db.walletDao().observe()
    fun observeShop() = db.shopDao().observe()
    fun observeTransactions() = db.transactionDao().observeAll()
    fun observeBots() = db.botDao().observeAll()

    suspend fun ensureSeed() {
        if (db.petDao().getOnce() == null) {
            db.petDao().upsert(PetEntity())
        }
        if (db.walletDao().getOnce() == null) {
            db.walletDao().upsert(WalletEntity())
        }
        if (db.shopDao().getOnce() == null) {
            db.shopDao().upsert(ShopEntity())
        }
        if (db.botDao().count() == 0) {
            val bots = List(20) { i ->
                BotEntity(id = (i + 1).toLong(), type = "WORKER")
            }
            db.botDao().upsertAll(bots)
        }
    }

    suspend fun feedPet(cost: Int = Balance.FEED_COST) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        if (wallet.cash < cost) return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash - cost))
        db.petDao().upsert(pet.copy(hunger = (pet.hunger + Balance.PET_FOOD_HUNGER_GAIN).coerceAtMost(100)))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "pet_food", amount = cost)
        )
    }

    suspend fun playWithPet(cost: Int = Balance.PLAY_COST) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        if (wallet.cash < cost) return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash - cost))
        db.petDao().upsert(pet.copy(mood = (pet.mood + Balance.PET_PLAY_MOOD_GAIN).coerceAtMost(100)))
    }

    suspend fun restPet() {
        val pet = db.petDao().getOnce() ?: PetEntity()
        db.petDao().upsert(pet.copy(energy = (pet.energy + Balance.PET_SLEEP_ENERGY_GAIN).coerceAtMost(100)))
    }

    // --- День 5: три банки ---
    suspend fun distributeBanks(spend: Int, save: Int, invest: Int) {
        val w = db.walletDao().getOnce() ?: WalletEntity()
        val total = w.cash + w.spend + w.save + w.invest
        // защита: сумма банок не больше total
        val s = spend.coerceIn(0, total)
        val sv = save.coerceIn(0, total - s)
        val inv = invest.coerceIn(0, total - s - sv)
        val newCash = (total - s - sv - inv).coerceAtLeast(0)
        db.walletDao().upsert(w.copy(cash = newCash, spend = s, save = sv, invest = inv))
    }

    // --- День 6: магазин ---
    suspend fun setShopPrice(price: Int) {
        val shop = db.shopDao().getOnce() ?: ShopEntity()
        db.shopDao().upsert(shop.copy(price = price.coerceIn(1, 30)))
    }

    suspend fun purchaseStock(supplierPricePerUnit: Double, units: Int) {
        val shop = db.shopDao().getOnce() ?: ShopEntity()
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        val cost = (supplierPricePerUnit * units).toInt()
        if (wallet.cash < cost) return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash - cost))
        db.shopDao().upsert(
            shop.copy(
                stock = shop.stock + units,
                costPrice = supplierPricePerUnit.toInt().coerceAtLeast(1),
                cash = shop.cash - cost
            )
        )
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "stock_purchase", amount = cost)
        )
    }

    suspend fun runDay(sold: Int) {
        val shop = db.shopDao().getOnce() ?: ShopEntity()
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        val actualSold = sold.coerceIn(0, shop.stock)
        val revenue = actualSold * shop.price
        val cost = actualSold * shop.costPrice
        val rent = Balance.SHOP_RENT_MARKET
        val tax = (revenue * Balance.TAX_RATE).toInt()
        val profit = revenue - cost - rent - tax
        db.shopDao().upsert(
            shop.copy(
                stock = shop.stock - actualSold,
                cash = shop.cash + revenue,
                soldToday = actualSold,
                revenueToday = revenue
            )
        )
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + profit.coerceAtLeast(0)))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "sales", amount = revenue)
        )
    }

    // --- День 11-14: боты + цикл ---
    suspend fun simulateBotDay(): com.example.financialliteracyapp.domain.economy.EconomicEngine.DayResult {
        val shop = db.shopDao().getOnce() ?: ShopEntity()
        val allBots = try {
            db.botDao().getAllOnce()
        } catch (e: Exception) {
            List(20) { i -> BotEntity(id = (i + 1).toLong()) }
        }
        val botsForSim = if (allBots.isEmpty()) List(20) { i -> BotEntity(id = (i + 1).toLong()) } else allBots

        val (updatedBots, result) = com.example.financialliteracyapp.domain.economy.EconomicEngine.simulateBotPurchases(shop, botsForSim)
        // сохраняем ботов
        db.botDao().upsertAll(updatedBots)
        // сохраняем магазин
        db.shopDao().upsert(
            shop.copy(
                stock = (shop.stock - result.sold).coerceAtLeast(0),
                cash = shop.cash + result.revenue,
                soldToday = result.sold,
                revenueToday = result.revenue
            )
        )
        // прибыль игроку
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + result.profit.coerceAtLeast(0)))
        // транзакции дня
        if (result.revenue > 0) {
            db.transactionDao().insert(TransactionEntity(kind = "INCOME", category = "bot_sales", amount = result.revenue))
        }
        if (result.cost > 0) {
            db.transactionDao().insert(TransactionEntity(kind = "EXPENSE", category = "cogs", amount = result.cost))
        }
        db.transactionDao().insert(TransactionEntity(kind = "EXPENSE", category = "rent", amount = result.rent))
        if (result.tax > 0) {
            db.transactionDao().insert(TransactionEntity(kind = "EXPENSE", category = "tax", amount = result.tax))
        }
        return result
    }

    suspend fun resetDay() {
        val shop = db.shopDao().getOnce() ?: return
        db.shopDao().upsert(shop.copy(soldToday = 0, revenueToday = 0))
        // пополняем ботов зарплатой для следующего дня (упрощённый цикл без работодателей)
        try {
            val bots = db.botDao().getAllOnce()
            if (bots.isNotEmpty()) {
                val replenished = bots.map { it.copy(wallet = (it.wallet + 100).coerceAtMost(2000)) }
                db.botDao().upsertAll(replenished)
            }
        } catch (_: Exception) {}
    }
}
