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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*
import kotlin.math.roundToInt

private enum class BankType(val title: String, val emoji: String, val color: Color, val hint: String) {
    NEED("Нужное", "🍖", BankSpend, "Корм, вода, уход, подстилка"),
    WANT("Желаемое", "🎀", BankSave, "Мячик, бантик, картина, торт"),
    SAVE("Копилка", "🐷", BankGrow, "На цель: мячик / палатка / набор")
}

private data class Coin(val id: Long, val value: Int, var offset: Offset, var placed: BankType?)

/**
 * Сцена «План»: делит доступные деньги на 3 банки (нужное / желаемое / копилка).
 * Эти деньги потом тратятся в других сценах именно из своей банки.
 * Один раз подтвердил план — его нельзя снова перетаскивать в тот же день.
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

/** План уже зафиксирован на этот день: показываем цифры, монеты не трогаем. */
@Composable
private fun LockedPlanView(wallet: WalletEntity, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("🎯 План на день", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Ты уже разделил деньги по 3 банкам. План зафиксирован — в этот день его не меняют.",
            fontSize = 13.sp, color = TextSecondary
        )
        Spacer(Modifier.height(16.dp))

        AppCard {
            Text("Доступно не по плану: ${wallet.cash} ₡", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            LockedBankRow(BankType.NEED, wallet.needPlan, wallet.needFact)
            LockedBankRow(BankType.WANT, wallet.wantPlan, wallet.wantFact)
            LockedBankRow(BankType.SAVE, wallet.savePlan, wallet.saveFact)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "В магазине «Нужное» списывается из зелёной банки, «Желаемое» — из синей. Копилка пополняет цели.",
            fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth(), onClick = onBack)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LockedBankRow(bank: BankType, plan: Int, fact: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(bank.emoji, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
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

/** Первый раз в этот день: перетаскиваем монеты по банкам и подтверждаем. */
@Composable
private fun PlanBuilderView(
    total: Int,
    onConfirm: (Int, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    // Разбиваем total на "монеты" (максимум 5 штук по ~равным частям)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text("Подели деньги на день", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Это сцена «План». Разложи монеты по 3 банкам — потом покупки списываются из своей банки.",
            fontSize = 13.sp, color = TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        Text("💰 $totalCoins ₡",
            fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Accent,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
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
            text = "Подтвердить план",
            color = Primary,
            modifier = Modifier.fillMaxWidth(),
            enabled = coins.isNotEmpty() && coins.all { it.placed != null }
        ) {
            val need = coins.filter { it.placed == BankType.NEED }.sumOf { it.value }
            val want = coins.filter { it.placed == BankType.WANT }.sumOf { it.value }
            val save = coins.filter { it.placed == BankType.SAVE }.sumOf { it.value }
            onConfirm(need, want, save)
        }
        Spacer(Modifier.height(8.dp))
        Text("Чтобы план применился, все монеты должны лежать в банках.", fontSize = 12.sp, color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally))
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
private fun AppCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(16.dp), content = content)
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