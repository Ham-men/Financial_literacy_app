package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.BotBrain
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.screens.kiosk.CashierScene
import com.example.financialliteracyapp.ui.screens.kiosk.KioskCustomer
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/** Бот на сцене кассы здания: идёт дверь → полка → касса. */
private data class StageBot(val id: Int, val units: Int)

/** «Живая касса» здания (День 7): боты берут товар на полках, идут к кассе, игрок пробивает. */
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
    var spawned by remember { mutableIntStateOf(0) }
    var served by remember { mutableIntStateOf(0) }
    var totalBuyers by remember { mutableIntStateOf(0) }
    var refunded by remember { mutableStateOf(false) }

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
                refunded = true
            }
        }
    }

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
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "🧾 Живая касса здания",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
        }

        // Сцена с покупателями
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
            val cashPos = with(density) { Offset(sceneW - 38.dp.toPx(), 332.dp.toPx()) }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFFFE0B2))
            )
            // Дверь
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .width(80.dp)
                    .height(130.dp)
                    .clickable { onBack() }
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
                Text("📦", Modifier.align(Alignment.BottomCenter), fontSize = 24.sp)
            }
            // Полки: заполненные ячейки = остаток склада
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 130.dp),
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
                Text("на полках: $stock", fontSize = 11.sp, color = Color.White)
            }
            // Касса
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 38.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧾", fontSize = 30.sp)
                Text("Касса $price ₡/шт", fontSize = 11.sp, color = Color.White)
            }
            // Финни
            Text("🦝", fontSize = 46.sp, modifier = Modifier.align(Alignment.Center).padding(bottom = 90.dp))
            // Пол
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF8D6E63))
            )

            // Боты на сцене: дверь → полка → касса
            botsOnStage.forEach { bot ->
                ArrivingBotView(
                    bot = bot,
                    door = door,
                    shelf = shelf,
                    cashPos = cashPos,
                    onQueue = { arrived ->
                        queue.add(KioskCustomer(id = arrived.id, units = arrived.units))
                    },
                    onDone = { arrived -> botsOnStage.remove(arrived) }
                )
            }
            // Очередь у кассы
            queue.take(6).forEachIndexed { i, _ ->
                Text(
                    "🧑",
                    fontSize = 24.sp,
                    modifier = Modifier.offset {
                        IntOffset(
                            (cashPos.x - 30.dp.toPx() - 28.dp.toPx() * i).roundToInt(),
                            cashPos.y.toInt()
                        )
                    }
                )
            }

            // Производитель покупателей
            LaunchedEffect(Unit) {
                if (totalBuyers == 0) {
                    totalBuyers = BotBrain.estimateBuyers(price.toFloat()).coerceIn(2, 5)
                }
                while (spawned < totalBuyers) {
                    delay(1500)
                    val units = Random.nextInt(1, 3)
                    if (repo.takeItemFromBuilding(buildingId, units)) {
                        spawned++
                        botsOnStage.add(StageBot(id = spawned, units = units))
                    } else {
                        break
                    }
                }
            }
        }

        Text(
            if (stock > 0)
                "Боты берут 🧃 с полок и встают к кассе. Обслужи их внизу: тащи товар в кассу, выдай сдачу."
            else
                "Полки пусты. Вернись на склад и закупи товар (📦) — боты не могут взять ничего.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Живая касса: игрок пробивает очередь
        CashierScene(
            price = price,
            queue = queue.toList(),
            cashierHired = false,
            servedToday = served,
            onServe = ::serve,
            onGoScene = { /* мы уже на сцене */ },
            onOpenReport = {}
        )

        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("🗒 Обслужил сегодня: $served шт. · В очереди: ${queue.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("Смена завершится, когда возьмёшь «◀» — товар у необслуженных вернётся на склад.", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Бот идёт по сцене: дверь → полка → касса; у полки показывает «+N 🧃». */
@Composable
private fun ArrivingBotView(
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
            launch { x.animateTo(shelf.x, tween(1100, easing = LinearEasing)) }
            launch { y.animateTo(shelf.y, tween(1100, easing = LinearEasing)) }
        }
        delay(350)
        grabbed = true
        delay(800)
        coroutineScope {
            launch { x.animateTo(cashPos.x, tween(1100, easing = LinearEasing)) }
            launch { y.animateTo(cashPos.y, tween(1100, easing = LinearEasing)) }
        }
        onQueue(bot)
        onDone(bot)
    }

    Box(
        Modifier.offset {
            IntOffset(x.value.roundToInt(), y.value.roundToInt())
        }
    ) {
        Text("🤖", fontSize = 32.sp)
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