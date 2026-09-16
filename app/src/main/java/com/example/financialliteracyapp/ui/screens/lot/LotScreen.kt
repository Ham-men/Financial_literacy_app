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

/** Стройплощадка (День 20): план ларька / киоска, цена, аренда, подтверждение покупки/продажи. */
@Composable
fun LotScreen(
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

    val buildingEmoji = if (lotType == 0) "🍋" else "🥖"
    val buildingName = if (lotType == 0) "Ларёк с лимонадом" else "Киоск с хлебом"

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
                "🏗️ Стройплощадка",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cashAmount ₡")
        }

        // Сцена стройплощадки / готового здания
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color(0xFFD7CCC8))
        ) {
            // Небо
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFBDE0FE))
            )
            // Земля
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.BottomCenter)
                    .background(if (lotPurchased) Color(0xFF81C784) else Color(0xFFA1887F))
            )
            if (lotPurchased) {
                // Готовое здание с вывеской
                Surface(
                    Modifier.align(Alignment.Center).padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Column(
                        Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(buildingEmoji, fontSize = 44.sp)
                        Text(buildingName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("ваше здание · ул. Рынок 2", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Text("🚩", fontSize = 24.sp, modifier = Modifier.align(Alignment.TopStart).padding(top = 18.dp, start = 20.dp))
            } else {
                // Табличка «Продаётся»
                Surface(
                    Modifier.align(Alignment.Center).padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Column(
                        Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("「 Продаётся 」", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Accent)
                        Text("${Balance.LOT_PRICE} ₡", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("аренда +${Balance.LOT_RENT_PER_DAY} ₡/день", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                // Рабочий с тачкой
                Text("👷", fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 20.dp))
                Text("🧱", fontSize = 26.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 30.dp, bottom = 40.dp))
                Text("🛠️", fontSize = 22.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 64.dp, bottom = 26.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        if (lotPurchased) {
            AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("💡 Площадка освоена", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Здание приносит +${Balance.LOT_RENT_PER_DAY} ₡ каждый день.\n" +
                    "Продажа вернёт только ${Balance.LOT_SELL_PRICE} ₡ (было ${Balance.LOT_PRICE} ₡) — недвижимость теряет цену, как в жизни.\n" +
                    "Продать — значит потерять ${Balance.LOT_PRICE - Balance.LOT_SELL_PRICE} ₡.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))

            BigActionButton(
                "💼 Продать за ${Balance.LOT_SELL_PRICE} ₡",
                Danger,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
            Spacer(Modifier.height(24.dp))
        } else {
        // Два плана зданий
        Text(
            "Что построим?",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // План 1: ларёк
            Surface(
                onClick = { selectedPlan = 0 },
                shape = RoundedCornerShape(16.dp),
                color = if (selectedPlan == 0) Color(0xFFC8E6C9) else Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🍋", fontSize = 36.sp)
                    Text("Ларёк с лимонадом", fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Text("+${Balance.LOT_RENT_PER_DAY} ₡/день", fontSize = 12.sp, color = Primary)
                    Text("аренда участка в доход", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    if (selectedPlan == 0) Text("✓ выбран", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                }
            }
            // План 2: киоск с хлебом
            Surface(
                onClick = { selectedPlan = 1 },
                shape = RoundedCornerShape(16.dp),
                color = if (selectedPlan == 1) Color(0xFFFFE0B2) else Color(0xFFFFF3E0),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🥖", fontSize = 36.sp)
                    Text("Киоск с хлебом", fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Text("+${Balance.LOT_RENT_PER_DAY} ₡/день", fontSize = 12.sp, color = Primary)
                    Text("аренда участка в доход", fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    if (selectedPlan == 1) Text("✓ выбран", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("💡 Правила площадки", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Покупка участка стоит ${Balance.LOT_PRICE} ₡. Здание приносит +${Balance.LOT_RENT_PER_DAY} ₡ каждый день.\n" +
                "Продажа вернёт только ${Balance.LOT_SELL_PRICE} ₡ — недвижимость теряет цену, как в жизни.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(16.dp))

        if (cashAmount < Balance.LOT_PRICE) {
            Text(
                "Не хватает ${Balance.LOT_PRICE - cashAmount} ₡. Заработай в ларьке и вернись!",
                color = Danger,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(10.dp))
            BigActionButton(
                "💪 Заработать в ларьке",
                PrimaryDark,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = onBack
            )
        } else {
            BigActionButton(
                "🏗️ Купить участок за ${Balance.LOT_PRICE} ₡",
                Primary,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { showConfirm = true }
        }

        Spacer(Modifier.height(24.dp))

        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Купить участок?") },
                text = { Text("Спишем ${Balance.LOT_PRICE} ₡. Здание будет приносить +${Balance.LOT_RENT_PER_DAY} ₡ каждый день.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            repo.buyLot()
                            prefs.setLotPurchased(true)
                            prefs.setLotType(selectedPlan)
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