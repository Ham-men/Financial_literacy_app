package com.example.financialliteracyapp.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.ui.theme.*
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Сцена «Карта» — сетка районов 3×12 с scroll.
 * Соответствует районы.html: участки с домами, зданиями, «продаётся».
 * Сохраняет функционал: джойстик, боты, здания из БД.
 */
@Composable
fun MapScreen(
    onGoHome: () -> Unit,
    onOpenBuilding: (Long) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()

    val wallet by repo.observeWallet().collectAsState(initial = null)
    val bots by repo.observeBots().collectAsState(initial = emptyList())
    val cash = wallet?.cash ?: 500

    // Позиция Финни
    var finniX by remember { mutableStateOf(200f) }
    var finniY by remember { mutableStateOf(300f) }
    var joystickActive by remember { mutableStateOf(false) }
    var joystickKnobX by remember { mutableStateOf(0f) }
    var joystickKnobY by remember { mutableStateOf(0f) }

    val moveSpeed = 3f
    LaunchedEffect(joystickActive, joystickKnobX, joystickKnobY) {
        if (joystickActive) {
            val dx = (joystickKnobX) / 60f
            val dy = (joystickKnobY) / 60f
            if (abs(dx) > 0.1f || abs(dy) > 0.1f) {
                finniX += dx * moveSpeed
                finniY += dy * moveSpeed
                finniX = finniX.coerceIn(20f, 780f)
                finniY = finniY.coerceIn(20f, 580f)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Шапка
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Дата 01.01.2020 12:00", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Surface(
                shape = RoundedCornerShape(50),
                color = Primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            ) {
                Text(
                    "💰 $cash ₡",
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp
                )
            }
        }

        // Карта с scroll
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
        ) {
            // Scrollable map grid — как районы.html: чёрный фон = дороги, белые участки = районы
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Grid 3x12
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(12) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(3) { col ->
                                val plotType = getPlotType(row, col)
                                PlotTile(
                                    type = plotType,
                                    row = row,
                                    col = col,
                                    onClick = { /* Открыть здание/участок */ }
                                )
                            }
                        }
                    }
                }
            }

            // Финни (енот)
            Box(
                Modifier
                    .size(36.dp)
                    .offset { IntOffset((finniX - 18).toInt(), (finniY - 18).toInt()) }
            ) {
                Text("🦝", fontSize = 28.sp)
                Text(
                    "Финни", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-16).dp).background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(3.dp)).padding(horizontal = 2.dp)
                )
            }

            // Боты-NPC
            bots.forEachIndexed { index, bot ->
                MapBotActor(bot, index)
            }

            // Джойстик снизу слева
            VirtualJoystick(centerX = 60f, centerY = 500f,
                onActiveChanged = { joystickActive = it },
                onKnobChanged = { joystickKnobX = it.x; joystickKnobY = it.y }
            )

            // Кнопка «Дом» справа снизу
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Surface(
                    onClick = onGoHome,
                    shape = RoundedCornerShape(24.dp),
                    color = Primary
                ) {
                    Text("🏠 Дом", fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                }
            }
        }
    }
}

private enum class PlotType {
    HOUSE, FOR_SALE, BUILDING, PARK, FACTORY, COMPLEX
}

private fun getPlotType(row: Int, col: Int): PlotType {
    return when (row % 12) {
        0 -> if (col == 0) PlotType.HOUSE else PlotType.FOR_SALE
        1 -> when (col) { 0 -> PlotType.BUILDING; 1 -> PlotType.COMPLEX; else -> PlotType.FACTORY }
        2 -> when (col) { 0 -> PlotType.PARK; 1 -> PlotType.FOR_SALE; else -> PlotType.BUILDING }
        3 -> when (col) { 0 -> PlotType.FOR_SALE; 1 -> PlotType.HOUSE; else -> PlotType.PARK }
        4 -> when (col) { 0 -> PlotType.BUILDING; 1, 2 -> PlotType.FOR_SALE; else -> PlotType.FOR_SALE }
        5 -> when (col) { 0 -> PlotType.FOR_SALE; 1 -> PlotType.FACTORY; else -> PlotType.PARK }
        6 -> when (col) { 0 -> PlotType.HOUSE; 1 -> PlotType.FOR_SALE; else -> PlotType.BUILDING }
        7 -> when (col) { 0, 2 -> PlotType.FOR_SALE; 1 -> PlotType.PARK; else -> PlotType.FOR_SALE }
        8 -> when (col) { 0 -> PlotType.BUILDING; 1 -> PlotType.FOR_SALE; 2 -> PlotType.HOUSE; else -> PlotType.HOUSE }
        9 -> when (col) { 0, 2 -> PlotType.FOR_SALE; 1 -> PlotType.FACTORY; else -> PlotType.FACTORY }
        10 -> when (col) { 0 -> PlotType.PARK; 1 -> PlotType.FOR_SALE; else -> PlotType.BUILDING }
        else -> when (col) { 0 -> PlotType.FOR_SALE; 1 -> PlotType.HOUSE; 2 -> PlotType.FOR_SALE; else -> PlotType.FOR_SALE }
    }
}

