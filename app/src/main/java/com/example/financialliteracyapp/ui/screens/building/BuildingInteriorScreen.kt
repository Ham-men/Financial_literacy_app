package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.R
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.GameRepository
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.launch

/** Экран внутри здания — горячие точки: прилавок, полки, касса, склад, дверь, найм, улучшения, учёт. */
@Composable
fun BuildingInteriorScreen(
    buildingId: Long,
    onExit: () -> Unit,
    onOpenSuppliers: () -> Unit,
    onOpenShelves: () -> Unit,
    onOpenPricer: () -> Unit,
    onOpenCashier: () -> Unit,
    onOpenCleaning: () -> Unit,
    onOpenHire: () -> Unit,
    onOpenUpgrades: () -> Unit,
    onOpenAccounting: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()
    val buildings by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
    val building = buildings.firstOrNull { it.id == buildingId }
    val wallet by repo.observeWallet().collectAsState(initial = null)
    val employees by repo.observeWorkersForBuilding(buildingId).collectAsState(initial = emptyList())
    val allBots by repo.observeBots().collectAsState(initial = emptyList())
    var dayResult by remember { mutableStateOf<GameRepository.DayResult?>(null) }
    var runningDay by remember { mutableStateOf(false) }
    var hireSalary by remember { mutableIntStateOf(100) }
    val cash = wallet?.cash ?: 500
    val needPlan = wallet?.needPlan ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val savePlan = wallet?.savePlan ?: 0
    // Бюджет трат из «нужного»: если план разложен — банка нужное + мешок, иначе весь мешок
    val planSet = needPlan + wantPlan + savePlan > 0
    val needAvailable = if (planSet) needPlan + cash else cash

    val stock = building?.stock ?: 0
    val price = building?.price ?: 8
    val costPrice = building?.costPrice ?: 3
    val buildingCash = building?.cash ?: 0
    val soldToday = building?.soldToday ?: 0
    val isOpen = building?.isOpen ?: true
    val gameMinute by GameClock.minute.collectAsState()
    val clockIsOpen = GameRules.isShopOpen(gameMinute)
    val shopActuallyOpen = isOpen && clockIsOpen
    val dirtLevel = building?.dirtLevel ?: 0
    val isProducts = building?.type == "PRODUCTS"

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onExit,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀ Выход", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 14.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                building?.let { getBuildingEmoji(it.type) + " " + getBuildingName(it.type) } ?: "Здание",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 Мешок $cash ₡ · Нужное $needPlan ₡")
        }

        // Комната здания (вид сбоку как в KioskScreen)
        Box(
            Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFF2E3C6))
        ) {
            // Потолок/витрина
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFFFE0B2))
            )
            // Вывеска
            Box(
                Modifier
                    .width(120.dp)
                    .height(80.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 26.dp)
                    .background(Color(0xFFB3E5FC), RoundedCornerShape(10.dp))
            ) {
                Text(getBuildingEmoji(building?.type ?: ""), Modifier.align(Alignment.Center), fontSize = 32.sp)
            }

            // Дверь (выход/вход) — слева внизу
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .width(80.dp)
                    .height(130.dp)
                    .clickable { onExit() }
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.TopCenter)
                        .background(Color(0xFF795548), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                ) {
                    Text("🚪", Modifier.align(Alignment.Center), fontSize = 40.sp)
                }
                Column(
                    Modifier.align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isProducts) {
                        Text("📦", fontSize = 24.sp)
                        HotspotTag("Закупка", enabled = true, onClick = onOpenSuppliers)
                    } else {
                        Text("🚗", fontSize = 24.sp)
                        HotspotTag("Приём машин", enabled = false, onClick = {})
                    }
                }
            }

            // Полки/склад (только товарные) — справа внизу
            if (isProducts) {
                Column(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 130.dp)
                        .clickable(enabled = stock > 0) { onOpenShelves() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier
                            .width(110.dp)
                            .height(88.dp)
                            .background(Color(0xFF4E342E), RoundedCornerShape(6.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val filled = stock.coerceIn(0, 9)
                        repeat(3) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(3) { col ->
                                    val idx = row * 3 + col
                                    Text(if (idx < filled) "📦" else "▫️", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    HotspotTag(
                        if (stock > 0) "Склад/Полки ($stock)" else "Пусто: закупись",
                        enabled = stock > 0,
                        onClick = onOpenShelves
                    )
                }
            } else {
                // СТО: подъёмник с машиной клиента
                Column(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 130.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .width(120.dp)
                            .height(88.dp)
                            .background(Color(0xFF424242), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚗🛠️", fontSize = 30.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Подъёмник", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // Стул найма — слева от кассы
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 100.dp, bottom = 38.dp)
                    .clickable { onOpenHire() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👤", fontSize = 30.sp)
                HotspotTag("Нанять", enabled = true, onClick = onOpenHire)
            }

            // Уборка — по центру пола
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 38.dp)
                    .clickable { onOpenCleaning() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧽", fontSize = 30.sp)
                HotspotTag(if (dirtLevel > 0) "Уборка ($dirtLevel)" else "Уборка", enabled = true, onClick = onOpenCleaning)
            }

            // Финни в центре
            Image(
                painterResource(R.drawable.ic_pet_raccoon),
                contentDescription = null,
                modifier = Modifier.size(46.dp).align(Alignment.Center).padding(bottom = 90.dp)
            )

            // Пол
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF8D6E63))
            )

            // Ценник — слева сверху (только у товарных зданий)
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 44.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isProducts) "🏷️" else "💲", fontSize = 30.sp)
                HotspotTag(
                    if (isProducts) "Ценник" else "Прайс",
                    enabled = isProducts,
                    onClick = onOpenPricer
                )
            }

            // Касса/Улучшения/Учёт — справа внизу
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 38.dp)
                    .clickable { onOpenCashier() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧾", fontSize = 30.sp)
                HotspotTag("Касса", enabled = true, onClick = onOpenCashier)
            }

            // Улучшения — маленькая иконка над кассой
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 120.dp)
                    .clickable { onOpenUpgrades() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⬆️", fontSize = 24.sp)
                HotspotTag("Улучшения", enabled = true, onClick = onOpenUpgrades)
            }

            // Учёт — маленькая иконка выше
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 200.dp)
                    .clickable { onOpenAccounting() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📊", fontSize = 24.sp)
                HotspotTag("Учёт", enabled = true, onClick = onOpenAccounting)
            }
        }

        // Инфо-строка
