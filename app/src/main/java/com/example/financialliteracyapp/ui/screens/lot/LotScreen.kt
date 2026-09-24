package com.example.financialliteracyapp.ui.screens.lot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.launch

/** Варианты магазинов на покупной площадке: Ларёк / Стройка / СТО. */
private data class ShopPlan(val id: Int, val emoji: String, val name: String, val desc: String, val bg: Color)

/** Визуал купленного магазина, согласован с картой (MapScreen.lotEmoji/lotName). */
private fun lotEmoji(type: Int) = when (type) {
    0 -> "🛒"
    1 -> "🔧"
    else -> "🚗"
}

private fun lotName(type: Int) = when (type) {
    0 -> "Ларёк"
    1 -> "Стройка"
    else -> "СТО"
}

/** Покупка магазина на свободном районе карты. Компактный экран под телефон. */
@Composable
fun LotScreen(
    plotId: String,
    onBack: () -> Unit,
    onBuildingBuilt: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val scope = rememberCoroutineScope()

    val cash by repo.observeWallet().collectAsState(initial = null)
    val cashAmount = cash?.cash ?: 500
    val lotPurchased by prefs.lotPurchased.collectAsState(initial = false)
    val lotType by prefs.lotType.collectAsState(initial = 0)

    var selectedPlan by remember { mutableStateOf(0) }
    var showConfirm by remember { mutableStateOf(false) }
    var showSellConfirm by remember { mutableStateOf(false) }

    val buildingEmoji = lotEmoji(lotType)
    val buildingName = lotName(lotType)

    val plans = listOf(
        ShopPlan(0, "🛒", "Ларёк", "продукты", Color(0xFFC8E6C9)),
        ShopPlan(1, "🔧", "Стройка", "материалы", Color(0xFFFFF3E0)),
        ShopPlan(2, "🚗", "СТО", "ремонт", Color(0xFFB3E5FC))
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Компактный заголовок
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
                "🛍️ Купить магазин",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cashAmount ₡")
        }

        // Компактная сцена площадки / готового здания
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Color(0xFFD7CCC8))
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFBDE0FE))
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .align(Alignment.BottomCenter)
                    .background(if (lotPurchased) Color(0xFF81C784) else Color(0xFFA1887F))
            )
            if (lotPurchased) {
                // Готовый магазин с вывеской
                Surface(
                    Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(buildingEmoji, fontSize = 30.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(buildingName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                            Text("куплено", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Табличка «Продаётся»
                Surface(
                    Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Column(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("「 Продаётся 」", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Accent)
                        Text("${Balance.LOT_PRICE} ₡", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("+${Balance.LOT_RENT_PER_DAY} ₡/день", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Text("👷", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 6.dp))
                Text("🧱", fontSize = 20.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 8.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        if (lotPurchased) {
            AppCard(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("💡 Площадка освоена", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Здание приносит +${Balance.LOT_RENT_PER_DAY} ₡ каждый день. " +
                    "Продажа вернёт только ${Balance.LOT_SELL_PRICE} ₡ (было ${Balance.LOT_PRICE} ₡).",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            BigActionButton(
                "💼 Продать за ${Balance.LOT_SELL_PRICE} ₡",
                Danger,
                Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) { showSellConfirm = true }

            if (showSellConfirm) {
                AlertDialog(
                    onDismissRequest = { showSellConfirm = false },
                    title = { Text("Продать здание?") },
                    text = { Text("Вернётся только ${Balance.LOT_SELL_PRICE} ₡. Ты потеряешь ${Balance.LOT_PRICE - Balance.LOT_SELL_PRICE} ₡. Продаём?") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                repo.sellLot()
                                prefs.setLotPurchased(false)
                                prefs.setLotPlotId("")
                                showSellConfirm = false
                                onBuildingBuilt()
                            }
                        }) { Text("Продать") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSellConfirm = false }) { Text("Оставить") }
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
        } else {
            // Выбор типа магазина: 3 карточки в ряд
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                plans.forEach { plan ->
                    Surface(
                        onClick = { selectedPlan = plan.id },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedPlan == plan.id) plan.bg else Color(0xFFF2F2F2),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(plan.emoji, fontSize = 26.sp)
                            Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(plan.desc, fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
                            if (selectedPlan == plan.id) Text("✓", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AppCard(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    "Участок стоит ${Balance.LOT_PRICE} ₡, здание приносит +${Balance.LOT_RENT_PER_DAY} ₡/день. " +
                    "Продажа вернёт только ${Balance.LOT_SELL_PRICE} ₡.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            if (cashAmount < Balance.LOT_PRICE) {
                Text(
                    "Не хватает ${Balance.LOT_PRICE - cashAmount} ₡.",
                    color = Danger,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(6.dp))
                BigActionButton(
                    "💪 Заработать в ларьке",
                    PrimaryDark,
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    onClick = onBack
                )
            } else {
                BigActionButton(
                    "🛍️ Купить за ${Balance.LOT_PRICE} ₡",
                    Primary,
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) { showConfirm = true }
            }

            Spacer(Modifier.height(12.dp))

            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("Купить магазин?") },
                    text = { Text("Спишем ${Balance.LOT_PRICE} ₡. Будет приносить +${Balance.LOT_RENT_PER_DAY} ₡ каждый день.") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                repo.buyLot()
                                prefs.setLotPurchased(true)
                                prefs.setLotType(selectedPlan)
                                prefs.setLotPlotId(plotId)
                                showConfirm = false
                                onBuildingBuilt()
                            }
                        }) { Text("Купить") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm = false }) { Text("Пока нет") }
                    }
                )
            }
        }
    }
}