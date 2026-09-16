package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.CatalogItemEntity
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*
import androidx.compose.ui.graphics.Color

@Composable
fun MarketScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: MarketViewModel = viewModel(factory = MarketViewModel.factory(repo))
    val items by vm.catalog.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val pet by vm.pet.collectAsState()

    val cash = wallet?.cash ?: 500
    val needPlan = wallet?.needPlan ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val planSet = needPlan + wantPlan + (wallet?.savePlan ?: 0) > 0
    val availableForItem = { item: CatalogItemEntity ->
        val base = when (item.category) {
            "NEED" -> needPlan
            "WANT" -> wantPlan
            else -> 0
        }
        // наличные — запас, который дописывается к банке
        base + cash
    }
    val showCash = cash + needPlan + wantPlan
    val petName = pet?.name ?: "Финни"
    val hunger = pet?.hunger ?: 70
    val mood = pet?.mood ?: 80
    val energy = pet?.energy ?: 90

    // Разделяем на нужное/желаемое
    val needItems = items.filter { it.category == "NEED" }
    val wantItems = items.filter { it.category == "WANT" }

    // Диалог подтверждения покупки
    var pendingItem by remember { mutableStateOf<CatalogItemEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header с заголовком и балансом
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🏪 Магазин", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                if (planSet) "💰 $showCash ₡ (в банках Нужное/Желаемое)"
                else "💰 $cash ₡",
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary
            )
            }
        }

        // Статы питомца и подсказка
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "🐷 $petName: 🍖$hunger 😊$mood ⚡$energy",
                fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f)
            )
            BigActionButton("◀", PrimaryDark, Modifier.width(48.dp).height(36.dp)) { onBack() }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Товары для Финни: нужное — для ухода, желаемое — для радости",
            fontSize = 11.sp, color = TextSecondary
        )

        if (showCash < 20) {
            Spacer(Modifier.height(4.dp))
            Text(
                "⚠️ Не хватает монет? Выполни задание 📋 или заработай в ларьке 🍋",
                fontSize = 11.sp, color = Color(0xFFFF9800)
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (needItems.isNotEmpty()) {
                item {
                    Text("🍖 Нужное", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                }
                items(needItems.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { item ->
                            MarketItemCard(
                                item   = item,
                                available = availableForItem(item),
                                onBuy  = { pendingItem = item },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }

            if (wantItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    Text("🎀 Желаемое", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                }
                items(wantItems.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { item ->
                            MarketItemCard(
                                item   = item,
                                available = availableForItem(item),
                                onBuy  = { pendingItem = item },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Диалог подтверждения
    pendingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingItem = null },
            title   = { Text("Купить «${item.title}»?") },
            text    = {
                Column {
                    Text("Цена: ${item.price} ₡")
                    if (item.hungerEffect > 0) Text("Сытость +${item.hungerEffect}")
                    if (item.moodEffect > 0)   Text("Настроение +${item.moodEffect}")
                    if (item.energyEffect > 0) Text("Бодрость +${item.energyEffect}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.buy(item.id)
                    pendingItem = null
                }) { Text("Купить") }
            },
            dismissButton = {
                TextButton(onClick = { pendingItem = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun MarketItemCard(
    item: CatalogItemEntity,
    available: Int,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = available >= item.price
    val categoryColor = if (item.category == "NEED") Color(0xFF4CAF50) else Color(0xFF2196F3)
    val categoryLabel = if (item.category == "NEED") "🍖 Нужное" else "🎀 Желаемое"
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = categoryLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(categoryColor, RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("${item.price} ₡", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
            }
            Spacer(Modifier.height(6.dp))
            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Сытость +${item.hungerEffect} · Настроение +${item.moodEffect} · Бодрость +${item.energyEffect}",
                fontSize = 10.sp, color = TextSecondary
            )
            Spacer(Modifier.height(10.dp))
            BigActionButton(
                text = if (canAfford) "Купить" else "Не хватает ${item.price - available} ₡",
                color = if (canAfford) Primary else Color(0xFF9E9E9E),
                modifier = Modifier.fillMaxWidth(),
                enabled = canAfford
            ) { onBuy() }
        }
    }
}