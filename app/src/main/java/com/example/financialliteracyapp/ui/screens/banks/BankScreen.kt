package com.example.financialliteracyapp.ui.screens.banks

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*
import kotlin.math.roundToInt

private enum class BankType(val title: String, val emoji: String, val color: Color, val hint: String) {
    NEED("Нужное", "🟢", BankSpend, "Корм, вода,\nуход, подстилка"),
    WANT("Желаемое", "🔵", BankSave, "Мячик, бантик,\nкартина, торт"),
    SAVE("Копилка", "🟡", BankGrow, "На цель:\nмячик / палатка / набор")
}

private data class Coin(val id: Long, val value: Int, var offset: Offset, var placed: BankType?)

@Composable
fun BankScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: BankViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = BankViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()
    // показываем текущее распределение из Room, если есть
    val totalFromDb = (wallet?.cash ?: 500) + (wallet?.needPlan ?: 0) + (wallet?.wantPlan ?: 0) + (wallet?.savePlan ?: 0)

    var coins by remember {
        mutableStateOf(
            List(5) { i -> Coin(i.toLong(), 100, Offset.Zero, null) }
        )
    }
    var totalCoins by remember { mutableIntStateOf(500) }
    // синхронизируем показ total с DB на первый вход
    LaunchedEffect(totalFromDb) {
        if (wallet != null) totalCoins = totalFromDb - (coins.filter { it.placed != null }.sumOf { it.value })
    }

    val bankBounds = remember { mutableStateMapOf<BankType, Rect>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Распредели монеты",
            style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Перетащи монеты в одну из копилок",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        Text("💰 $totalCoins ₡",
            fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Accent,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        // Область с монетами (сверху) и тремя банками (снизу)
        Box(Modifier.weight(1f).fillMaxWidth()) {

            // Три банка (фон)
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BankType.values().forEach { bank ->
                    BankBox(
                        bank = bank,
                        count = coins.filter { it.placed == bank }.sumOf { it.value },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .onGloballyPositioned { coords ->
                                bankBounds[bank] = coords.boundsInRoot()
                            }
                    )
                }
            }

            // Монеты (сверху, draggable)
            coins.forEachIndexed { index, coin ->
                if (coin.placed == null) {
                    DraggableCoin(
                        coin = coin,
                        index = index,
                        onDragEnd = { dropPos ->
                            val target = bankBounds.entries.firstOrNull { it.value.contains(dropPos) }?.key
                            if (target != null) {
                                coins = coins.map {
                                    if (it.id == coin.id) it.copy(placed = target) else it
                                }
                                totalCoins -= coin.value
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        BigActionButton(
            text = "Подтвердить распределение",
            color = Primary,
            modifier = Modifier.fillMaxWidth(),
            enabled = coins.all { it.placed != null }
        ) {
            val need = coins.filter { it.placed == BankType.NEED }.sumOf { it.value }
            val want = coins.filter { it.placed == BankType.WANT }.sumOf { it.value }
            val save = coins.filter { it.placed == BankType.SAVE }.sumOf { it.value }
            vm.confirm(need, want, save)
            onBack()
        }
    }
}

@Composable
private fun BankBox(bank: BankType, count: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(bank.emoji, fontSize = 36.sp)
        Text(bank.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = bank.color)
        Text("$count ₡", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(bank.hint, fontSize = 11.sp, color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun DraggableCoin(
    coin: Coin,
    index: Int,
    onDragEnd: (Offset) -> Unit
) {
    var offset by remember { mutableStateOf(Offset(40f + index * 90f, 40f)) }
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .onGloballyPositioned { coords ->
                positionInRoot = coords.positionInRoot()
            }
            .size(72.dp)
            .clip(RoundedCornerShape(50))
            .background(Accent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { onDragEnd(positionInRoot + Offset(36f, 36f)) },
                    onDrag = { change, drag ->
                        change.consume()
                        offset += drag
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("💰", fontSize = 32.sp)
    }
}