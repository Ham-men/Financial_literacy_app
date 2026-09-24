package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.BotBrain
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.screens.kiosk.KioskCustomer
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/** Бот на сцене кассы здания: идёт дверь → полка → касса. */
private data class StageBot(val id: Int, val units: Int)

/** Оплата покупателя: какую купюру дал и какая сдача. */
private data class Payment(val paid: Int, val change: Int)

private val BILLS = listOf(1, 2, 5, 10, 20, 50, 100, 200)

private fun makePayment(total: Int): Payment {
    val paid = if (Random.nextBoolean()) {
        total
    } else {
        BILLS.firstOrNull { it >= total } ?: ((total / 100 + 1) * 100)
    }
    return Payment(paid = paid, change = (paid - total).coerceAtLeast(0))
}

/** 5 вариантов ответа (плюс правильный), все разные и не отрицательные. */
private fun buildChangeOptions(correct: Int): List<Int> {
    val options = LinkedHashSet<Int>()
    options.add(correct)
    while (options.size < 5) {
        val delta = Random.nextInt(-30, 31) + Random.nextInt(-10, 11)
        val candidate = (correct + delta).coerceAtLeast(0)
        if (candidate != correct) options.add(candidate)
    }
    return options.shuffled()
}

/** «Живая касса» здания (День 7): слева комната с дверью и ботами, справа прилавок со сдачей. */
@Composable
fun BuildingCashierScreen(
    buildingId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()

    val buildings by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
    val building = buildings.firstOrNull { it.id == buildingId }
    val wallet by repo.observeWallet().collectAsState(initial = null)
    val cash = wallet?.cash ?: 500
    val stock = building?.stock ?: 0
    val price = building?.price ?: 8

    val queue = remember { mutableStateListOf<KioskCustomer>() }
    val botsOnStage = remember { mutableStateListOf<StageBot>() }
    var served by remember { mutableIntStateOf(0) }

    fun serve(customer: KioskCustomer) {
        scope.launch {
            if (queue.remove(customer)) {
                repo.completeSaleInBuilding(buildingId, customer.units)
                served++
            }
        }
    }

    // Уход со сцены: необслуженные возвращают товар на склад
    DisposableEffect(Unit) {
        onDispose {
            val units = queue.sumOf { it.units } + botsOnStage.sumOf { it.units }
            if (units > 0) scope.launch {
                repo.refundStockToBuilding(buildingId, units)
            }
        }
    }

    // Без внешнего verticalScroll: вложенный scroll + fillMaxSize даёт бесконечную высоту и краш.
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "🧾 Живая касса здания",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
        }

        // Два блока: слева комната, справа прилавок
        Row(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            // --- ЛЕВЫЙ БЛОК: комната с дверью и ботами ---
            Box(Modifier.weight(1.15f).fillMaxHeight()) {
                BuildCashierRoom(
                    modifier = Modifier.fillMaxSize(),
                    stock = stock,
                    price = price,
                    botsOnStage = botsOnStage,
                    queue = queue,
                    onBack = onBack,
                    onQueueAdd = { queue.add(it) },
                    onQueueDone = { botsOnStage.remove(it) },
                    onSpawnBot = { bot ->
                        val taken = repo.takeItemFromBuilding(buildingId, bot.units)
                        if (taken) botsOnStage.add(bot)
                        taken
                    }
                )
            }

            Spacer(Modifier.width(8.dp))

            // --- ПРАВЫЙ БЛОК: прилавок и сдача ---
            CashierCounter(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                price = price,
                queue = queue.toList(),
                served = served,
                onServe = ::serve
            )
        }

        Spacer(Modifier.height(6.dp))
        AppCard(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("🗒 Обслужил: $served шт. · В очереди: ${queue.size} · На полках: $stock 🧃",
                fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BuildCashierRoom(
    modifier: Modifier,
    stock: Int,
    price: Int,
    botsOnStage: List<StageBot>,
    queue: List<KioskCustomer>,
    onBack: () -> Unit,
    onQueueAdd: (KioskCustomer) -> Unit,
    onQueueDone: (StageBot) -> Unit,
    onSpawnBot: suspend (StageBot) -> Boolean
) {
    BoxWithConstraints(
        modifier.background(Color(0xFFF2E3C6))
    ) {
        val density = LocalDensity.current
        val sceneW = with(density) { maxWidth.toPx() }
        val sceneH = with(density) { maxHeight.toPx() }
        val door = with(density) { Offset(44.dp.toPx(), (sceneH - 68.dp.toPx()).coerceAtLeast(60.dp.toPx())) }
        val shelf = with(density) { Offset((sceneW - 60.dp.toPx()).coerceAtLeast(0f), (sceneH - 178.dp.toPx()).coerceAtLeast(40.dp.toPx())) }
        val cashPos = with(density) { Offset((sceneW - 30.dp.toPx()).coerceAtLeast(0f), (sceneH - 58.dp.toPx()).coerceAtLeast(60.dp.toPx())) }

        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(Color(0xFFFFE0B2))
        )
        // Дверь
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp)
                .width(64.dp)
                .height(110.dp)
                .clickable { onBack() }
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFF795548), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            ) {
                Text("🚪", Modifier.align(Alignment.Center), fontSize = 32.sp)
            }
        }
        // Полки: заполненные ячейки = остаток склада
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .width(90.dp)
                    .height(72.dp)
                    .background(Color(0xFF4E342E), RoundedCornerShape(6.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val filled = stock.coerceIn(0, 9)
                repeat(3) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(3) { col ->
                            val idx = row * 3 + col
                            Text(if (idx < filled) "🧃" else "▫️", fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text("на полках: $stock", fontSize = 9.sp, color = Color.White)
        }
        // Касса
        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🧾", fontSize = 24.sp)
            Text("$price ₡/шт", fontSize = 9.sp, color = Color.White)
        }
        // Финни
        Text("🦝", fontSize = 36.sp, modifier = Modifier.align(Alignment.Center).padding(bottom = 80.dp))
        // Пол
        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFF8D6E63))
        )

        // Боты на сцене: дверь → полка → касса
        botsOnStage.forEach { bot ->
            RoomRobotView(
                bot = bot,
                door = door,
                shelf = shelf,
                cashPos = cashPos,
                onQueue = { arrived ->
                    onQueueAdd(KioskCustomer(id = arrived.id, units = arrived.units))
                },
                onDone = { arrived -> onQueueDone(arrived) }
            )
        }
        // Очередь у кассы
        queue.take(6).forEachIndexed { i, _ ->
            Text(
                "🧑",
                fontSize = 20.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        (cashPos.x - 24.dp.toPx() - 22.dp.toPx() * i).roundToInt().coerceAtLeast(10),
                        cashPos.y.toInt()
                    )
                }
            )
        }

        // Производитель покупателей: пока на сцене + в очереди меньше прогноза спроса
        LaunchedEffect(Unit) {
            val buyers = BotBrain.estimateBuyers(price.toFloat()).coerceIn(2, 5)
            var id = 0
            while (true) {
                delay(1300)
                if (botsOnStage.size + queue.size >= buyers) break
                id++
                val taken = onSpawnBot(StageBot(id = id, units = Random.nextInt(1, 3)))
                if (!taken) break
            }
        }
    }
}

