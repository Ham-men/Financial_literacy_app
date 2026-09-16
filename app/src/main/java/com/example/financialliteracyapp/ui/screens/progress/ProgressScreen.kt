package com.example.financialliteracyapp.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

/** Прогресс (День 21): задания, цель, итог последнего дня. */
@Composable
fun ProgressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(repo, prefs))

    val quests by vm.quests.collectAsState()
    val goals by vm.goals.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val currentDay by vm.day.collectAsState()

    val doneQuests = quests.count { it.completed }
    val totalQuests = quests.size
    val saveFact = wallet?.saveFact ?: 0

    // Итог последнего прошедшего дня (завершённого): сумма прихода/расхода
    val lastDay = (currentDay - 1).coerceAtLeast(1)
    val dayTx = transactions.filter { it.day == lastDay }
    val income = dayTx.filter { it.kind == "INCOME" }.sumOf { it.amount }
    val expense = dayTx.filter { it.kind == "EXPENSE" }.sumOf { it.amount }
    val balanceDelta = income - expense

    val topicIcon: (String) -> String = { topic ->
        when (topic) {
            "PLANNING" -> "📅"
            "SAVING" -> "🐷"
            "SPENDING" -> "💳"
            else -> "📋"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("📈 Мой прогресс",
            style = MaterialTheme.typography.headlineMedium)
        Text("День $currentDay — как я справляюсь",
            fontSize = 13.sp, color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        // Задания
        AppCard {
            Text("📋 Задания", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = {
                        if (totalQuests > 0) doneQuests.toFloat() / totalQuests else 0f
                    },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).height(10.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("$doneQuests/$totalQuests", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(10.dp))
            quests.forEach { q ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text(
                        if (q.completed) "✅" else "⬜",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("${topicIcon(q.topic)} ${q.title}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                    if (q.completed) Text("+${q.reward} ₡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Цели
        AppCard {
            Text("🎯 Мои цели", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text("В копилке: $saveFact ₡", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            goals.forEach { goal ->
                val progress = if (goal.targetAmount > 0)
                    (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.title, Modifier.weight(1f), fontSize = 14.sp)
                    Text("${goal.currentAmount}/${goal.targetAmount} ₡", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Spacer(Modifier.height(2.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Итог последнего дня
        AppCard {
            Text("📊 Итог дня $lastDay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            if (transactions.isEmpty()) {
                Text("Транзакций пока нет — начни день в ларьке! 🍋",
                    fontSize = 13.sp, color = TextSecondary)
            } else {
                Row(Modifier.fillMaxWidth()) {
                    Text("Получено:", Modifier.weight(1f))
                    Text("+$income ₡", fontWeight = FontWeight.Bold, color = Primary)
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Потрачено:", Modifier.weight(1f))
                    Text("−$expense ₡", fontWeight = FontWeight.Bold, color = Danger)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text("Итог:", Modifier.weight(1f))
                    Text(
                        if (balanceDelta >= 0) "+$balanceDelta ₡" else "−${-balanceDelta} ₡",
                        fontWeight = FontWeight.Bold,
                        color = if (balanceDelta >= 0) Primary else Danger
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        balanceDelta > 0 -> "Отлично! Доходов больше, чем расходов — деньги этого дня остались у тебя."
                        balanceDelta == 0 -> "В ноль. Попробуй продать лимонад подороже или дешевле закупить."
                        else -> "Расходы больше доходов. Посмотри, где можно сэкономить завтра."
                    },
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth(), onClick = onBack)
        Spacer(Modifier.height(24.dp))
    }
}