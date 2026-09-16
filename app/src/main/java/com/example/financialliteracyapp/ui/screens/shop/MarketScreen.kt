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
fun MarketScreen(
    onBack: () -> Unit,
    onOpenKiosk: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: MarketViewModel = viewModel(factory = MarketViewModel.factory(repo))
    val items by vm.catalog.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val pet by vm.pet.collectAsState()

    val cash = wallet?.cash ?: 500
    val petName = pet?.name ?: "Финни"
    val hunger = pet?.hunger ?: 70
    val mood = pet?.mood ?: 80
    val energy = pet?.energy ?: 90

    // Разделяем на нужное/желаемое
    val needItems = items.filter { it.category == "NEED" }
    val wantItems = items.filter { it.category == "WANT" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header с Финни и балансом
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(petName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Сытость $hunger | Настроение $mood | Бодрость $energy", fontSize = 12.sp, color = TextSecondary)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(12.dp)
            ) {
                Text("💰 $cash ₡", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Нужное
        if (needItems.isNotEmpty()) {
            Text("🍖 Нужное", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(needItems.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { item ->
                            MarketItemCard(
                                item = item,
                                cash = cash,
                                onBuy = { /* TODO: купить через repo */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Box(Modifier.weight(1f)) {}
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // Желаемое
        if (wantItems.isNotEmpty()) {
            Text("🎀 Желаемое", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(wantItems.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { item ->
                            MarketItemCard(
                                item = item,
                                cash = cash,
                                onBuy = { /* TODO: купить через repo */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Box(Modifier.weight(1f)) {}
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth()) { onBack() }
        BigActionButton("🏪 Ларёк (заработок)", Primary, Modifier.fillMaxWidth()) { onOpenKiosk() }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MarketItemCard(
    item: CatalogItemEntity,
    cash: Int,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = cash >= item.price
    val categoryColor = if (item.category == "NEED") Color(0xFF4CAF50) else Color(0xFF2196F3)
    val categoryLabel = if (item.category == "NEED") "🍖 Нужное" else "🎀 Желаемое"
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                // Category badge
                Text(
                    text = categoryLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(categoryColor, RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("${item.price} ₡", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary)
            }
            Spacer(Modifier.height(8.dp))
            Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Сытость +${item.hungerEffect} | Настроение +${item.moodEffect} | Бодрость +${item.energyEffect}",
                fontSize = 12.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            BigActionButton(
                text = if (canAfford) "Купить" else "Не хватает ${item.price - cash} ₡",
                color = if (canAfford) Primary else Color(0xFF9E9E9E),
                modifier = Modifier.fillMaxWidth(),
                enabled = canAfford
            ) { onBuy() }
        }
    }
}