/** Бот идёт по сцене: дверь → полка → касса; у полки показывает «+N 🧃». */
@Composable
private fun RoomRobotView(
    bot: StageBot,
    door: Offset,
    shelf: Offset,
    cashPos: Offset,
    onQueue: (StageBot) -> Unit,
    onDone: (StageBot) -> Unit
) {
    val x = remember(bot.id) { Animatable(door.x) }
    val y = remember(bot.id) { Animatable(door.y) }
    var grabbed by remember(bot.id) { mutableStateOf(false) }

    LaunchedEffect(bot.id) {
        coroutineScope {
            launch { x.animateTo(shelf.x, tween(900, easing = LinearEasing)) }
            launch { y.animateTo(shelf.y, tween(900, easing = LinearEasing)) }
        }
        delay(300)
        grabbed = true
        delay(700)
        coroutineScope {
            launch { x.animateTo(cashPos.x, tween(900, easing = LinearEasing)) }
            launch { y.animateTo(cashPos.y, tween(900, easing = LinearEasing)) }
        }
        onQueue(bot)
        onDone(bot)
    }

    Box(
        Modifier.offset {
            IntOffset(x.value.roundToInt(), y.value.roundToInt())
        }
    ) {
        Text("🤖", fontSize = 26.sp)
        if (grabbed) {
            Text(
                "+${bot.units} 🧃",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.offset(y = (-14).dp)
            )
        }
    }
}

