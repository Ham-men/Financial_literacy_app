package com.example.financialliteracyapp.data

import com.example.financialliteracyapp.data.local.AppDatabase
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.data.local.entity.BuildingEntity
import com.example.financialliteracyapp.data.local.entity.CatalogItemEntity
import com.example.financialliteracyapp.data.local.entity.GoalEntity
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import com.example.financialliteracyapp.data.local.entity.TransactionEntity
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.min

/** Репозиторий v5.0: здания, боты с позициями, замкнутый экономический цикл. */
class GameRepository(private val db: AppDatabase) {

    fun observePet() = db.petDao().observe()
    fun observeWallet() = db.walletDao().observe()
    fun observeBuildings() = db.buildingDao().observeAll()
    fun observeBuildingsByDistrict(district: String) = db.buildingDao().observeByDistrict(district)
    fun observeTransactions() = db.transactionDao().observeAll()
    fun observeBots() = db.botDao().observeAll()
    fun observeWorkersForBuilding(buildingId: Long) = db.botDao().observeByWorkplace(buildingId)
    fun observeGoals() = db.goalDao().observeAll()
    fun observeQuests() = db.questDao().observeAll()
    fun observeCatalog() = db.catalogItemDao().observeAll()
    fun observeCatalogByCategory(category: String) = db.catalogItemDao().observeByCategory(category)

    // --- Совместимость со старым ларьком: здание «Продукты» вместо ShopEntity ---
    /** Стартовое здание района Рынок — «продуктовый» магазин игрока. */
    private suspend fun legacyBuilding(): BuildingEntity? =
        getBuildingByTypeAndDistrict("PRODUCTS", "Рынок")

    fun observeShop(): Flow<BuildingEntity?> =
        observeBuildingsByDistrict("Рынок").map { list -> list.firstOrNull { it.type == "PRODUCTS" } }

    suspend fun setShopPrice(price: Int) {
        val b = legacyBuilding() ?: return
        db.buildingDao().upsert(b.copy(price = price.coerceAtLeast(1)))
    }

    suspend fun purchaseStock(pricePerUnit: Double, units: Int) {
        val b = legacyBuilding() ?: return
        purchaseStockForBuilding(b.id, pricePerUnit, units)
    }

    suspend fun simulateBotDay() {
        simulateDay()
    }

    suspend fun takeItem(units: Int): Boolean {
        val b = legacyBuilding() ?: return false
        return takeItemFromBuilding(b.id, units)
    }

    suspend fun refundStock(units: Int) {
        val b = legacyBuilding() ?: return
        refundStockToBuilding(b.id, units)
    }

    suspend fun completeSale(units: Int) {
        val b = legacyBuilding() ?: return
        completeSaleInBuilding(b.id, units)
    }

    suspend fun upsertPet(pet: PetEntity) = db.petDao().upsert(pet)