@Composable
private fun PlotTile(type: PlotType, row: Int, col: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = when (type) {
            PlotType.HOUSE -> Color.White
            PlotType.FOR_SALE -> Color(0xFFA3E6A3)
            PlotType.BUILDING -> if ((row + col) % 2 == 0) Color.White else Color(0xFFE0E0E0)
            PlotType.PARK -> Color.White
            PlotType.FACTORY -> Color.White
            PlotType.COMPLEX -> Color.White
            else -> Color.White
        },
        border = BorderStroke(2.dp, Color(0xFF555555)),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (type) {
                PlotType.HOUSE -> {
                    if (col == 0 && row % 12 == 0) {
                        Text("ДОМ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Text("🏠", fontSize = 24.sp)
                }
                PlotType.FOR_SALE -> Text("продается", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                PlotType.BUILDING -> Text("🏢", fontSize = 24.sp)
                PlotType.PARK -> Text("🌳", fontSize = 24.sp)
                PlotType.FACTORY -> Text("🏭", fontSize = 24.sp)
                PlotType.COMPLEX -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🏠", fontSize = 14.sp)
                            Text("🏠", fontSize = 14.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🏠", fontSize = 14.sp)
                            Text("🏠", fontSize = 14.sp)
                        }
                        Text("🏪", fontSize = 18.sp, modifier = Modifier.background(Color.Black, RoundedCornerShape(3.dp)).padding(2.dp))
                    }
                }
                else -> Text("?", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun MapBotActor(bot: BotEntity, index: Int) {
    val spawnX = 320f + (index % 4) * 100f
    val spawnY = 470f
    var x by remember(bot.id.toString()) { mutableStateOf(spawnX) }
    var y by remember(bot.id.toString()) { mutableStateOf(spawnY) }
    var label by remember(bot.id.toString()) { mutableStateOf("🏠 дома") }
    val emoji = botEmoji(bot)

    LaunchedEffect(bot.id) {
        while (true) {
            label = "🛠️ на работу"
            delay(400)
            label = "🛠️ работает"
            delay(1500)
            label = "🛍️ покупает"
            delay(1200)
            label = "🏠 домой"
            delay(1500)
            label = "🏠 дома"
            delay(1500)
        }
    }

    Box(
        Modifier.offset { IntOffset(x.roundToInt() - 16, y.roundToInt() - 16) }
    ) {
        Text(emoji, fontSize = 24.sp)
        Text(
            label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.offset(y = (-10).dp).background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp)).padding(horizontal = 3.dp)
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
private fun VirtualJoystick(
    centerX: Float, centerY: Float,
    onActiveChanged: (Boolean) -> Unit,
    onKnobChanged: (Offset) -> Unit
) {
    val radius = 80f
    val knobRadius = 35f
    Box(
        Modifier
            .size((radius * 2).dp, (radius * 2).dp)
            .offset { IntOffset((centerX - radius).toInt(), (centerY - radius).toInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { center ->
                        onActiveChanged(true)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        var newX = knobRadius + amount.x
                        var newY = knobRadius + amount.y
                        val dx = newX - knobRadius
                        val dy = newY - knobRadius
                        val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        if (dist > knobRadius) {
                            val angle = atan2(dy.toDouble(), dx.toDouble())
                            newX = knobRadius + cos(angle).toFloat() * knobRadius
                            newY = knobRadius + sin(angle).toDouble().toFloat() * knobRadius
                        }
                        onKnobChanged(Offset(newX, newY))
                    },
                    onDragEnd = { onActiveChanged(false) },
                    onDragCancel = { onActiveChanged(false) }
                )
            }
    ) {
        Box(
            Modifier
                .size((radius * 2).dp)
                .background(Color.White.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(radius.dp))
        )
        Box(
            Modifier
                .size((knobRadius * 2).dp)
                .background(Color.White.copy(alpha = 0.6f))
                .clip(RoundedCornerShape(knobRadius.dp))
                .offset { IntOffset(knobRadius.toInt(), knobRadius.toInt()) }
        )
    }
}
