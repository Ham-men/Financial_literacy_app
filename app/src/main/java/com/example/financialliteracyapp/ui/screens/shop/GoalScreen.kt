package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun GoalScreen(onBack: () -> Unit, embedded: Boolean = false) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: GoalViewModel = viewModel(factory = GoalViewModel.factory(repo))
    val goals by vm.goals.collectAsState()
    val wallet by vm.wallet.collectAsState()

    val cash = wallet?.cash ?: 500
    val saveFact = wallet?.saveFact ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🐷 Копилка и цели", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(12.dp)
            ) {
                Text("💰 $cash ₡", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Общая копилка
        AppCard {
            Text("💰 Моя копилка", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Отложено:", Modifier.weight(1f))
                Text("$saveFact ₡", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Primary)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BigActionButton("Пополнить", Primary, Modifier.weight(1f)) {
                    // TODO: показать диалог пополнения
                }
                BigActionButton("Снять", PrimaryDark, Modifier.weight(1f), enabled = saveFact > 0) {
                    // TODO: показать диалог снятия
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Цели
        Text("🎯 Цели накопления", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        goals.forEach { goal ->
            GoalCard(
                goal = goal,
                cash = cash,
                onAdd = { amount -> vm.addToGoal(goal.id, amount) },
                onWithdraw = { amount -> vm.withdrawFromGoal(goal.id, amount) }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))
        if (!embedded) {
            BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth()) { onBack() }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GoalCard(
    goal: com.example.financialliteracyapp.data.local.entity.GoalEntity,
    cash: Int,
    onAdd: (Int) -> Unit,
    onWithdraw: (Int) -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
    val remaining = goal.targetAmount - goal.currentAmount
    val daysEstimate = if (remaining > 0) (remaining / 50).coerceAtLeast(1) else 0

    AppCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(goal.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${goal.currentAmount} / ${goal.targetAmount} ₡", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            color = Primary,
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (goal.completed || goal.currentAmount >= goal.targetAmount)
                "✅ Цель достигнута!"
            else
                "Осталось $remaining ₡ • ~$daysEstimate дней по 50 ₡/день",
            fontSize = 12.sp, color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BigActionButton(
                text = "+50 ₡",
                color = Primary,
                modifier = Modifier.weight(1f),
                enabled = cash >= 50
            ) { onAdd(50) }
            BigActionButton(
                text = "+100 ₡",
                color = Primary,
                modifier = Modifier.weight(1f),
                enabled = cash >= 100
            ) { onAdd(100) }
            BigActionButton(
                text = "Снять",
                color = PrimaryDark,
                modifier = Modifier.weight(1f),
                enabled = goal.currentAmount > 0
            ) {
                // TODO: показать диалог ввода суммы для снятия
                onWithdraw(minOf(goal.currentAmount, 50))
            }
        }
    }
}