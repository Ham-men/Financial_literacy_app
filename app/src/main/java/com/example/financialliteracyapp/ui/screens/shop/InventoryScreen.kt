package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.financialliteracyapp.data.local.entity.TransactionEntity
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.components.StatBar
import com.example.financialliteracyapp.ui.theme.*
import kotlin.math.abs

@Composable
fun InventoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: InventoryViewModel = viewModel(factory = InventoryViewModel.factory(repo))
    val shop by vm.shop.collectAsState()
    val transactions by vm.transactions.collectAsState()

    // Избегаем проблем с smart cast — копируем в локальную переменную
    val shopEntity = shop

    // Вычисляем закупки/продажи/остаток/себестоимость из транзакций
    val stockPurchases = transactions
        .filter { it.kind == "EXPENSE" && it.category == "stock_purchase" }
        .sumOf { it.amount }
    val unitsPurchased = if (shopEntity?.costPrice != null && shopEntity.costPrice > 0) stockPurchases / shopEntity.costPrice else 0
    val cogs = transactions
        .filter { it.kind == "EXPENSE" && it.category == "cogs" }
        .sumOf { it.amount }
    val unitsSold = if (shopEntity?.costPrice != null && shopEntity.costPrice > 0) cogs / shopEntity.costPrice else 0
    val currentStock = shopEntity?.stock ?: 0
    val avgCostPrice = if (unitsPurchased > 0) stockPurchases / unitsPurchased else (shopEntity?.costPrice ?: 3)

    // Мини-игра: инвентаризация — пользователь вводит физический остаток
    var countedStock by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    var discrepancy by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("📦 Товарный журнал (Склад)",
            style = MaterialTheme.typography.headlineMedium)
        Text("Учёт закупок, продаж и остатков",
            fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        // Таблица склада
        AppCard {
            Text("📊 Сводка по Лимонаду", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            InventoryRow("Закуплено (шт.)", unitsPurchased.toString())
            InventoryRow("Продано (шт.)", unitsSold.toString())
            InventoryRow("Остаток на полках (журнал)", currentStock.toString())
            InventoryRow("Себестоимость за ед.", "$avgCostPrice ₡")
            InventoryRow("Стоимость остатка", "${currentStock * avgCostPrice} ₡")
        }

        Spacer(Modifier.height(16.dp))

        // Мини-игра «Инвентаризация»
        AppCard {
            Text("🔍 Мини-игра: Инвентаризация", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("Посчитай лимонады на полках и введи число:",
                fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = if (countedStock >= 0) countedStock.toString() else "",
                onValueChange = { text ->
                    countedStock = text.toIntOrNull() ?: -1
                },
                label = { Text("Физический остаток (шт.)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            BigActionButton(
                text = if (showResult) "🔁 Пересчитать" else "✅ Сверить",
                color = Primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!showResult && countedStock >= 0) {
                    discrepancy = countedStock - currentStock
                    showResult = true
                } else if (showResult) {
                    showResult = false
                    countedStock = -1
                }
            }

            if (showResult) {
                Spacer(Modifier.height(12.dp))
                InventoryResultCard(discrepancy = discrepancy, journalStock = currentStock, physicalStock = countedStock)
            }
        }

        Spacer(Modifier.height(16.dp))

        // История операций склада
        AppCard {
            Text("📋 Операции склада", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            val stockTxs = transactions
                .filter { it.category in setOf("stock_purchase", "cogs", "bot_sales") }
                .take(10)
            if (stockTxs.isEmpty()) {
                Text("Нет операций по складу. Закупи товар или открой магазин.", color = TextSecondary, fontSize = 13.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(stockTxs) { tx ->
                        StockTxRow(tx)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth(), onClick = onBack)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InventoryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun StockTxRow(tx: TransactionEntity) {
    val entry = when (tx.category) {
        "stock_purchase" -> Triple("📥", Primary, "Закупка ${tx.amount} ₡")
        "cogs" -> Triple("📤", Danger, "Продажа (себест. ${tx.amount} ₡)")
        "bot_sales" -> Triple("🤖", Accent, "Боты купили ${tx.amount} ₡")
        else -> Triple("📦", TextPrimary, "${tx.category} ${tx.amount} ₡")
    }
    val (icon, color, desc) = entry
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(desc, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("День ${tx.day}", fontSize = 11.sp, color = TextSecondary)
        }
        Text(
            (if (tx.kind == "INCOME") "+" else "−") + "${tx.amount} ₡",
            fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp
        )
    }
}

@Composable
private fun InventoryResultCard(discrepancy: Int, journalStock: Int, physicalStock: Int) {
    val (text, color) = when {
        discrepancy == 0 -> "✅ Всё сходится! Журнал = полки ($journalStock шт.)" to Primary
        discrepancy > 0 -> "⚠️ На полках больше на $discrepancy шт. (журнал $journalStock, полки $physicalStock). Возможна лишняя закупка или ошибка учёта." to Accent
        else -> "❌ Нехватка ${abs(discrepancy)} шт. (журнал $journalStock, полки $physicalStock). Проверь воровку или потери." to Danger
    }
    AppCard {
        Text(text, fontSize = 13.sp, color = color)
    }
}