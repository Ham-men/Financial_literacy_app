package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.GoalEntity
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*
import kotlin.math.ceil

@Composable
fun GoalScreen(onBack: () -> Unit, embedded: Boolean = false) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: GoalViewModel = viewModel(factory = GoalViewModel.factory(repo))
    val goals by vm.goals.collectAsState()
    val avgSave by vm.avgDailySave.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("🎯 Цели накопления", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Выбери цель ⭐ — её прогресс виден на главном экране",
            fontSize = 12.sp, color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        goals.forEach { goal ->
            GoalCard(
                goal = goal,
                avgSave = avgSave,
                onSetActive = { vm.setActive(goal.id) },
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
    goal: GoalEntity,
    avgSave: Int,
    onSetActive: () -> Unit,
    onAdd: (Int) -> Unit,
    onWithdraw: (Int) -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0)
    val days = if (remaining > 0) ceil(remaining.toDouble() / avgSave.coerceAtLeast(1)).toInt() else 0
    var amountDialog by remember { mutableStateOf<GoalAction?>(null) }

    AppCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (goal.isActive) "⭐ " else "○ ",
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = onSetActive)
            )
            Text(goal.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${goal.currentAmount} / ${goal.targetAmount} ₡",
                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Primary)
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
                "Осталось $remaining ₡ • ~$days дн. по $avgSave ₡/день",
            fontSize = 12.sp, color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!goal.isActive) {
                BigActionButton("⭐ Выбрать", PrimaryDark, Modifier.weight(1f), onClick = onSetActive)
            } else {
                BigActionButton(
                    text = "+50 ₡",
                    color = Primary,
                    modifier = Modifier.weight(1f),
                    enabled = !goal.completed
                ) { onAdd(50) }
                BigActionButton(
                    text = "+100 ₡",
                    color = Primary,
                    modifier = Modifier.weight(1f),
                    enabled = !goal.completed
                ) { onAdd(100) }
            }
            if (goal.isActive) {
                BigActionButton(
                    text = "Снять…",
                    color = PrimaryDark,
                    modifier = Modifier.weight(1f),
                    enabled = goal.currentAmount > 0
                ) { amountDialog = GoalAction.Withdraw }
            }
        }

        if (goal.isActive) {
            Spacer(Modifier.height(8.dp))
            amountDialog?.let { action ->
                GoalAmountDialog(
                    goal = goal,
                    avgSave = avgSave,
                    isWithdraw = action == GoalAction.Withdraw,
                    onConfirm = { amount ->
                        if (action == GoalAction.Withdraw) onWithdraw(amount) else onAdd(amount)
                    },
                    onDismiss = { amountDialog = null }
                )
            }
        }
    }
}

private enum class GoalAction { Withdraw }

/** Диалог суммы для цели (снятие с подтверждением и предпросмотром срока). */
@Composable
private fun GoalAmountDialog(
    goal: GoalEntity,
    avgSave: Int,
    isWithdraw: Boolean,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("50") }
    val amount = text.toIntOrNull() ?: 0
    val remainingNow = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(amount); onDismiss() },
                enabled = amount > 0 && (if (isWithdraw) amount <= goal.currentAmount else amount <= (goal.targetAmount - goal.currentAmount))
            ) { Text(if (isWithdraw) "Снять" else "Отложить", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text(goal.title) },
        text = {
            Column {
                Text(if (isWithdraw) "Сколько снять из цели?" else "Сколько отложить на цель?", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() }.take(5) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    suffix = { Text("₡") }
                )
                Spacer(Modifier.height(8.dp))
                val delta = if (isWithdraw) amount else -amount
                val newRemaining = (remainingNow + delta).coerceAtLeast(0)
                val newDays = if (newRemaining > 0) ceil(newRemaining.toDouble() / avgSave.coerceAtLeast(1)).toInt() else 0
                Text(
                    if (isWithdraw)
                        "Цель вернётся к ${newRemaining} ₡ — до неё ~$newDays дн."
                    else
                        "Останется $newRemaining ₡ — до цели ~$newDays дн. (по $avgSave ₡/день)",
                    fontSize = 12.sp, color = TextSecondary
                )
            }
        }
    )
}