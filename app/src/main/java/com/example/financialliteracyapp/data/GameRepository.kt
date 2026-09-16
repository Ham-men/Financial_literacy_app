package com.example.financialliteracyapp.data

import com.example.financialliteracyapp.data.local.AppDatabase
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.data.local.entity.CatalogItemEntity
import com.example.financialliteracyapp.data.local.entity.GoalEntity
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import com.example.financialliteracyapp.data.local.entity.ShopEntity
import com.example.financialliteracyapp.data.local.entity.TransactionEntity
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.domain.economy.Balance
import kotlinx.coroutines.flow.first

/** Репозиторий MVP: сиды + базовые операции. План День 2. */
class GameRepository(private val db: AppDatabase) {

    fun observePet() = db.petDao().observe()
    fun observeWallet() = db.walletDao().observe()
    fun observeShop() = db.shopDao().observe()
    fun observeTransactions() = db.transactionDao().observeAll()
    fun observeBots() = db.botDao().observeAll()
    fun observeGoals() = db.goalDao().observeAll()
    fun observeQuests() = db.questDao().observeAll()
    fun observeCatalog() = db.catalogItemDao().observeAll()
    fun observeCatalogByCategory(category: String) = db.catalogItemDao().observeByCategory(category)

    suspend fun upsertPet(pet: PetEntity) = db.petDao().upsert(pet)

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
        // Seed goals (3 цели: 300, 800, 1500)
        if (db.goalDao().observeAll().first().isEmpty()) {
            val goals = listOf(
                GoalEntity(title = "Мячик для Финни", targetAmount = 300, currentAmount = 0, order = 0),
                GoalEntity(title = "Палатка в парке", targetAmount = 800, currentAmount = 0, order = 1),
                GoalEntity(title = "Набор художника", targetAmount = 1500, currentAmount = 0, order = 2)
            )
            db.goalDao().upsertAll(goals)
        }
        // Seed quests (6 заданий на 3 темы)
        if (db.questDao().observeAll().first().isEmpty()) {
            val quests = listOf(
                // Планирование
                QuestEntity(topic = "PLANNING", title = "Три банки", description = "Разложи 300 ₡ так, чтобы хватило на корм и хоть что-то в копилку", reward = 50, order = 0),
                QuestEntity(topic = "PLANNING", title = "Непредвиденный расход", description = "Финни порвал подстилку, нужно 50 ₡. Откуда взять: из желаемого или из копилки?", reward = 50, order = 1),
                // Сбережения
                QuestEntity(topic = "SAVING", title = "Цель ближе", description = "Положи в копилку не меньше 50 ₡, увидь, как шкала цели подросла", reward = 50, order = 2),
                QuestEntity(topic = "SAVING", title = "Не снимай сразу", description = "Соблазн купить торт из копилки; если снимаешь — подтверждаешь и видишь сдвиг срока", reward = 50, order = 3),
                // Покупки
                QuestEntity(topic = "SPENDING", title = "Сравни цены", description = "Два одинаковых корма, разная цена; купи дешевле (мини-игра «Найди дешевле»)", reward = 50, order = 4),
                QuestEntity(topic = "SPENDING", title = "Сдача на кассе", description = "Клиент дал 100, товар 70; дай сдачу 30 (мини-игра «Касса»)", reward = 50, order = 5)
            )
            db.questDao().upsertAll(quests)
        }
        // Seed catalog (8 товаров: 4 нужных + 4 желаемых)
        if (db.catalogItemDao().observeAll().first().isEmpty()) {
            val items = listOf(
                // Нужное
                CatalogItemEntity(title = "Корм", price = 40, category = "NEED", hungerEffect = 40, iconRes = "ic_item_food", order = 0),
                CatalogItemEntity(title = "Вода", price = 20, category = "NEED", hungerEffect = 15, iconRes = "ic_item_water", order = 1),
                CatalogItemEntity(title = "Расчёска / уход", price = 25, category = "NEED", moodEffect = 10, iconRes = "ic_item_brush", order = 2),
                CatalogItemEntity(title = "Подстилка", price = 50, category = "NEED", energyEffect = 15, iconRes = "ic_item_bedding", order = 3),
                // Желаемое
                CatalogItemEntity(title = "Мячик", price = 60, category = "WANT", moodEffect = 25, iconRes = "ic_item_ball", order = 4),
                CatalogItemEntity(title = "Бантик", price = 35, category = "WANT", moodEffect = 10, iconRes = "ic_item_bow", order = 5),
                CatalogItemEntity(title = "Картина на стену", price = 80, category = "WANT", moodEffect = 15, iconRes = "ic_item_picture", order = 6),
                CatalogItemEntity(title = "Праздничный торт", price = 90, category = "WANT", moodEffect = 20, iconRes = "ic_item_cake", order = 7)
            )
            db.catalogItemDao().upsertAll(items)
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

    // --- День 5: три банки (нужное/желаемое/копилка) ---
    suspend fun distributeBanks(needPlan: Int, wantPlan: Int, savePlan: Int) {
        val w = db.walletDao().getOnce() ?: WalletEntity()
        val total = w.cash + w.needPlan + w.wantPlan + w.savePlan
        // защита: сумма плана не больше total
        val n = needPlan.coerceIn(0, total)
        val w_ = wantPlan.coerceIn(0, total - n)
        val s = savePlan.coerceIn(0, total - n - w_)
        val newCash = (total - n - w_ - s).coerceAtLeast(0)
        db.walletDao().upsert(w.copy(cash = newCash, needPlan = n, wantPlan = w_, savePlan = s))
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

    /** Зарплата Милы-кассира: расход дня, списывается только если хватает баланса. */
    suspend fun payCashierSalary(hired: Boolean) {
        if (!hired) return
        val wallet = db.walletDao().getOnce() ?: return
        if (wallet.cash < Balance.CASHIER_SALARY) return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash - Balance.CASHIER_SALARY))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "salary", amount = Balance.CASHIER_SALARY)
        )
    }

    /** Доход от участка: пассивная «аренда» каждый день. */
    suspend fun payLotRent(owned: Boolean) {
        if (!owned) return
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + Balance.LOT_RENT_PER_DAY))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "lot_rent", amount = Balance.LOT_RENT_PER_DAY)
        )
    }

    /** Покупка второго здания: списывает участок из доступных. */
    suspend fun buyLot() {
        val wallet = db.walletDao().getOnce() ?: return
        if (wallet.cash < Balance.LOT_PRICE) return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash - Balance.LOT_PRICE))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "lot", amount = Balance.LOT_PRICE)
        )
    }

    /** Продажа здания дешевле, чем купил: урок «недвижимость теряет цену». */
    suspend fun sellLot() {
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + Balance.LOT_SELL_PRICE))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "lot_sale", amount = Balance.LOT_SELL_PRICE)
        )
    }

    // --- Цели накопления ---
    suspend fun addToGoal(goalId: Int, amount: Int) {
        val goals = db.goalDao().observeAll().first()
        val goal = goals.find { it.id == goalId } ?: return
        val wallet = db.walletDao().getOnce() ?: return
        if (wallet.cash < amount) return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash - amount, saveFact = wallet.saveFact + amount))
        val updatedGoal = goal.copy(currentAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount))
        db.goalDao().upsert(updatedGoal)
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "goal_deposit", amount = amount)
        )
    }

    suspend fun withdrawFromGoal(goalId: Int, amount: Int) {
        val goals = db.goalDao().observeAll().first()
        val goal = goals.find { it.id == goalId } ?: return
        if (goal.currentAmount < amount) return
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + amount, saveFact = wallet.saveFact - amount))
        val updatedGoal = goal.copy(currentAmount = (goal.currentAmount - amount).coerceAtLeast(0))
        db.goalDao().upsert(updatedGoal)
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "goal_withdraw", amount = amount)
        )
    }
}
