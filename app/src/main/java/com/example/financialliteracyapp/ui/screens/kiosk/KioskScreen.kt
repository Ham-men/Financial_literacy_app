package com.example.financialliteracyapp.ui.screens.kiosk

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.R
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.BotBrain
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.screens.minigames.PricerGame
import com.example.financialliteracyapp.ui.screens.minigames.SuppliersGame
import com.example.financialliteracyapp.ui.screens.shop.AccountingScreen
import com.example.financialliteracyapp.ui.screens.shop.MarketScreen
import com.example.financialliteracyapp.ui.screens.shop.ShopViewModel
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/** Сцена ларька (разбор.txt) — хаб: комната-сцена с живыми покупателями + вкладки. */
@Composable
fun KioskScreen(
    onBack: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenShelves: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val shop by vm.shop.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val cashierHired by prefs.cashierHired.collectAsState(initial = false)

    val cash = wallet?.cash ?: 500
    val stock = shop?.stock ?: 0
    val priceInt = shop?.price ?: 8

    var tab by remember { mutableIntStateOf(0) }

    // --- Сессия ларька: покупатели, боты на сцене, обслуживание ---
    val scope = rememberCoroutineScope()
    val queue = remember { mutableStateListOf<KioskCustomer>() }
    val botsOnStage = remember { mutableStateListOf<StageBot>() }
    var spawned by remember { mutableIntStateOf(0) }
    var served by remember { mutableIntStateOf(0) }
    var totalBuyers by remember { mutableIntStateOf(0) }

    fun serve(customer: KioskCustomer) {
        scope.launch {
            if (queue.remove(customer)) {
                repo.completeSale(customer.units)
                served++
            }
        }
    }

    // Кассир пробивает сам: одна покупка раз в ~1.2 с (из любой вкладки)
    LaunchedEffect(cashierHired, queue.firstOrNull()?.id) {
        if (cashierHired) {
            val c = queue.firstOrNull() ?: return@LaunchedEffect
            delay(1200)
            serve(c)
        }
    }

    // Выход из ларька: необслуженные покупатели возвращают товар на полки
    DisposableEffect(Unit) {
        onDispose {
            val units = queue.sumOf { it.units }
            if (units > 0) scope.launch { repo.refundStock(units) }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "🍋 Ларёк с лимонадом",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
        }

        // Подвкладки ларька
        TabRow(selectedTabIndex = tab) {
            listOf(
                "🏬" to "Ларёк",
                "🛍" to "Витрина",
                "📦" to "Закупка",
                "🏷" to "Цена",
                "🧾" to "Касса",
                "📊" to "Учёт",
                "🤖" to "Найм"
            ).forEachIndexed { i, (emoji, label) ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (i == 6) {
                                Image(
                                    painterResource(R.drawable.ic_bot_worker),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(emoji, fontSize = 14.sp)
                            }
                            Text(label, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                )
            }
        }

        when (tab) {
            // --- Комната ларька (вид сбоку): покупатели приходят по сцене ---
            0 -> Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                BoxWithConstraints(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFFF2E3C6))
                ) {
                    val density = LocalDensity.current
                    val sceneW = with(density) { maxWidth.toPx() }
                    val door = with(density) { Offset(52.dp.toPx(), 330.dp.toPx()) }
                    val shelf = with(density) { Offset(sceneW - 71.dp.toPx(), 250.dp.toPx()) }
                    val cash = with(density) { Offset(sceneW - 38.dp.toPx(), 332.dp.toPx()) }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .align(Alignment.TopCenter)
                            .background(Color(0xFFFFE0B2))
                    )
                    Box(
                        Modifier
                            .width(120.dp)
                            .height(80.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 26.dp)
                            .background(Color(0xFFB3E5FC), RoundedCornerShape(10.dp))
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                            .width(80.dp)
                            .height(130.dp)
                            .clickable { tab = 2 }
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
                            Text("📦", fontSize = 24.sp)
                            HotspotTag("Закупить товар", enabled = true)
                        }
                    }
                    // Полки: count заполненных ячеек = остаток склада (сохраняется между заходами)
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
                                        Text(if (idx < filled) "🧃" else "▫️", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        HotspotTag(
                            if (stock > 0) "Полки: расставить ($stock шт.)" else "Полки пусты: закупись",
                            enabled = stock > 0
                        )
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 100.dp, bottom = 38.dp)
                            .clickable { tab = 6 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (cashierHired) {
                            Image(
                                painterResource(R.drawable.ic_bot_worker),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text("🪑", fontSize = 30.sp)
                        }
                        HotspotTag(if (cashierHired) "Кассир · касса" else "Нанять кассира", enabled = true)
                    }
                    Image(
                        painterResource(R.drawable.ic_pet_raccoon),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp).align(Alignment.Center).padding(bottom = 90.dp)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF8D6E63))
                    )
                    Column(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 44.dp)
                            .clickable { tab = 3 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏷️", fontSize = 30.sp)
                        HotspotTag("Ценник", enabled = true)
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 38.dp)
                            .clickable { tab = 4 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (cashierHired) {
                            Image(
                                painterResource(R.drawable.ic_bot_worker),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Text("🧾", fontSize = 30.sp)
                        }
                        HotspotTag(
                            if (cashierHired) "Касса · кассир" else "Касса",
                            enabled = true
                        )
                    }

                    // --- Живые покупатели ---
                    botsOnStage.forEach { bot ->
                        ArrivingBotView(
                            bot = bot,
                            door = door,
                            shelf = shelf,
                            cash = cash,
                            onQueue = { arrived ->
                                queue.add(KioskCustomer(id = arrived.id, units = arrived.units))
                            },
                            onDone = { arrived ->
                                botsOnStage.remove(arrived)
                            }
                        )
                    }
                    queue.take(6).forEachIndexed { i, _ ->
                        Image(
                            painterResource(R.drawable.ic_bot_student),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .offset {
                                    IntOffset(
                                        (cash.x - 30.dp.toPx() - 28.dp.toPx() * i).roundToInt(),
                                        cash.y.toInt()
                                    )
                                }
                        )
                    }

                    // Производитель покупателей: пока не встречены все
                    LaunchedEffect(Unit) {
                        if (totalBuyers == 0) {
                            totalBuyers = BotBrain.estimateBuyers(priceInt.toFloat()).coerceIn(2, 5)
                        }
                        while (spawned < totalBuyers) {
                            delay(1500)
                            val units = Random.nextInt(1, 3)
                            if (repo.takeItem(units)) {
                                spawned++
                                botsOnStage.add(StageBot(id = spawned, units = units))
                            } else {
                                break
                            }
                        }
                    }
                    // Уход со сцены: боты в пути возвращают товар на полки
                    DisposableEffect(Unit) {
                        onDispose {
                            val inFlight = botsOnStage.toList()
                            if (inFlight.isNotEmpty()) {
                                scope.launch {
                                    repo.refundStock(inFlight.sumOf { it.units })
                                    botsOnStage.removeAll(inFlight.toSet())
                                }
                            }
                        }
                    }
                }

                Text(
                    if (stock > 0)
                        "Покупатели заходят, берут товар с полок и встают к кассе 🧾." +
                        (if (cashierHired) "\nКассир пробивает очередь сам." else "\nОбслужи их на кассе: вкладка «Касса» 🧾.")
                    else
                        "Полки пусты. «Закупка» 📦 — купи лимонад, потом расставь его на полки." +
                        (if (cashierHired) "\nКассир уже на месте, но товара нет." else "\nСтул у кассы пустует — вкладка «Найм» 🤖."),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
                )

                AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("📦 Склад и цена", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("Товара в наличии", Modifier.weight(1f))
                        Text("$stock шт.", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Text("Себест.: ${shop?.costPrice ?: 3} ₡", color = TextSecondary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text("Цена за единицу", Modifier.weight(1f))
                        Text("$priceInt ₡", fontWeight = FontWeight.Bold, color = Accent)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text("Продано сегодня", Modifier.weight(1f))
                        Text("$served шт.", fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (cashierHired)
                            "Кассир считает кассу сам — смотреть можно в любой вкладке."
                        else
                            "Сменить цену — подвкладка «Цена» 🏷️, обслужить покупателей — «Касса» 🧾.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
            // --- Витрина покупок (бывший Магазин) ---
            1 -> MarketScreen(onBack = {}, embedded = true)
            // --- Закупка у поставщика ---
            2 -> SuppliersGame(onFinish = { tab = 0 }, embedded = true)
            // --- Поставь цену ---
            3 -> PricerGame(onFinish = { tab = 0 }, embedded = true)
            // --- Живая касса ---
            4 -> CashierScene(
                price = priceInt,
                queue = queue.toList(),
                cashierHired = cashierHired,
                servedToday = served,
                onServe = ::serve,
                onGoScene = { tab = 0 },
                onOpenReport = onOpenReport
            )
            // --- Учёт ларька (касса/склад/P&L) ---
            5 -> AccountingScreen(onBack = {}, embedded = true, onOpenReport = onOpenReport)
            // --- Найм кассира ---
            6 -> HireScreen(onBack = { tab = 0 })
        }
    }
}

/** Бот идёт по сцене: дверь → полка → касса. У полки показывает «+N 🧃». */
@Composable
private fun ArrivingBotView(
    bot: StageBot,
    door: Offset,
    shelf: Offset,
    cash: Offset,
    onQueue: (StageBot) -> Unit,
    onDone: (StageBot) -> Unit
) {
    val x = remember(bot.id) { Animatable(door.x) }
    val y = remember(bot.id) { Animatable(door.y) }
    var grabbed by remember(bot.id) { mutableStateOf(false) }

    LaunchedEffect(bot.id) {
        coroutineScope {
            launch { x.animateTo(shelf.x, tween(1100, easing = LinearEasing)) }
            launch { y.animateTo(shelf.y, tween(1100, easing = LinearEasing)) }
        }
        delay(350)
        grabbed = true
        delay(800)
        coroutineScope {
            launch { x.animateTo(cash.x, tween(1100, easing = LinearEasing)) }
            launch { y.animateTo(cash.y, tween(1100, easing = LinearEasing)) }
        }
        onQueue(bot)
        onDone(bot)
    }

    Box(
        Modifier.offset {
            IntOffset(x.value.roundToInt(), y.value.roundToInt())
        }
    ) {
        Image(
            painterResource(R.drawable.ic_bot_student),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        if (grabbed) {
            Text(
                "+${bot.units} 🧃",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.offset(y = (-18).dp)
            )
        }
    }
}

@Composable
private fun HotspotTag(label: String, enabled: Boolean) {
    Surface(
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