package com.example.financialliteracyapp.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun JournalScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: JournalViewModel = viewModel(factory = JournalViewModel.factory(repo))
    val txs by vm.transactions.collectAsState()

    // мини-игра: разложить первые 4 операции (если мало — берём демо)
    var index by remember { mutableIntStateOf(0) }
    var income by remember { mutableIntStateOf(0) }
    var expense by remember { mutableIntStateOf(0) }
    // стартовая касса из wallet/shop cash + баланс
    val realIncome = txs.filter { it.kind == "INCOME" }.sumOf { it.amount }
    val realExpense = txs.filter { it.kind == "EXPENSE" }.sumOf { it.amount }
    val realBalance = realIncome - realExpense

    // для игры используем реальные транзакции, если есть ≥4, иначе демо
    val gameList = if (txs.size >= 4) txs.take(4) else null
    val current = gameList?.getOrNull(index)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("📒 Кассовая книга",
            style = MaterialTheme.typography.headlineMedium)
        Text("Разложи операции по колонкам",
            fontSize = 14.sp, color = TextSecondary)

        Spacer(Modifier.height(24.dp))

        if (current != null) {
            AppCard {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${current.category} • день ${current.day}",
                        fontSize = 14.sp, color = TextSecondary)
                    Text(
                        if (current.kind == "INCOME") "+${current.amount} ₡" else "−${current.amount} ₡",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Accent
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth()) {
                BigActionButton("➕ Приход", Primary, Modifier.weight(1f).height(80.dp)) {
                    if (current.kind == "INCOME") income += current.amount else expense += current.amount
                    index++
                }
                Spacer(Modifier.width(8.dp))
                BigActionButton("➖ Расход", Danger, Modifier.weight(1f).height(80.dp)) {
                    if (current.kind == "EXPENSE") expense += current.amount else income += current.amount
                    index++
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Подсказка: Приход — деньги пришли, Расход — ушли", fontSize = 12.sp, color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Если транзакций <4 — показываем демо-режим с подсказкой
            if (txs.isEmpty()) {
                AppCard {
                    Text("Пока нет операций. Сыграй день: закупи товар, поставь цену, обслужи клиентов.",
                        fontSize = 14.sp, color = TextSecondary)
                }
                Spacer(Modifier.height(12.dp))
            }
            AppCard {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📊 Результат",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Приход:", Modifier.weight(1f))
                        Text("+$income ₡ (реально +$realIncome ₡)", fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("Расход:", Modifier.weight(1f))
                        Text("−$expense ₡ (реально −$realExpense ₡)", fontWeight = FontWeight.Bold, color = Danger)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Остаток:", Modifier.weight(1f),
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${income - expense} ₡ (реально $realBalance ₡)", fontWeight = FontWeight.Bold,
                            fontSize = 18.sp, color = Primary)
                    }
                }
            }
            // Список реальных транзакций
            if (txs.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Последние операции", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(txs.take(20)) { tx ->
                        AppCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (tx.kind == "INCOME") "➕" else "➖", fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(tx.category, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("день ${tx.day}", fontSize = 12.sp, color = TextSecondary)
                                }
                                Text(
                                    (if (tx.kind == "INCOME") "+" else "−") + "${tx.amount} ₡",
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.kind == "INCOME") Primary else Danger
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Text("Остаток в кассе: $realBalance ₡",
            fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(12.dp))

        BigActionButton(
            text = if (gameList != null && index >= gameList.size) "✅ Готово" else if (txs.isEmpty()) "Назад" else "✅ Готово",
            color = Primary,
            modifier = Modifier.fillMaxWidth()
        ) { onFinish() }
    }
}