    suspend fun ensureSeed() {
        if (db.petDao().getOnce() == null) {
            db.petDao().upsert(PetEntity())
        }
        if (db.walletDao().getOnce() == null) {
            db.walletDao().upsert(WalletEntity())
        }
        if (db.buildingDao().observeAll().first().isEmpty()) {
            // Стартовые здания на районе Рынок: «Продукты» и «СТО»
            db.buildingDao().upsert(
                BuildingEntity(
                    type = "PRODUCTS",
                    district = "Рынок",
                    x = 5, y = 5,
                    level = 1,
                    stock = 50,
                    price = 8,
                    costPrice = 3,
                    cash = 200,
                    dirtLevel = 30
                )
            )
            db.buildingDao().upsert(
                BuildingEntity(
                    type = "AUTO_SERVICE",
                    district = "Рынок",
                    x = 13, y = 5,
                    level = 1,
                    stock = 30,
                    price = 30,
                    costPrice = 12,
                    cash = 400,
                    dirtLevel = 10
                )
            )
        }
        val allBuildings = db.buildingDao().observeAll().first()
        val productsId = allBuildings.firstOrNull { it.type == "PRODUCTS" }?.id ?: -1L
        val autoServiceId = allBuildings.firstOrNull { it.type == "AUTO_SERVICE" }?.id ?: -1L
        if (db.botDao().count() == 0) {
            // 2 работника в «Продуктах» (зарплата), 1 механик в СТО, остальные — покупатели.
            val bots = listOf(
                BotEntity(id = 1L, type = "WORKER",  salary = 100, wallet = 200, loyalty = 0.6f,
                    hasCar = false, workBuildingId = productsId, state = "HOME"),
                BotEntity(id = 2L, type = "WORKER",  salary = 100, wallet = 200, loyalty = 0.6f,
                    hasCar = false, workBuildingId = productsId, state = "HOME"),
                BotEntity(id = 3L, type = "ENGINEER", salary = 120, wallet = 300, loyalty = 0.6f,
                    hasCar = true, workBuildingId = autoServiceId, state = "HOME"),
                BotEntity(id = 4L, type = "STUDENT",  salary = 0, wallet = 300, loyalty = 0.5f,
                    hasCar = false, workBuildingId = -1, state = "HOME"),
                BotEntity(id = 5L, type = "RETIREE",  salary = 0, wallet = 400, loyalty = 0.5f,
                    hasCar = false, workBuildingId = -1, state = "HOME"),
                BotEntity(id = 6L, type = "MANAGER",  salary = 0, wallet = 600, loyalty = 0.5f,
                    hasCar = true, workBuildingId = -1, state = "HOME")
            )
            db.botDao().upsertAll(bots)
        }
        if (db.goalDao().observeAll().first().isEmpty()) {
            val goals = listOf(
                GoalEntity(title = "Мячик для Финни", targetAmount = 300, currentAmount = 0, order = 0),
                GoalEntity(title = "Палатка в парке", targetAmount = 800, currentAmount = 0, order = 1),
                GoalEntity(title = "Набор художника", targetAmount = 1500, currentAmount = 0, order = 2)
            )
            db.goalDao().upsertAll(goals)
        }
        if (db.questDao().observeAll().first().isEmpty()) {
            val quests = listOf(
                QuestEntity(topic = "PLANNING", title = "Три банки", description = "Разложи 300 ₡ так, чтобы хватило на корм и хоть что-то в копилку", reward = 50, order = 0),
                QuestEntity(topic = "PLANNING", title = "Непредвиденный расход", description = "Финни порвал подстилку, нужно 50 ₡. Откуда взять: из желаемого или из копилки?", reward = 50, order = 1),
                QuestEntity(topic = "SAVING", title = "Цель ближе", description = "Положи в копилку не меньше 50 ₡, увидь, как шкала цели подросла", reward = 50, order = 2),
                QuestEntity(topic = "SAVING", title = "Не снимай сразу", description = "Соблазн купить торт из копилки; если снимаешь — подтверждаешь и видишь сдвиг срока", reward = 50, order = 3),
                QuestEntity(topic = "SPENDING", title = "Сравни цены", description = "Два одинаковых корма, разная цена; купи дешевле (мини-игра «Найди дешевле»)", reward = 50, order = 4),
                QuestEntity(topic = "SPENDING", title = "Сдача на кассе", description = "Клиент дал 100, товар 70; дай сдачу 30 (мини-игра «Касса»)", reward = 50, order = 5)
            )
            db.questDao().upsertAll(quests)
        }
        if (db.catalogItemDao().observeAll().first().isEmpty()) {
            val items = listOf(
                CatalogItemEntity(title = "Корм", price = 40, category = "NEED", hungerEffect = 40, iconRes = "ic_item_food", order = 0),
                CatalogItemEntity(title = "Вода", price = 20, category = "NEED", hungerEffect = 15, iconRes = "ic_item_water", order = 1),
                CatalogItemEntity(title = "Расчёска / уход", price = 25, category = "NEED", moodEffect = 10, iconRes = "ic_item_brush", order = 2),
                CatalogItemEntity(title = "Подстилка", price = 50, category = "NEED", energyEffect = 15, iconRes = "ic_item_bedding", order = 3),
                CatalogItemEntity(title = "Мячик", price = 60, category = "WANT", moodEffect = 25, iconRes = "ic_item_ball", order = 4),
                CatalogItemEntity(title = "Бантик", price = 35, category = "WANT", moodEffect = 10, iconRes = "ic_item_bow", order = 5),
                CatalogItemEntity(title = "Картина на стену", price = 80, category = "WANT", moodEffect = 15, iconRes = "ic_item_picture", order = 6),
                CatalogItemEntity(title = "Праздничный торт", price = 90, category = "WANT", moodEffect = 20, iconRes = "ic_item_cake", order = 7)
            )
            db.catalogItemDao().upsertAll(items)
        }
    }

