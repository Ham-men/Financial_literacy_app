package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun PnLScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: PnLViewModel = viewModel(factory = PnLViewModel.factory(repo))
    val transactions by vm.transactions.collectAsState()
    val shop by vm.shop.collectAsState()

    // Реальные данные из транзакций
    val revenue = transactions.filter { it.kind == "INCOME" }.sumOf { it.amount }
    val cogs = transactions.filter { it.kind == "EXPENSE" && it.category == "cogs" }.sumOf { it.amount }
    val rent = transactions.filter { it.kind == "EXPENSE" && it.category == "rent" }.sumOf { it.amount }
    val tax = transactions.filter { it.kind == "EXPENSE" && it.category == "tax" }.sumOf { it.amount }
    val otherExpenses = transactions
        .filter { it.kind == "EXPENSE" && it.category !in setOf("cogs", "rent", "tax") }
        .sumOf { it.amount }
    val grossProfit = revenue - cogs
    val netProfit = grossProfit - rent - tax - otherExpenses

    // Мини-игра: разложить по конвертам
    var gameRevenue by remember { mutableIntStateOf(0) }
    var gameCogs by remember { mutableIntStateOf(0) }
    var gameRent by remember { mutableIntStateOf(0) }
    var gameTax by remember { mutableIntStateOf(0) }
    var gameProfit by remember { mutableIntStateOf(0) }
    var gameStep by remember { mutableIntStateOf(0) } // 0-4: выручка, себестоимость, аренда, налог, прибыль
    var showHint by remember { mutableStateOf(false) }

    val totalToDistribute = revenue
    val allocated = gameCogs + gameRent + gameTax
    val remaining = totalToDistribute - allocated

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("📈 P&L: Прибыли и убытки",
            style = MaterialTheme.typography.headlineMedium)
        Text("Распредели выручку по конвертам",
            fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        // Реальный P&L
        AppCard {
            Text("📊 Реальный отчёт", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            PnLRow("Выручка", revenue, Primary)
            PnLRow("Себестоимость (COGS)", -cogs, Danger)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            PnLRow("Валовая прибыль", grossProfit, Primary, big = true)
            Spacer(Modifier.height(4.dp))
            PnLRow("Аренда", -rent, Danger)
            PnLRow("Налог (13%)", -tax, Danger)
            if (otherExpenses > 0) PnLRow("Прочее", -otherExpenses, Danger)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            PnLRow("Чистая прибыль", netProfit, if (netProfit >= 0) Primary else Danger, big = true)
        }

        Spacer(Modifier.height(16.dp))

        // Мини-игра
        AppCard {
            Text("🎮 Мини-игра: Разложи по конвертам", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("У тебя выручка $totalToDistribute ₡. Разложи деньги по конвертам:",
                fontSize = 13.sp, color = TextSecondary)

            Spacer(Modifier.height(12.dp))

            EnvelopeRow("📦 Себестоимость", "COGS", gameCogs, Danger) { gameCogs = it }
            EnvelopeRow("🏠 Аренда", "RENT", gameRent, Danger) { gameRent = it }
            EnvelopeRow("📝 Налог", "TAX", gameTax, Danger) { gameTax = it }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                Text("Остаток для прибыли:", Modifier.weight(1f), fontSize = 14.sp)
                Text("$remaining ₡", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = if (remaining >= 0) Primary else Danger)
            }

            Spacer(Modifier.height(12.dp))

            if (gameStep < 3) {
                BigActionButton(
                    text = "➡ Далее: ${listOf("Себестоимость", "Аренда", "Налог")[gameStep]}",
                    color = Primary,
                    modifier = Modifier.fillMaxWidth()
                ) { gameStep++ }
            } else {
                val calcProfit = totalToDistribute - gameCogs - gameRent - gameTax
                gameProfit = calcProfit
                Row(Modifier.fillMaxWidth()) {
                    Text("💰 Чистая прибыль:", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${if (calcProfit >= 0) "+" else ""}$calcProfit ₡", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        color = if (calcProfit >= 0) Primary else Danger)
                }
                Spacer(Modifier.height(12.dp))
                val correct = gameCogs == cogs && gameRent == rent && gameTax == tax
                Text(
                    if (correct) "✅ Идеально! Все конверты заполнены верно."
                    else "⚠️ Есть расхождения. Реально: COGS $cogs, Аренда $rent, Налог $tax.",
                    fontSize = 13.sp, color = if (correct) Primary else Accent
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth(), onClick = onBack)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PnLRow(label: String, amount: Int, color: androidx.compose.ui.graphics.Color, big: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), fontSize = if (big) 16.sp else 14.sp, fontWeight = if (big) FontWeight.Bold else FontWeight.Normal)
        Text("${if (amount >= 0) "+" else ""}$amount ₡", fontSize = if (big) 18.sp else 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun EnvelopeRow(label: String, key: String, currentValue: Int, color: androidx.compose.ui.graphics.Color, onChange: (Int) -> Unit) {
    var textValue by remember { mutableStateOf(currentValue.toString()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        OutlinedTextField(
            value = textValue,
            onValueChange = { v ->
                textValue = v
                onChange(v.toIntOrNull() ?: 0)
            },
            modifier = Modifier.width(100.dp),
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default,
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = color,
                unfocusedBorderColor = color.copy(alpha = 0.5f)
            )
        )
    }
}