/** Правая половина: прилавок. Тащи товар в кассу, потом выбери сдачу (только точную). */
@Composable
private fun CashierCounter(
    modifier: Modifier,
    price: Int,
    queue: List<KioskCustomer>,
    served: Int,
    onServe: (KioskCustomer) -> Unit
) {
    Column(modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))) {
        Text(
            "🛒 Прилавок",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 10.dp, top = 8.dp)
        )

        // Контент в ограниченной по высоте области со скроллом — без вложенного внешнего scroll.
        Box(Modifier.fillMaxSize()) {
            val customer = queue.firstOrNull()
            if (customer == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😴", fontSize = 40.sp)
                        Text("Очередь пуста", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (served > 0) "Вся смена пробита!" else "Боты подходят слева.",
                            fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 10.dp)
                ) {

        val rungSet = remember(customer.id) { mutableStateListOf<Int>() }
        val cartBounds = remember { mutableStateMapOf<Int, Rect>() }
        var boardOrigin by remember(customer.id) { mutableStateOf(Offset.Zero) }
        var registerRect by remember(customer.id) { mutableStateOf<Rect?>(null) }
        var draggedId by remember(customer.id) { mutableStateOf<Int?>(null) }
        var dropPos by remember(customer.id) { mutableStateOf(Offset.Zero) }
        var payment by remember(customer.id) { mutableStateOf<Payment?>(null) }
        var answer by remember(customer.id) { mutableStateOf<Int?>(null) }

        val total = rungSet.size.coerceAtMost(customer.units) * price
        val allRung = rungSet.size >= customer.units
        val change = payment?.change ?: 0
        val options = remember(payment) {
            val p = payment ?: return@remember emptyList<Int>()
            buildChangeOptions(p.change)
        }

        LaunchedEffect(customer.id, rungSet.size) {
            if (allRung && payment == null) {
                payment = makePayment(total)
                answer = null
            }
        }

        // После выбора ответа показываем вердикт и переходим к следующему покупателю.
        LaunchedEffect(customer.id, answer) {
            if (answer != null) {
                delay(1500)
                onServe(customer)
            }
        }

        // Карточка покупателя
        AppCard(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                "🧑 Покупатель: ${customer.units} × 🧃 = ${customer.units * price} ₡",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Доска перетаскивания
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(180.dp)
                .background(Color(0xFFEFE6D0), RoundedCornerShape(14.dp))
                .onGloballyPositioned { boardOrigin = it.boundsInRoot().topLeft }
        ) {
            // Кассовый аппарат (справа)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 10.dp)
                    .onGloballyPositioned { registerRect = it.boundsInRoot() },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF37474F),
                border = BorderStroke(2.dp, Color(0xFF90A4AE))
            ) {
                Column(
                    Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Касса", fontSize = 10.sp, color = Color.White)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$total ₡",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF176)
                    )
                    Text(
                        if (rungSet.isEmpty()) "сюда 🧃" else "+${rungSet.size}",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Корзина с товаром (слева) + летящая бутылка
            Row(
                Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0 until customer.units).forEach { index ->
                    if (index !in rungSet) {
                        StockJuiceTile(
                            id = index,
                            size = 40.dp,
                            isDragging = draggedId == index,
                            onBounds = { rect ->
                                cartBounds[index] = Rect(
                                    rect.left - boardOrigin.x, rect.top - boardOrigin.y,
                                    rect.right - boardOrigin.x, rect.bottom - boardOrigin.y
                                )
                            },
                            onDragStart = {
                                draggedId = index
                                val r = cartBounds[index]
                                dropPos = if (r != null) Offset(r.left + r.width / 2f, r.top + r.height / 2f)
                                else Offset.Zero
                            },
                            onDrag = { amount -> if (draggedId == index) dropPos += amount },
                            onDragEnd = {
                                val rootDrop = Offset(dropPos.x + boardOrigin.x, dropPos.y + boardOrigin.y)
                                val reg = registerRect
                                if (reg != null && reg.contains(rootDrop)) {
                                    if (index !in rungSet) rungSet.add(index)
                                }
                                draggedId = null
                            },
                            onDragCancel = { if (draggedId == index) draggedId = null }
                        )
                    }
                }
                val dragging = draggedId
                if (dragging != null) {
                    Text(
                        "🧃",
                        fontSize = 26.sp,
                        modifier = Modifier
                            .zIndex(10f)
                            .align(Alignment.CenterVertically)
                            .offset {
                                IntOffset(
                                    (dropPos.x - 15).roundToInt(),
                                    (dropPos.y - 17).roundToInt()
                                )
                            }
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        if (allRung) {
            val p = payment
            if (p != null) {
                // --- Сдача: вопрос без подсказки, 5 вариантов, один правильный ---
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    // Условие задачи
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(
                                "🛒 Товары на $total ₡",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF5D4037)
                            )
                            Text(
                                "🧑 Покупатель дал ${p.paid} ₡",
                                fontSize = 13.sp,
                                color = Color(0xFF5D4037)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Сколько дать сдачи? 🤔",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Accent
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 5 вариантов ответа
                    val picked = answer
                    if (picked == null) {
                        options.chunked(3).forEachIndexed { rowIdx, row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { option ->
                                    Surface(
                                        onClick = { answer = option },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, Color(0xFFD8CFBD)),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                "$option ₡",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                                if (row.size < 3) {
                                    repeat(3 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            if (rowIdx < options.chunked(3).lastIndex) {
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    } else {
                        // Вердикт: верно / неверно
                        val correct = picked == p.change
                        Box(
                            Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (correct) "✅ Верно! Сдача $change ₡" else "❌ Неверно! Нужно $change ₡",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (correct) Color(0xFF1B5E20) else Danger
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Товары $total ₡ − деньги ${p.paid} ₡ = сдача $change ₡",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("Следующий покупатель…", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        } else {
            // Прогресс пробивки
            LinearProgressIndicator(
                progress = { (rungSet.size.toFloat() / customer.units).coerceIn(0f, 1f) },
                color = Primary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(6.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Тащи 🧃 с полки в кассу: ${rungSet.size} / ${customer.units}",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.height(4.dp))
                }
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun StockJuiceTile(
    id: Int,
    size: Dp,
    isDragging: Boolean,
    onBounds: (Rect) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .pointerInput(id) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() }
                )
            },
        shape = RoundedCornerShape(10.dp),
        color = if (isDragging) Color(0xFFFFE0B2) else Color.White,
        border = BorderStroke(1.dp, Color(0xFFBCAAA4))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("🧃", fontSize = 18.sp)
        }
    }
}