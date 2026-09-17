package com.example.financialliteracyapp.ui.screens.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.theme.Primary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Размер тайла на карте (px). Здания задаются в тайлах — позиция BuildingEntity.x/y. */
private const val TILE = 40f
private const val BUILDING_W = 100f
private const val BUILDING_H = 80f

/** Экран карты района — top-down: дороги, здания из БД, боты-NPC, Финни. */
@Composable
fun MapScreen(
    onGoHome: () -> Unit,
    onOpenBuilding: (Long) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()

    val wallet by repo.observeWallet().collectAsState(initial = null)
    val dbBuildings by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
    val bots by repo.observeBots().collectAsState(initial = emptyList())
    val cash = wallet?.cash ?: 500

    // Здания из БД → пиксельные прямоугольники на карте
    val buildings = remember(dbBuildings) {
        dbBuildings.map { b ->
            MapBuilding(
                id = b.id,
                type = b.type,
                label = buildingLabel(b.type),
                x = b.x * TILE,
                y = b.y * TILE,
                width = BUILDING_W,
                height = BUILDING_H,
                isOpen = b.isOpen
            )
        }
    }
    // Точки входа в здания (куда боты идут) — стабильны между рекомпозициями
    val doors = remember(dbBuildings) {
        dbBuildings.associate { it.id to Offset(it.x * TILE + BUILDING_W / 2f, it.y * TILE + BUILDING_H - 8f) }
    }
    val productsId = buildings.firstOrNull { it.type == "PRODUCTS" }?.id
    val autoServiceId = buildings.firstOrNull { it.type == "AUTO_SERVICE" }?.id

    fun doorFor(bot: BotEntity): Offset? {
        // Сотрудник идёт в своё здание; водитель без работы — в СТО; остальные — в Продукты
        val work = bot.workBuildingId
        if (work != -1L) return doors[work]
        return when {
            bot.hasCar && autoServiceId != null -> doors[autoServiceId]
            productsId != null -> doors[productsId]
            else -> null
        }
    }

    // Позиция Финни (в пикселях на карте)
    var finniX by remember { mutableStateOf(200f) }
    var finniY by remember { mutableStateOf(300f) }

    // Состояние джойстика
    var joystickActive by remember { mutableStateOf(false) }
    var joystickCenterX by remember { mutableStateOf(0f) }
    var joystickCenterY by remember { mutableStateOf(0f) }
    var joystickKnobX by remember { mutableStateOf(0f) }
    var joystickKnobY by remember { mutableStateOf(0f) }

    fun openBuildingAt(x: Float, y: Float) {
        val b = buildings.firstOrNull { x in it.x..(it.x + it.width) && y in it.y..(it.y + it.height) }
        if (b != null) onOpenBuilding(b.id)
    }

    fun persistBotState(botId: Long, state: String, x: Float, y: Float) {
        scope.launch { repo.updateBotStatePosition(botId, state, x, y) }
    }

    // Джойстик: вычисление вектора движения
    val moveSpeed = 3f
    LaunchedEffect(joystickActive, joystickKnobX, joystickKnobY) {
        if (joystickActive) {
            val dx = (joystickKnobX - joystickCenterX) / 60f // нормализованный -1..1
            val dy = (joystickKnobY - joystickCenterY) / 60f
            if (abs(dx) > 0.1f || abs(dy) > 0.1f) {
                finniX += dx * moveSpeed
                finniY += dy * moveSpeed
                // Границы карты
                finniX = finniX.coerceIn(20f, 780f)
                finniY = finniY.coerceIn(20f, 580f)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF8FBC8F))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    finniX = offset.x
                    finniY = offset.y
                    openBuildingAt(offset.x, offset.y)
                })
            }
    ) {
        // Дороги (простые линии)
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .padding(top = 200.dp)
                .background(Color(0xFF696969))
        )
        Box(
            Modifier
                .width(40.dp)
                .fillMaxHeight()
                .align(Alignment.Center)
                .background(Color(0xFF696969))
        )

        // Здания из БД
        buildings.forEach { b ->
            BuildingMarker(b)
        }

        // Боты-NPC из БД: ходят дом ↔ своё здание, работают, покупают
        bots.forEachIndexed { index, bot ->
            MapBotActor(
                bot = bot,
                index = index,
                door = remember(bot.id, doors) { doorFor(bot) },
                onStateChanged = ::persistBotState
            )
        }

        // Финни (енот)
        Box(
            Modifier
                .size(40.dp)
                .offset {
                    IntOffset((finniX - 20).toInt(), (finniY - 20).toInt())
                }
        ) {
            Text("🦝", fontSize = 32.sp)
            // Имя над головой
            Text(
                "Финни", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-18).dp)
            )
        }

        // HUD сверху
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "🗺️ Район Рынок · боты: ${bots.size}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Chip("💰 $cash ₡")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Тап по экрану — перемещение. Тап по зданию — вход. Левый край — джойстик.",
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Виртуальный джойстик (левый нижний угол)
        VirtualJoystick(
            centerX = 100f, centerY = 500f,
            onActiveChanged = { joystickActive = it },
            onCenterChanged = { joystickCenterX = it.x; joystickCenterY = it.y },
            onKnobChanged = { joystickKnobX = it.x; joystickKnobY = it.y }
        )

        // Кнопка «Дом» справа снизу
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Surface(
                onClick = onGoHome,
                shape = RoundedCornerShape(28.dp),
                color = Primary
            ) {
                Text(
                    "🏠 Дом",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

/** Бот-NPC из БД: жизненный цикл дома ↔ работа ↔ магазин, состояние над головой. */
@Composable
private fun MapBotActor(
    bot: BotEntity,
    index: Int,
    door: Offset?,
    onStateChanged: (Long, String, Float, Float) -> Unit
) {
    // Дом-точка: нижняя «улица», точки разнесены по индексу (подальше от джойстика)
    val spawnX = 320f + (index % 4) * 100f
    val spawnY = 470f

    val x = remember(bot.id) { Animatable(spawnX) }
    val y = remember(bot.id) { Animatable(spawnY) }
    var label by remember(bot.id) { mutableStateOf("🏠 дома") }
    val emoji = botEmoji(bot)

    LaunchedEffect(bot.id, door) {
        val d = door
        if (d == null) {
            label = "🏠 дома"
            onStateChanged(bot.id, "HOME", spawnX, spawnY)
            return@LaunchedEffect
        }
        while (true) {
            // 1. Идёт на работу (к двери магазина)
            label = "🛠️ на работу"
            onStateChanged(bot.id, "MOVING_TO_WORK", d.x, d.y)
            coroutineScope {
                launch { x.animateTo(d.x, tween(1800, easing = LinearEasing)) }
                launch { y.animateTo(d.y, tween(1800, easing = LinearEasing)) }
            }
            delay(400)
            // 2. Работает (у входа в здание)
            label = "🛠️ работает"
            onStateChanged(bot.id, "WORKING", d.x, d.y)
            delay(1500)
            // 3. Покупает (та же точка для P0)
            label = "🛍️ покупает"
            onStateChanged(bot.id, "SHOPPING", d.x, d.y)
            delay(1200)
            // 4. Возвращается домой
            label = "🏠 домой"
            onStateChanged(bot.id, "MOVING_HOME", spawnX, spawnY)
            coroutineScope {
                launch { x.animateTo(spawnX, tween(1800, easing = LinearEasing)) }
                launch { y.animateTo(spawnY, tween(1800, easing = LinearEasing)) }
            }
            delay(400)
            // 5. Дома
            label = "🏠 дома"
            onStateChanged(bot.id, "HOME", spawnX, spawnY)
            delay(1500)
        }
    }

    Box(
        Modifier.offset {
            IntOffset(x.value.roundToInt() - 16, y.value.roundToInt() - 16)
        }
    ) {
        Text(emoji, fontSize = 24.sp)
        Text(
            label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White,
            modifier = Modifier.offset(y = (-10).dp).background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 3.dp, vertical = 1.dp)
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

private fun buildingLabel(type: String): String = when (type) {
    "PRODUCTS" -> "🛒 Продукты"
    "AUTO_SERVICE" -> "🔧 СТО"
    "CONSTRUCTION" -> "🏗️ Стройтехника"
    "HEALTH" -> "🏥 Здоровье"
    "ART" -> "🎨 Искусство"
    "RESIDENTIAL" -> "🏠 Жилой дом"
    else -> "🏢 Здание"
}

@Composable
private fun BuildingMarker(b: MapBuilding) {
    val isOpen = b.isOpen
    Box(
        Modifier
            .size(b.width.dp, b.height.dp)
            .offset {
                IntOffset(b.x.toInt(), b.y.toInt())
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (isOpen) Color(0xFF8B4513) else Color(0xFF6B4423))
                .clip(RoundedCornerShape(8.dp))
        )
        Text(b.emoji, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
        Text(
            b.label, fontSize = 10.sp, color = Color.White, maxLines = 1, textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).fillMaxWidth().padding(horizontal = 4.dp)
        )
        if (isOpen) {
            Text("🟢", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
        }
    }
}

@Composable
private fun VirtualJoystick(
    centerX: Float, centerY: Float,
    onActiveChanged: (Boolean) -> Unit,
    onCenterChanged: (Offset) -> Unit,
    onKnobChanged: (Offset) -> Unit
) {
    val radius = 80f
    val knobRadius = 35f
    Box(
        Modifier
            .size((radius * 2).dp, (radius * 2).dp)
            .offset {
                IntOffset((centerX - radius).toInt(), (centerY - radius).toInt())
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { center ->
                        onCenterChanged(center)
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
        // Основание джойстика
        Box(
            Modifier
                .size((radius * 2).dp)
                .background(Color.White.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(radius.dp))
        )
        // Ручка
        Box(
            Modifier
                .size((knobRadius * 2).dp)
                .background(Color.White.copy(alpha = 0.6f))
                .clip(RoundedCornerShape(knobRadius.dp))
                .offset {
                    IntOffset(knobRadius.toInt(), knobRadius.toInt())
                }
        )
    }
}

data class MapBuilding(
    val id: Long,
    val type: String,
    val label: String,
    val x: Float, val y: Float,
    val width: Float, val height: Float,
    val isOpen: Boolean
) {
    val emoji: String = when (type) {
        "PRODUCTS" -> "🛒"
        "AUTO_SERVICE" -> "🔧"
        "CONSTRUCTION" -> "🏗️"
        "HEALTH" -> "🏥"
        "ART" -> "🎨"
        "RESIDENTIAL" -> "🏠"
        else -> "🏢"
    }
}