package com.example.financialliteracyapp.ui.screens.banks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class BankType(val title: String, val emoji: String, val color: Color, val hint: String) {
    NEED("Нужное", "🍖", BankSpend, "Корм, вода, уход"),
    WANT("Желаемое", "🎀", BankSave, "Игрушки, особый корм"),
    SAVE("Копилка", "🐷", BankGrow, "На цель: мячик / палатка")
}

private data class Coin(val id: Long, val value: Int, var offset: Offset, var placed: BankType?)

/**
 * Сцена «План» — распределение денег по 3 банкам.
 * Соответствует банк.html: 3 варианта (желаемое / нужное / копилка).
 * Сохраняет весь функционал: перетаскивание монет, подтверждение плана.
 */
@Composable
fun BankScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: BankViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = BankViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()

    val planSet = (wallet?.needPlan ?: 0) + (wallet?.wantPlan ?: 0) + (wallet?.savePlan ?: 0) > 0

    if (wallet != null && planSet) {
        LockedPlanView(wallet = wallet!!, onBack = onBack)
        return
    }

    PlanBuilderView(
        total = (wallet?.cash ?: 500),
        onConfirm = { need, want, save ->
            vm.confirm(need, want, save)
            onBack()
        },
        onBack = onBack
    )
}

@Composable
private fun LockedPlanView(wallet: WalletEntity, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Шапка
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Дата 01.01.2020", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "нужное \\ желаемое \\ копилка\n${wallet.needPlan} \\ ${wallet.wantPlan} \\ ${wallet.savePlan}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(Modifier.height(24.dp))

        // Заголовок
        Text(
            "🎯 План на день",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ты разделил деньги по 3 банкам. План зафиксирован.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        // Банки
        BankRow(BankType.NEED, wallet.needPlan, wallet.needFact)
        BankRow(BankType.WANT, wallet.wantPlan, wallet.wantFact)
        BankRow(BankType.SAVE, wallet.savePlan, wallet.saveFact)

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text("← Назад", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun BankRow(bank: BankType, plan: Int, fact: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(bank.emoji, fontSize = 22.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(bank.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = bank.color)
            Text(bank.hint, fontSize = 11.sp, color = TextSecondary)
        }
        Text("$plan ₡", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (fact > 0) {
            Spacer(Modifier.width(8.dp))
            Text("потрачено $fact ₡", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun PlanBuilderView(
    total: Int,
    onConfirm: (Int, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    var coins by remember {
        val parts = maxOf(1, kotlin.math.ceil(total / 100f).toInt().coerceAtMost(5))
        val denom = if (parts == 0) 1 else parts
        val base = total / denom
        val remainder = total - base * denom
        mutableStateOf(
            List(parts) { i ->
                Coin(i.toLong(), base + if (i == parts - 1) remainder else 0, Offset.Zero, null)
            }
        )
    }
    var totalCoins by remember { mutableIntStateOf(total) }
    val bankBounds = remember { mutableStateMapOf<BankType, Rect>() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Шапка
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Дата 01.01.2020", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "нужное \\ желаемое \\ копилка\n0 \\ 0 \\ 0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "💰 $totalCoins ₡",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Accent,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Разложи монеты по 3 банкам",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))

        // 3 банки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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

        // Монеты
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

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val need = coins.filter { it.placed == BankType.NEED }.sumOf { it.value }
                val want = coins.filter { it.placed == BankType.WANT }.sumOf { it.value }
                val save = coins.filter { it.placed == BankType.SAVE }.sumOf { it.value }
                onConfirm(need, want, save)
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(22.dp),
            enabled = coins.isNotEmpty() && coins.all { it.placed != null }
        ) {
            Text("Подтвердить план", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(
            "Чтобы план применился, все монеты должны лежать в банках.",
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BankBox(bank: BankType, count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, bank.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        color = Color.White
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(bank.emoji, fontSize = 32.sp)
            Text(bank.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = bank.color)
            Text("$count ₡", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(bank.hint, fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
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
            .size(64.dp)
            .clip(CircleShape)
            .background(Accent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _ -> },
                    onDragEnd = { onDragEnd(positionInRoot + Offset(32f, 32f)) },
                    onDrag = { change, _ ->
                        change.consume()
                        offset += change.position - change.previousPosition
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("💰", fontSize = 28.sp)
    }
}
