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

    fun observeBuildingById(id: Long) = db.buildingDao().observeById(id)
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
            // Стартовое здание на районе Рынок: только «Продукты» (лаpёк).
            // СТО/Стройматериалы игрок построит сам, купив участок на карте.
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
        }
        val allBuildings = db.buildingDao().observeAll().first()
        val productsId = allBuildings.firstOrNull { it.type == "PRODUCTS" }?.id ?: -1L
        val autoServiceId = allBuildings.firstOrNull { it.type == "AUTO_SERVICE" }?.id ?: -1L
        if (db.botDao().count() == 0) {
            // 2 работника в «Продуктах» (зарплата), остальные — покупатели.
            // Механик (ENGINEER) пока без рабочего здания — встанет, когда игрок построит СТО.
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
                QuestEntity(topic = "SPENDING", title = "Сдача на кассе", description = "Клиент дал 100, товар 70; дай сдачу 30 (мини-игра «Касса»)", reward = 50, order = 5),
                QuestEntity(topic = "PLAY", title = "Первая игрушка", description = "Купи игрушку для Финни в комнате развлечений и получи первый опыт", reward = 50, order = 6, progress = 0, target = 3),
                QuestEntity(topic = "PLAY", title = "Уровень развлечений 2", description = "Набери 10 опыта с игрушками — копилка начнёт расти быстрее", reward = 70, order = 7, progress = 0, target = 10),
                QuestEntity(topic = "PLAY", title = "Уровень развлечений 5", description = "Набери 25 опыта с игрушками — копилка получит максимальную ставку", reward = 120, order = 8, progress = 0, target = 25)
            )
            db.questDao().upsertAll(quests)
        } else if (db.questDao().observeAll().first().none { it.topic == "PLAY" }) {
            // Существующие установки: добавляем задания комнаты развлечений.
            db.questDao().upsertAll(
                listOf(
                    QuestEntity(topic = "PLAY", title = "Первая игрушка", description = "Купи игрушку для Финни в комнате развлечений и получи первый опыт", reward = 50, order = 6, progress = 0, target = 3),
                    QuestEntity(topic = "PLAY", title = "Уровень развлечений 2", description = "Набери 10 опыта с игрушками — копилка начнёт расти быстрее", reward = 70, order = 7, progress = 0, target = 10),
                    QuestEntity(topic = "PLAY", title = "Уровень развлечений 5", description = "Набери 25 опыта с игрушками — копилка получит максимальную ставку", reward = 120, order = 8, progress = 0, target = 25)
                )
            )
        }
        if (db.catalogItemDao().observeAll().first().isEmpty()) {
            val items = listOf(
                CatalogItemEntity(title = "Корм", description = "Сытный обед для голодного дня", price = 40, category = "NEED", hungerEffect = 40, iconRes = "ic_item_food", order = 0),
                CatalogItemEntity(title = "Вода", description = "Свежая вода в мисочке", price = 20, category = "NEED", hungerEffect = 15, iconRes = "ic_item_water", order = 1),
                CatalogItemEntity(title = "Расчёска / уход", description = "Шёрстка будет блестеть", price = 25, category = "NEED", moodEffect = 10, iconRes = "ic_item_brush", order = 2),
                CatalogItemEntity(title = "Подстилка", description = "Мягкое место для сна", price = 50, category = "NEED", energyEffect = 15, iconRes = "ic_item_bedding", order = 3),
                CatalogItemEntity(title = "Мячик", description = "Гоняй по полу и лови лапкой", price = 60, category = "WANT", moodEffect = 25, xpReward = 3, iconRes = "ic_item_ball", order = 4),
                CatalogItemEntity(title = "Бантик", description = "Яркий бантик для красивых фото", price = 35, category = "WANT", moodEffect = 10, xpReward = 2, iconRes = "ic_item_bow", order = 5),
                CatalogItemEntity(title = "Картина на стену", description = "Картина, чтобы любоваться на рыб", price = 80, category = "WANT", moodEffect = 15, xpReward = 4, iconRes = "ic_item_picture", order = 6),
                CatalogItemEntity(title = "Праздничный торт", description = "Торт на праздничный день", price = 90, category = "WANT", moodEffect = 20, xpReward = 5, iconRes = "ic_item_cake", order = 7)
            )
            db.catalogItemDao().upsertAll(items)
        }
    }

    // --- Питомец ---
    /** Списать cost из банки (need/want) или из мешка, если план не разложен. Копилку не тратим. */
    private suspend fun spendFromPlan(useNeed: Boolean, cost: Int): Boolean {
        if (cost <= 0) return true
        val wallet = db.walletDao().getOnce() ?: return false
        val planSet = wallet.needPlan + wallet.wantPlan + wallet.savePlan > 0
        val updated: WalletEntity? = when {
            !useNeed && planSet -> {
                val remaining = (wallet.wantPlan + wallet.cash) - cost
                if (remaining < 0) null
                else wallet.copy(
                    cash = if (wallet.wantPlan >= cost) wallet.cash
                           else (wallet.wantPlan + wallet.cash - cost).coerceAtLeast(0),
                    wantPlan = (wallet.wantPlan - cost).coerceAtLeast(0),
                    wantFact = wallet.wantFact + cost
                )
            }
            useNeed && planSet -> {
                val remaining = (wallet.needPlan + wallet.cash) - cost
                if (remaining < 0) null
                else wallet.copy(
                    cash = if (wallet.needPlan >= cost) wallet.cash
                           else (wallet.needPlan + wallet.cash - cost).coerceAtLeast(0),
                    needPlan = (wallet.needPlan - cost).coerceAtLeast(0),
                    needFact = wallet.needFact + cost
                )
            }
            else -> {
                val newCash = GameRules.spend(wallet.cash, cost) ?: return false
                wallet.copy(cash = newCash)
            }
        }
        if (updated == null) return false
        db.walletDao().upsert(updated)
        return true
    }

    /** Банка «нужное»: еда, лечение, товар в магазины, найм и зарплаты. */
    private suspend fun spendFromNeed(cost: Int): Boolean = spendFromPlan(useNeed = true, cost)

    /** Банка «желаемое»: игрушки для Финни (витрина WANT). */
    private suspend fun spendFromWant(cost: Int): Boolean = spendFromPlan(useNeed = false, cost)

    suspend fun feedPet(cost: Int = Balance.FEED_COST) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        if (!spendFromNeed(cost)) return
        db.petDao().upsert(pet.copy(hunger = (pet.hunger + Balance.PET_FOOD_HUNGER_GAIN).coerceAtMost(100)))
        db.transactionDao().insert(TransactionEntity(kind = "EXPENSE", category = "pet_food", amount = cost))
    }

    suspend fun healPet(cost: Int = Balance.HEAL_COST) {
        val pet = db.petDao().getOnce() ?: PetEntity()
        if (!spendFromNeed(cost)) return
        db.petDao().upsert(pet.copy(mood = (pet.mood + Balance.PET_HEAL_MOOD_GAIN).coerceAtMost(100)))
        db.transactionDao().insert(TransactionEntity(kind = "EXPENSE", category = "pet_heal", amount = cost))
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

    // --- Банки (мешок ↔ 3 банки: карман/желаемое/копилка) ---
    /** Перенос монеты (или выбранной суммы) из мешка в банку. */
    suspend fun moveBagToBank(bank: String, amount: Int) {
        if (amount <= 0) return
        val w = db.walletDao().getOnce() ?: return
        if (w.cash < amount) return
        val updated = when (bank) {
            "NEED" -> w.copy(cash = w.cash - amount, needPlan = w.needPlan + amount)
            "WANT" -> w.copy(cash = w.cash - amount, wantPlan = w.wantPlan + amount)
            "SAVE" -> w.copy(cash = w.cash - amount, savePlan = w.savePlan + amount)
            else -> return
        }
        db.walletDao().upsert(updated)
    }

    /** «Вывести всё в мешок»: все деньги из всех банок обратно в мешок (cash). */
    suspend fun withdrawAllToBag() {
        val w = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(
            w.copy(
                cash = w.cash + w.needPlan + w.wantPlan + w.savePlan,
                needPlan = 0, wantPlan = 0, savePlan = 0
            )
        )
    }

    /** Чит для теста: прибавить деньги напрямую в банку (карман/желаемое/копилка) без списания с мешка. */
    suspend fun cheatAddToBank(bank: String, amount: Int) {
        if (amount <= 0) return
        val w = db.walletDao().getOnce() ?: return
        val updated = when (bank) {
            "NEED" -> w.copy(needPlan = w.needPlan + amount)
            "WANT" -> w.copy(wantPlan = w.wantPlan + amount)
            "SAVE" -> w.copy(savePlan = w.savePlan + amount)
            else -> return
        }
        db.walletDao().upsert(updated)
    }

    /** Все доходы идут в мешок (cash). Распределение по банкам — только тут. */
    suspend fun distributeBanks(needPlan: Int, wantPlan: Int, savePlan: Int) {
        val w = db.walletDao().getOnce() ?: WalletEntity()
        val alloc = GameRules.solvePlan(w.cash, needPlan, wantPlan, savePlan)
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
        val cost = (supplierPricePerUnit * units).toInt()
        if (!spendFromNeed(cost)) return
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

    /**
     * Одна пассивная продажа нанятого сотрудника (v5): списывает 1 ед. товара со склада
     * и кладёт выручку В КАССУ КОНКРЕТНОГО МАГАЗИНА (building.cash), а не в мешок игрока.
     * Забрать деньги можно на сцене «Найм сотрудника» кнопкой «Забрать деньги».
     */
    suspend fun passiveSaleInBuilding(buildingId: Long): Boolean {
        val building = db.buildingDao().observeById(buildingId).first() ?: return false
        if (building.stock < 1) return false
        val revenue = building.price
        val cost = building.costPrice
        db.buildingDao().upsert(
            building.copy(
                stock = building.stock - 1,
                cash = building.cash + revenue,
                soldToday = building.soldToday + 1,
                revenueTotal = building.revenueTotal + revenue
            )
        )
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
        return true
    }

    /**
     * «Забрать деньги» со сцены «Найм сотрудника»: вся касса конкретного магазина (building.cash)
     * уходит в мешок игрока (cash), откуда её можно распределить по банкам на сцене ПЛАН.
     */
    suspend fun withdrawBuildingCash(buildingId: Long): Int {
        val building = db.buildingDao().observeById(buildingId).first() ?: return 0
        val amount = building.cash
        if (amount <= 0) return 0
        db.buildingDao().upsert(building.copy(cash = 0))
        val w = db.walletDao().getOnce() ?: return 0
        db.walletDao().upsert(w.copy(cash = w.cash + amount))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "shop_withdraw", amount = amount, buildingId = buildingId)
        )
        return amount
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

    /** Найм бота на работу в здание игрока: привязка workBuildingId + зарплата (плюс разовый сбор 50 ₡ из «нужного»). */
    suspend fun hireBotToBuilding(botId: Long, buildingId: Long, salary: Int) {
        val bot = db.botDao().getAllOnce().find { it.id == botId } ?: return
        if (!spendFromNeed(50)) return
        db.botDao().upsert(bot.copy(workBuildingId = buildingId, salary = salary, state = "HOME"))
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "hiring", amount = 50, buildingId = buildingId)
        )
    }

    suspend fun paySalaryToBot(botId: Long, amount: Int) {
        val bot = db.botDao().getAllOnce().find { it.id == botId } ?: return
        if (!spendFromNeed(amount)) return
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

        // Игрок платит зарплаты сотрудникам своих зданий (из банки «нужное»)
        for (building in buildings) {
            val employees = workingBots.filter { it.workBuildingId == building.id }
            val salaryCost = employees.sumOf { it.salary }
            if (salaryCost > 0 && spendFromNeed(salaryCost)) {
                totalSalary += salaryCost
                db.transactionDao().insert(
                    TransactionEntity(kind = "EXPENSE", category = "salary", amount = salaryCost, buildingId = building.id)
                )
                // Боты получают зарплату
                for (bot in employees) {
                    db.botDao().upsert(bot.copy(wallet = bot.wallet + bot.salary, state = "WORKING"))
                }
            } else if (salaryCost > 0) {
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
        // Копилка — накопительный счёт: проценты возвращаются в банку копилка.
        // Ставка растёт с уровнем развлечений Финни (базовые 5% + 1% за уровень).
        val wallet = db.walletDao().getOnce()
        if (wallet != null) {
            val pet = db.petDao().getOnce() ?: PetEntity()
            val level = GameRules.playLevel(pet.xp)
            val interest = GameRules.saveInterest(wallet.savePlan, GameRules.playInterestPercent(level))
            // Факт за день сбрасывается, банки остаются (перераспределение — кнопкой «Вывести всё в мешок»).
            db.walletDao().upsert(
                wallet.copy(savePlan = wallet.savePlan + interest, needFact = 0, wantFact = 0, saveFact = 0)
            )
            if (interest > 0) {
                db.transactionDao().insert(
                    TransactionEntity(kind = "INCOME", category = "save_interest", amount = interest)
                )
            }
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

    // --- Зарплата нанятых сотрудников (1 на здание, v5.0) ---
    suspend fun payCashierSalary(count: Int) {
        if (count <= 0) return
        val total = Balance.CASHIER_SALARY * count
        if (!spendFromNeed(total)) return
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "salary", amount = total)
        )
    }

    // --- Участки / купленные магазины (v5: здание = рабочее место + продажа) ---
    /** Покупка магазина на свободном участке: создаём рабочее здание, пассивного дохода нет.
     *  Оплата из банки «нужное» (в карман): needPlan → needFact, фолбэк на мешок. */
    suspend fun buyShop(plotId: String, type: String): Boolean {
        if (db.buildingDao().getByPlotId(plotId) != null) return false
        if (!spendFromNeed(Balance.LOT_PRICE)) return false
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "lot", amount = Balance.LOT_PRICE)
        )
        db.buildingDao().upsert(
            BuildingEntity(
                type = type,
                district = "Рынок",
                level = 1,
                stock = 20,
                price = defaultPriceFor(type),
                costPrice = defaultCostFor(type),
                cash = 0,
                isOpen = true,
                plotId = plotId
            )
        )
        return true
    }

    private fun defaultPriceFor(type: String): Int = when (type) {
        "AUTO_SERVICE" -> 30
        "CONSTRUCTION" -> 15
        else -> 8
    }

    private fun defaultCostFor(type: String): Int = when (type) {
        "AUTO_SERVICE" -> 12
        "CONSTRUCTION" -> 6
        else -> 3
    }

    /** Продажа купленного магазина: возврат части денег, здание удаляется с карты. */
    suspend fun sellShop(plotId: String) {
        val building = db.buildingDao().getByPlotId(plotId) ?: return
        val wallet = db.walletDao().getOnce() ?: return
        db.walletDao().upsert(wallet.copy(cash = wallet.cash + Balance.LOT_SELL_PRICE))
        db.transactionDao().insert(
            TransactionEntity(kind = "INCOME", category = "lot_sale", amount = Balance.LOT_SELL_PRICE)
        )
        db.buildingDao().deleteByPlotId(plotId)
    }

    // --- Каталог покупок для питомца (витрина: нужное/желаемое) ---
    suspend fun buyCatalogItem(itemId: Int) {
        val catalog = db.catalogItemDao().observeAll().first()
        val item = catalog.find { it.id == itemId } ?: return
        val pet = db.petDao().getOnce() ?: PetEntity()

        val paid = when (item.category) {
            "NEED" -> spendFromNeed(item.price)
            "WANT" -> spendFromWant(item.price)
            else -> false
        }
        if (!paid) return

        db.petDao().upsert(
            pet.copy(
                hunger = (pet.hunger + item.hungerEffect).coerceAtMost(100),
                mood   = (pet.mood   + item.moodEffect).coerceAtMost(100),
                energy = (pet.energy + item.energyEffect).coerceAtMost(100),
                xp     = (pet.xp     + item.xpReward).coerceAtMost(GameRules.MAX_PLAY_LEVEL * GameRules.XP_PER_LEVEL)
            )
        )
        db.transactionDao().insert(
            TransactionEntity(kind = "EXPENSE", category = "shop_purchase", amount = item.price)
        )
        updatePlayQuests(pet.xp + item.xpReward)
    }

    /** Задания комнаты развлечений: прогресс = набранный опыт, при достижении цели — награда. */
    private suspend fun updatePlayQuests(xp: Int) {
        val play = db.questDao().observeAll().first().filter { it.topic == "PLAY" && !it.completed }
        for (quest in play) {
            val newProgress = xp.coerceAtMost(quest.target)
            if (newProgress <= quest.progress) continue
            val done = newProgress >= quest.target
            db.questDao().upsert(quest.copy(progress = newProgress, completed = done))
            if (done) {
                val wallet = db.walletDao().getOnce() ?: continue
                db.walletDao().upsert(wallet.copy(cash = wallet.cash + quest.reward))
                db.transactionDao().insert(
                    TransactionEntity(kind = "INCOME", category = "quest_reward", amount = quest.reward)
                )
            }
        }
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