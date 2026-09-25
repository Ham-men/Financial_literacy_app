package com.example.financialliteracyapp.ui.screens.banks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.GameRules
import kotlin.math.roundToInt

private data class BankOption(
    val id: String,
    val emoji: String,
    val text: String,
    val color: Color
)

private val banks = listOf(
    BankOption("NEED", "👖", "в карман", Color(0xFF4CAF50)),
    BankOption("WANT", "🚛", "в желаемое", Color(0xFF2196F3)),
    BankOption("SAVE", "🐷", "в копилку", Color(0xFFFFC107))
)

/** Сколько отдельных монет показываем в куче (остаток — «+N ₡ в мешке»). */
private const val MAX_SHOWN_COINS = 10

@Composable
fun BankScreen() {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: BankViewModel = viewModel(factory = BankViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()
    val cash = wallet?.cash ?: 0
    val (fullCoins, remainder) = GameRules.bagCoins(cash)

    // Монеты для перетаскивания: по 50 ₡ + остаток (до MAX_SHOWN_COINS штук)
    val displayCoins = buildList {
        repeat(minOf(fullCoins, MAX_SHOWN_COINS)) { add(50) }
        if (remainder > 0) add(remainder)
    }
    val hiddenCoins = (fullCoins - minOf(fullCoins, MAX_SHOWN_COINS)).coerceAtLeast(0)

    // Области банок (в глобальных координатах сцены) для сброса монеты
    var bankBounds by remember { mutableStateOf<Map<String, Rect>>(emptyMap()) }
    // Какая банка сейчас «под курсором» (подсветка)
    var hoverBank by remember { mutableStateOf<String?>(null) }
    // Координаты контейнера над всеми слоями (для пересчёта центра монеты)
    var overlayCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3EA))
    ) {
        // --- Нижний слой: шапка, мешок, банки, кнопки ---
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "💼 Мешок",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$cash ₡",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF795548)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "Зажми монету и перетащи в нужную банку ▶",
                fontSize = 12.sp, color = Color.Gray
            )
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = { vm.withdrawAllToBag() },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.width(180.dp).height(38.dp)
            ) {
                Text("🎒 Вывести всё в мешок", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxSize()) {
                // Слева — подсказка и куча монет (сами монеты — в верхнем слое)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(30.dp))
                    if (displayCoins.isEmpty()) {
                        Text("Мешок пуст", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Заработай в лареке, СТО или стройке,\nденьги придут сюда.",
                            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            "🪙 ${displayCoins.size} монет",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF795548)
                        )
                        if (hiddenCoins > 0) {
                            Spacer(Modifier.height(2.dp))
                            Text("+ ещё $hiddenCoins монет в мешке", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Справа — банки-приёмники + кнопки (компактно, всё на экране)
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    banks.forEachIndexed { index, bank ->
                        val balance = when (bank.id) {
                            "NEED" -> wallet?.needPlan ?: 0
                            "WANT" -> wallet?.wantPlan ?: 0
                            else -> wallet?.savePlan ?: 0
                        }
                        BankDropZone(
                            bank = bank,
                            balance = balance,
                            hot = hoverBank == bank.id,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                bankBounds = bankBounds + (bank.id to coords.boundsInRoot())
                            }
                        )
                        if (index < banks.lastIndex) Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Копилка растёт на 5% каждый день.",
                        fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- Верхний слой: перетаскиваемые монеты ---
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { overlayCoords = it }
        ) {
            val density = LocalDensity.current
            val startX = with(density) { 34.dp.toPx() }
            val startY = with(density) { 100.dp.toPx() }
            val stepX = with(density) { 20.dp.toPx() }
            val stepY = with(density) { 16.dp.toPx() }

            displayCoins.forEachIndexed { index, value ->
                val origin = overlayCoords?.boundsInRoot()?.topLeft ?: Offset.Zero
                DraggableCoin(
                    value = value,
                    start = Offset(startX + (index % 4) * stepX, startY + (index / 4) * stepY),
                    bankBounds = bankBounds,
                    pointerOrigin = origin,
                    onHover = { hoverBank = it },
                    onDrop = { bank -> vm.moveToBank(bank, value) }
                )
            }
        }
    }
}

@Composable
private fun DraggableCoin(
    value: Int,
    start: Offset,
    bankBounds: Map<String, Rect>,
    pointerOrigin: Offset,
    onHover: (String?) -> Unit,
    onDrop: (String) -> Unit
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    fun coinCenter(): Offset {
        val half = with(density) { 26.dp.toPx() }
        return pointerOrigin + start + dragOffset + Offset(half, half)
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (start.x + dragOffset.x).roundToInt(),
                    (start.y + dragOffset.y).roundToInt()
                )
            }
            .size(52.dp)
            .zIndex(if (dragging) 5f else 3f)
            .pointerInput(value, start, bankBounds) {
                detectDragGestures(
                    onDragStart = {
                        dragging = true
                        onHover(null)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragOffset += amount
                        val center = coinCenter()
                        onHover(
                            bankBounds.entries
                                .firstOrNull { it.value.contains(center) }
                                ?.key
                        )
                    },
                    onDragEnd = {
                        dragging = false
                        onHover(null)
                        val center = coinCenter()
                        val target = bankBounds.entries
                            .firstOrNull { it.value.contains(center) }
                            ?.key
                        if (target != null) {
                            onDrop(target)
                        } else {
                            dragOffset = Offset.Zero
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        onHover(null)
                        dragOffset = Offset.Zero
                    }
                )
            }
    ) {
        CoinFace(value, dragging)
    }
}

@Composable
private fun CoinFace(value: Int, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFFFFC107) else Color(0xFFF5E9CD),
        border = BorderStroke(2.dp, if (selected) Color(0xFFFF9800) else Color(0xFFD4B97A)),
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪙", fontSize = 20.sp)
                Text("$value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D4C00))
            }
        }
    }
}

@Composable
private fun BankDropZone(
    bank: BankOption,
    balance: Int,
    hot: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (hot) bank.color.copy(alpha = 0.45f) else bank.color.copy(alpha = 0.14f),
        border = BorderStroke(if (hot) 3.dp else 2.dp, bank.color),
        modifier = modifier.width(160.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bank.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    bank.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(
                "$balance ₡",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = bank.color
            )
        }
    }
}