    // --- Питомец ---
    suspend fun feedPet(cost: Int = Balance.FEED_COST) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        val newCash = GameRules.spend(wallet.cash, cost) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.petDao().upsert(pet.copy(hunger = (pet.hunger + Balance.PET_FOOD_HUNGER_GAIN).coerceAtMost(100)))
        db.transactionDao().insert(TransactionEntity(kind = "EXPENSE", category = "pet_food", amount = cost))
    }

    suspend fun playWithPet(cost: Int = Balance.PLAY_COST) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        val newCash = GameRules.spend(wallet.cash, cost) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.petDao().upsert(pet.copy(mood = (pet.mood + Balance.PET_PLAY_MOOD_GAIN).coerceAtMost(100)))
    }

    suspend fun restPet() {
        val pet = db.petDao().getOnce() ?: PetEntity()
        db.petDao().upsert(pet.copy(energy = (pet.energy + Balance.PET_SLEEP_ENERGY_GAIN).coerceAtMost(100)))
    }

    suspend fun movePet(x: Float, y: Float) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        db.petDao().upsert(pet.copy(x = x, y = y))
    }

    /** Состояние и позиция бота на карте (P0: видимые боты-NPC). */
    suspend fun updateBotStatePosition(botId: Long, state: String, x: Float, y: Float) {
        val bot = db.botDao().getAllOnce().find { it.id == botId } ?: return
        db.botDao().upsert(bot.copy(state = state, targetX = x, targetY = y))
    }

    // --- Банки (план/факт) ---
    suspend fun distributeBanks(needPlan: Int, wantPlan: Int, savePlan: Int) {
        val w = db.walletDao().getOnce() ?: WalletEntity()
        val total = w.cash + w.needPlan + w.wantPlan + w.savePlan
        val alloc = GameRules.solvePlan(total, needPlan, wantPlan, savePlan)
        db.walletDao().upsert(
            w.copy(cash = alloc.cashRemainder, needPlan = alloc.need, wantPlan = alloc.want, savePlan = alloc.save)
        )
    }

    suspend fun availableFor(category: String): Int {
        val w = db.walletDao().getOnce() ?: WalletEntity()
        val planSet = w.needPlan + w.wantPlan + w.savePlan > 0
        return when {
            !planSet -> w.cash
            category == "NEED" -> w.needPlan + w.cash
            category == "WANT" -> w.wantPlan + w.cash
            else -> w.cash
        }
    }

    // --- Здания v5.0 ---
    suspend fun getBuilding(id: Long): BuildingEntity? = db.buildingDao().observeById(id).first()

    suspend fun getBuildingByTypeAndDistrict(type: String, district: String): BuildingEntity? =
        db.buildingDao().getByTypeAndDistrict(type, district)

    suspend fun upsertBuilding(building: BuildingEntity) = db.buildingDao().upsert(building)

    suspend fun purchaseStockForBuilding(buildingId: Long, supplierPricePerUnit: Double, units: Int) {
        val building = db.buildingDao().observeById(buildingId).first() ?: return
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        val cost = (supplierPricePerUnit * units).toInt()
        val newCash = GameRules.spend(wallet.cash, cost) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.buildingDao().upsert(
            building.copy(
                stock = building.stock + units,
                costPrice = supplierPricePerUnit.toInt().coerceAtLeast(1),
                cash = building.cash - cost
            )
        )
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "stock_purchase", amount = cost, buildingId = buildingId)
        )
    }

    suspend fun takeItemFromBuilding(buildingId: Long, units: Int): Boolean {
        if (units <= 0) return false
        val building = db.buildingDao().observeById(buildingId).first() ?: return false
        if (building.stock < units) return false
        db.buildingDao().upsert(building.copy(stock = building.stock - units))
        return true
    }

    suspend fun refundStockToBuilding(buildingId: Long, units: Int) {
        if (units <= 0) return
        val building = db.buildingDao().observeById(buildingId).first() ?: return
        db.buildingDao().upsert(building.copy(stock = building.stock + units))
    }

    suspend fun completeSaleInBuilding(buildingId: Long, units: Int) {
        if (units <= 0) return
        val building = db.buildingDao().observeById(buildingId).first() ?: return
        val revenue = units * building.price
        val cost = units * building.costPrice
        val profit = (revenue - cost).coerceAtLeast(0)
        db.buildingDao().upsert(
            building.copy(
                cash = building.cash + revenue,
                soldToday = building.soldToday + units,
                revenueTotal = building.revenueTotal + revenue
            )
        )
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + profit))
        if (revenue > 0) {
            db.transactionDao().insert(
                TransactionEntity(kind = "INCOME", category = "bot_sales", amount = revenue, buildingId = buildingId)
            )
        }
        if (cost > 0) {
            db.transactionDao().insert(
                TransactionEntity(kind = "EXPENSE", category = "cogs", amount = cost, buildingId = buildingId)
            )
        }
    }

    suspend fun updateDirtLevel(buildingId: Long, dirt: Int) {
        val building = db.buildingDao().observeById(buildingId).first() ?: return
        db.buildingDao().upsert(building.copy(dirtLevel = dirt.coerceIn(0, 100)))
    }

    suspend fun rewardCleaning(amount: Int) {
        if (amount <= 0) return
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + amount))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "cleaning_reward", amount = amount)
        )
    }

    /** Найм бота на работу в здание игрока: привязка workBuildingId + зарплата (плюс разовый сбор 50 ₡). */
    suspend fun hireBotToBuilding(botId: Long, buildingId: Long, salary: Int) {
        val bot = db.botDao().getAllOnce().find { it.id == botId } ?: return
        val wallet = db.walletDao().getOnce() ?: return
        val newCash = GameRules.spend(wallet.cash, 50) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.botDao().upsert(bot.copy(workBuildingId = buildingId, salary = salary, state = "HOME"))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "hiring", amount = 50, buildingId = buildingId)
        )
    }

    suspend fun paySalaryToBot(botId: Long, amount: Int) {
        val bot = db.botDao().getAllOnce().find { it.id == botId } ?: return
        val wallet = db.walletDao().getOnce() ?: return
        val newCash = GameRules.spend(wallet.cash, amount) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.botDao().upsert(bot.copy(wallet = bot.wallet + amount))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "salary", amount = amount)
        )
    }

    // --- Симуляция дня v5.0: замкнутый цикл зарплата → траты в зданиях ---
    suspend fun simulateDay(): DayResult {
        val buildings = db.buildingDao().observeAll().first()
        val allBots = db.botDao().getAllOnce()

        if (buildings.isEmpty() || allBots.isEmpty()) return DayResult(0, 0, 0, 0, 0, 0, 0, 0)

        // 1. Утро: боты идут на работу (находят здание где работают)
        val workingBots = allBots.filter { it.workBuildingId != -1L }
        var totalSalary = 0
        var totalRevenue = 0
        var totalCost = 0
        var totalRent = 0
        var totalTax = 0

        // Игрок платит зарплаты сотрудникам своих зданий
        for (building in buildings) {
            val employees = workingBots.filter { it.workBuildingId == building.id }
            val salaryCost = employees.sumOf { it.salary }
            val playerWallet = db.walletDao().getOnce() ?: WalletEntity()
            val newCash = GameRules.spend(playerWallet.cash, salaryCost)
            if (newCash != null) {
                db.walletDao().upsert(playerWallet.copy(cash = newCash))
                totalSalary += salaryCost
                db.transactionDao().insert(
                    TransactionEntity(kind = "EXPENSE", category = "salary", amount = salaryCost, buildingId = building.id)
                )
                // Боты получают зарплату
                for (bot in employees) {
                    db.botDao().upsert(bot.copy(wallet = bot.wallet + bot.salary, state = "WORKING"))
                }
            } else {
                // Не хватает на зарплату — боты не работают, лояльность падает
                for (bot in employees) {
                    db.botDao().upsert(bot.copy(loyalty = (bot.loyalty * 0.5f).coerceIn(0f, 1f), state = "HOME"))
                }
            }
        }

        // 2. День: боты работают, здания получают выручку (упрощённо — по формуле)
        for (building in buildings) {
            val employees = workingBots.filter { it.workBuildingId == building.id }
            val maxSales = employees.size * 10 // каждый сотрудник может обслужить до 10 клиентов
            val potentialBuyers = allBots.filter {
                it.wallet > building.price && it.workBuildingId != building.id &&
                    (building.type != "AUTO_SERVICE" || it.hasCar)
            }
            val actualSales = min(maxSales, min(building.stock, potentialBuyers.size))
            if (actualSales > 0) {
                val revenue = actualSales * building.price
                val cost = actualSales * building.costPrice
                val rent = when (building.type) {
                    "PRODUCTS" -> 20
                    "AUTO_SERVICE" -> 30
                    "CONSTRUCTION" -> 40
                    "HEALTH" -> 50
                    "ART" -> 100
                    else -> 0
                }
                val tax = (revenue * Balance.TAX_RATE).toInt()
                val profit = revenue - cost - rent - tax

                db.buildingDao().upsert(
                    building.copy(
                        stock = building.stock - actualSales,
                        cash = building.cash + revenue,
                        soldToday = building.soldToday + actualSales,
                        revenueTotal = building.revenueTotal + revenue
                    )
                )
                // Боты-покупатели тратят деньги
                val buyers = potentialBuyers.take(actualSales)
                for (buyer in buyers) {
                    db.botDao().upsert(buyer.copy(wallet = buyer.wallet - building.price))
                }

                totalRevenue += revenue
                totalCost += cost
                totalRent += rent
                totalTax += tax

                db.transactionDao().insert(
                    TransactionEntity(kind = "INCOME", category = "bot_sales", amount = revenue, buildingId = building.id)
                )
                db.transactionDao().insert(
                    TransactionEntity(kind = "EXPENSE", category = "cogs", amount = cost, buildingId = building.id)
                )
                db.transactionDao().insert(
                    TransactionEntity(kind = "EXPENSE", category = "rent", amount = rent, buildingId = building.id)
                )
                if (tax > 0) {
                    db.transactionDao().insert(
                        TransactionEntity(kind = "EXPENSE", category = "tax", amount = tax, buildingId = building.id)
                    )
                }
            }
        }

        // 3. Вечер: сброс дневных счётчиков
        for (building in buildings) {
            db.buildingDao().upsert(building.copy(soldToday = 0))
        }

        val profit = totalRevenue - totalCost - totalRent - totalTax - totalSalary
        val playerWallet = db.walletDao().getOnce() ?: WalletEntity()
        db.walletDao().upsert(playerWallet.copy(cash = playerWallet.cash + profit.coerceAtLeast(0)))

        return DayResult(
            totalSalary = totalSalary,
            totalRevenue = totalRevenue,
            totalCost = totalCost,
            totalRent = totalRent,
            totalTax = totalTax,
            totalProfit = profit,
            buildingsActive = buildings.count { it.isOpen },
            botsWorking = workingBots.size
        )
    }

    data class DayResult(
        val totalSalary: Int,
        val totalRevenue: Int,
        val totalCost: Int,
        val totalRent: Int,
        val totalTax: Int,
        val totalProfit: Int,
        val buildingsActive: Int,
        val botsWorking: Int
    )

    suspend fun resetDay() {
        // Сброс банок плана
        val wallet = db.walletDao().getOnce()
        if (wallet != null) {
            db.walletDao().upsert(
                wallet.copy(
                    cash = wallet.cash + wallet.needPlan + wallet.wantPlan + wallet.savePlan,
                    needPlan = 0, wantPlan = 0, savePlan = 0,
                    needFact = 0, wantFact = 0, saveFact = 0
                )
            )
        }
        // Сброс soldToday у зданий
        val buildings = db.buildingDao().observeAll().first()
        for (b in buildings) {
            db.buildingDao().upsert(b.copy(soldToday = 0))
        }
        // Пополнение ботов (зарплата уже выплачена в simulateDay, но для безработных — доход)
        val bots = db.botDao().getAllOnce()
        if (bots.isNotEmpty()) {
            val replenished = bots.map { it.copy(wallet = (it.wallet + 100).coerceAtMost(2000)) }
            db.botDao().upsertAll(replenished)
        }
    }

    // --- Зарплата кассира (совместимость со старым кодом) ---
    suspend fun payCashierSalary(hired: Boolean) {
        if (!hired) return
        val wallet = db.walletDao().getOnce() ?: return
        val newCash = GameRules.spend(wallet.cash, Balance.CASHIER_SALARY) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "salary", amount = Balance.CASHIER_SALARY)
        )
    }

    // --- Участок / аренда ---
    suspend fun payLotRent(owned: Boolean) {
        if (!owned) return
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + Balance.LOT_RENT_PER_DAY))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "lot_rent", amount = Balance.LOT_RENT_PER_DAY)
        )
    }

    suspend fun buyLot() {
        val wallet = db.walletDao().getOnce() ?: return
        val newCash = GameRules.spend(wallet.cash, Balance.LOT_PRICE) ?: return
        db.walletDao().upsert(wallet.copy(cash = newCash))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "lot", amount = Balance.LOT_PRICE)
        )
    }

    suspend fun sellLot() {
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + Balance.LOT_SELL_PRICE))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "lot_sale", amount = Balance.LOT_SELL_PRICE)
        )
    }

    // --- Каталог покупок для питомца ---
    suspend fun buyCatalogItem(itemId: Int) {
        val catalog = db.catalogItemDao().observeAll().first()
        val item = catalog.find { it.id == itemId } ?: return
        val wallet = db.walletDao().getOnce() ?: WalletEntity()
        val planSet = wallet.needPlan + wallet.wantPlan + wallet.savePlan > 0
        val pet = db.petDao().getOnce() ?: PetEntity()

        val updated: WalletEntity? = when {
            planSet && item.category == "NEED" -> {
                val remaining = (wallet.needPlan + wallet.cash) - item.price
                if (remaining < 0) null
                else wallet.copy(
                    cash = if (wallet.needPlan >= item.price) wallet.cash
                           else (wallet.needPlan + wallet.cash - item.price).coerceAtLeast(0),
                    needPlan = (wallet.needPlan - item.price).coerceAtLeast(0),
                    needFact = wallet.needFact + item.price
                )
            }
            planSet && item.category == "WANT" -> {
                val remaining = (wallet.wantPlan + wallet.cash) - item.price
                if (remaining < 0) null
                else wallet.copy(
                    cash = if (wallet.wantPlan >= item.price) wallet.cash
                           else (wallet.wantPlan + wallet.cash - item.price).coerceAtLeast(0),
                    wantPlan = (wallet.wantPlan - item.price).coerceAtLeast(0),
                    wantFact = wallet.wantFact + item.price
                )
            }
            !planSet -> {
                val newCash = GameRules.spend(wallet.cash, item.price) ?: return
                wallet.copy(cash = newCash)
            }
            else -> null
        }
        if (updated == null) return

        db.walletDao().upsert(updated)
        db.petDao().upsert(
            pet.copy(
                hunger = (pet.hunger + item.hungerEffect).coerceAtMost(100),
                mood   = (pet.mood   + item.moodEffect).coerceAtMost(100),
                energy = (pet.energy + item.energyEffect).coerceAtMost(100)
            )
        )
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "shop_purchase", amount = item.price)
        )
    }

    // --- Цели ---
    suspend fun addToGoal(goalId: Int, amount: Int) {
        val goals = db.goalDao().observeAll().first()
        val goal = goals.find { it.id == goalId } ?: return
        val wallet = db.walletDao().getOnce() ?: return
        val planSet = wallet.needPlan + wallet.wantPlan + wallet.savePlan > 0

        val updated: WalletEntity? = if (planSet) {
            if (wallet.savePlan + wallet.cash < amount) null
            else wallet.copy(
                cash = if (wallet.savePlan >= amount) wallet.cash
                       else (wallet.savePlan + wallet.cash - amount).coerceAtLeast(0),
                savePlan = (wallet.savePlan - amount).coerceAtLeast(0),
                saveFact = wallet.saveFact + amount
            )
        } else {
            val newCash = GameRules.spend(wallet.cash, amount) ?: return
            wallet.copy(cash = newCash, saveFact = wallet.saveFact + amount)
        }
        if (updated == null) return

        db.walletDao().upsert(updated)
        val updatedGoal = goal.copy(
            currentAmount = GameRules.capGoalDeposit(goal.currentAmount, goal.targetAmount, amount)
        )
        db.goalDao().upsert(updatedGoal)
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "goal_deposit", amount = amount)
        )
    }

    suspend fun withdrawFromGoal(goalId: Int, amount: Int) {
        val goals = db.goalDao().observeAll().first()
        val goal = goals.find { it.id == goalId } ?: return
        val newAmount = GameRules.spend(goal.currentAmount, amount) ?: return
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + amount, saveFact = wallet.saveFact - amount))
        db.goalDao().upsert(goal.copy(currentAmount = newAmount))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "goal_withdraw", amount = amount)
        )
    }
}