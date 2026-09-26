package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.theme.Accent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class HiredBot(val id: Int, val units: Int)

/**
 * Авто-касса нанятого сотрудника (v5) — только визуальная анимация: боты заходят дверь →
 * полка → работник → уходят. ДЕЙСТВИТЕЛЬНЫЕ продажи считаются глобальным PassiveSales
 * (работает и вне сцены), чтобы не задваивать доход.
 */
@Composable
fun AutoCashierAnimation(
    buildingId: Long,
    stock: Int,
    price: Int,
    goodsEmoji: String,
    workerEmoji: String,
    modifier: Modifier = Modifier
) {
    val bots = remember { mutableStateListOf<HiredBot>() }

    // Актуальный запас без рестарта эффекта при каждой продаже (иначе ботинги никогда не заспавнятся:
    // продажа каждые 5 микров обнуляла счётчик, и now-last не успевало дойти до 5).
    val currentStock by rememberUpdatedState(stock)

    LaunchedEffect(buildingId) {
        var last = GameClock.minute.value
        while (true) {
            delay(400)
            val now = GameClock.minute.value
            if (!GameRules.isShopOpen(now)) { last = now; continue }
            if (now - last < GameRules.AUTO_CASHIER_BOT_EVERY_MINUTES) continue
            last = now
            if (currentStock < 1) continue
            bots.add(HiredBot(id = (bots.size + 1), units = 1))
        }
    }

    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val h = with(density) { maxHeight.toPx() }
        val w = with(density) { maxWidth.toPx() }
        val door = Offset(w * 0.5f, h * 0.04f)
        val shelf = Offset(w * 0.16f, h * 0.4f)
        val worker = Offset(w * 0.86f, h * 0.4f)

        Box(Modifier.fillMaxSize()) {
            bots.forEach { bot ->
                HiredBotWalker(
                    bot = bot,
                    door = door,
                    shelf = shelf,
                    worker = worker,
                    goodsEmoji = goodsEmoji,
                    workerEmoji = workerEmoji,
                    price = price,
                    onDone = { b -> if (bots.contains(b)) bots.remove(b) }
                )
            }
        }
    }
}

/** Один бот: дверь → полка (+товар) → работник (💸) → уходит. Только визуал. */
@Composable
private fun HiredBotWalker(
    bot: HiredBot,
    door: Offset,
    shelf: Offset,
    worker: Offset,
    goodsEmoji: String,
    workerEmoji: String,
    price: Int,
    onDone: (HiredBot) -> Unit
) {
    val x = remember(bot.id) { Animatable(door.x) }
    val y = remember(bot.id) { Animatable(door.y) }
    var grabbed by remember(bot.id) { mutableStateOf(false) }
    var paid by remember(bot.id) { mutableStateOf(false) }

    LaunchedEffect(bot.id) {
        coroutineScope {
            launch { x.animateTo(shelf.x, tween(900, easing = LinearEasing)) }
            launch { y.animateTo(shelf.y, tween(900, easing = LinearEasing)) }
        }
        grabbed = true
        delay(600)
        coroutineScope {
            launch { x.animateTo(worker.x, tween(900, easing = LinearEasing)) }
            launch { y.animateTo(worker.y, tween(900, easing = LinearEasing)) }
        }
        paid = true
        delay(1300)
        coroutineScope {
            launch { x.animateTo(door.x, tween(800, easing = LinearEasing)) }
            launch { y.animateTo(door.y, tween(800, easing = LinearEasing)) }
        }
        onDone(bot)
    }

    Box(modifier = Modifier.offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }) {
        Text("🤖", fontSize = 20.sp)
        if (grabbed && !paid) {
            Text(
                "+${bot.units} $goodsEmoji",
                fontSize = 9.sp,
                color = Accent,
                modifier = Modifier.padding(start = 18.dp)
            )
        }
        if (paid) {
            Text(
                "💸 ${bot.units * price} ₡",
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 18.dp, top = 2.dp)
            )
            Text(
                "→ $workerEmoji",
                fontSize = 9.sp,
                modifier = Modifier.padding(start = 18.dp, top = 14.dp)
            )
        }
    }
}