Text(
                if (stock > 0)
                    (if (isProducts)
                        "Товар на полках: $stock шт. Цена: $price ₡. Себестоимость: $costPrice ₡."
                    else
                        "Заказов в работе: $stock. Прайс ремонта: $price ₡. Себестоимость: $costPrice ₡.") +
                    " Продаж сегодня: $soldToday." +
                    (if (shopActuallyOpen) "\n🟢 Открыто (${GameRules.timeLabel(gameMinute)})"
                     else "\n🔴 Закрыто" + (if (clockIsOpen) "" else " — после 18:00")) +
                    (if (dirtLevel > 50) "\n🧽 Грязно! Боты сердятся — уберись." else "")
                else
                    "Склад пуст. Закупите детали/товар перед открытием.",
                fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
        )

        // Карточка склада и цены
        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("📦 Склад и касса", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row {
                Text("Товара в наличии", Modifier.weight(1f))
                Text("$stock шт.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Text("Касса здания: $buildingCash ₡", color = TextSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Row {
                Text("Цена за единицу", Modifier.weight(1f))
                Text("$price ₡", fontWeight = FontWeight.Bold, color = Accent)
            }
            Spacer(Modifier.height(6.dp))
            Row {
                Text("Себестоимость", Modifier.weight(1f))
                Text("$costPrice ₡", color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Закупка — подвкладка «Закупка» 📦. Ценник — «Ценник» 🏷️. Касса — «Касса» 🧾.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Карточка сотрудников и зарплаты
        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("👩‍💼 Сотрудники и зарплата", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            if (employees.isEmpty()) {
                Text("Нет сотрудников. Найми их через «Нанять» 👤.", fontSize = 13.sp, color = TextSecondary)
            } else {
                employees.forEach { bot ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${botEmoji(bot)} Кошелёк: ${bot.wallet} ₡", Modifier.weight(1f), fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { scope.launch { repo.paySalaryToBot(bot.id, bot.salary) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("ЗП ${bot.salary} ₡", fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Зарплата списывается из банки «нужное» (доступно $needAvailable ₡: нужное $needPlan + мешок $cash) в кошелёк бота — тот сможет покупать в этом здании.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(12.dp))

        // Карточка найма ботов района на работу в это здание (День 14)
        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("👤 Нанять на работу", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text("Безработные согласятся, если зарплата ≥ 100 ₡/день. Разовый сбор за найм — 50 ₡.", fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Зарплата", Modifier.weight(1f), fontSize = 13.sp)
                Text("$hireSalary ₡/день", fontWeight = FontWeight.Bold, color = Accent)
            }
            Slider(
                value = hireSalary.toFloat(),
                onValueChange = { hireSalary = it.toInt() },
                valueRange = 50f..300f,
                steps = 4
            )
            val candidates = allBots.filter { it.workBuildingId != buildingId }
            if (candidates.isEmpty()) {
                Text("Все боты уже работают в этом здании 🎉", fontSize = 13.sp, color = TextSecondary)
            } else {
                candidates.forEach { bot ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${botEmoji(bot)} ${bot.type} · 🏦 ${bot.wallet} ₡", Modifier.weight(1f), fontSize = 13.sp)
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    repo.hireBotToBuilding(bot.id, buildingId, hireSalary)
                                }
                            },
                            enabled = needAvailable >= 50,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Нанять", fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Карточка покупателей (траты ботов)
        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("🛍️ Боты района · покупатели", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            val buyers = allBots.filter {
                it.workBuildingId != buildingId && it.wallet > 0 &&
                    (isProducts || it.hasCar)
            }
            Text(
                ("${if (isProducts) "Покупателей" else "Водителей-клиентов"}: ${buyers.size}. " +
                    "У них в сумме ${buyers.sumOf { it.wallet }} ₡."),
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("📦 в наличии $stock · 💲 $price ₡ · 🧾 касса $buildingCash ₡", fontSize = 12.sp, color = TextSecondary)
            }
            Text(
                if (isProducts)
                    "Чем выше зарплаты и ниже цены — тем больше боты покупают (см. отчёт дня 📊)."
                else
                    "На СТО приезжают только боты с машиной 🚗. Прайс и зарплаты решают всё.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(12.dp))

        // Запуск дня: зарплата → траты ботов → выручка → прибыль (замкнутый цикл)
        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("🌙 Рабочий день", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Боты выходят на работу, получают зарплату, тратят её в зданиях и уходят домой.",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    runningDay = true
                    scope.launch {
                        dayResult = repo.simulateDay()
                        runningDay = false
                    }
                },
                enabled = !runningDay,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (runningDay) "Считаем…" else "🚀 Запустить день", fontWeight = FontWeight.Bold)
            }

            val r = dayResult
            if (r != null) {
                Spacer(Modifier.height(10.dp))
                DayResultRow("ЗП сотрудникам", r.totalSalary)
                DayResultRow("Выручка ботов", r.totalRevenue, income = true)
                DayResultRow("Себестоимость", r.totalCost)
                DayResultRow("Аренда", r.totalRent)
                DayResultRow("Налог", r.totalTax)
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                DayResultRow("Прибыль дня", r.totalProfit, income = r.totalProfit >= 0, bold = true)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Зданий работает: ${r.buildingsActive} · боты на смене: ${r.botsWorking}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DayResultRow(label: String, amount: Int, income: Boolean = false, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = TextSecondary
        )
        Text(
            "${if (income) "+" else "−"}$amount ₡",
            fontSize = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (income) Primary else TextSecondary
        )
    }
}

private fun botEmoji(bot: BotEntity): String = when {
    bot.hasCar -> "🚗"
    else -> when (bot.type) {
        "WORKER" -> "🚶"
        "ENGINEER" -> "🔧"
        "MANAGER" -> "🕴️"
        "RETIREE" -> "🧓"
        "STUDENT" -> "🧑‍🎓"
        "ELITE" -> "💼"
        else -> "🚶"
    }
}

@Composable
private fun HotspotTag(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(6.dp),
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun getBuildingEmoji(type: String): String = when (type) {
    "PRODUCTS" -> "🛒"
    "AUTO_SERVICE" -> "🔧"
    "CONSTRUCTION" -> "🏗️"
    "HEALTH" -> "🏥"
    "ART" -> "🎨"
    "RESIDENTIAL" -> "🏠"
    else -> "🏢"
}

private fun getBuildingName(type: String): String = when (type) {
    "PRODUCTS" -> "Продукты"
    "AUTO_SERVICE" -> "СТО"
    "CONSTRUCTION" -> "Стройтехника"
    "HEALTH" -> "Здоровье"
    "ART" -> "Искусство"
    "RESIDENTIAL" -> "Жилой дом"
    else -> "Здание"
}