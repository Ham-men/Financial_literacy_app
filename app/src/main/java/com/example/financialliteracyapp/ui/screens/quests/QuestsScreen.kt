package com.example.financialliteracyapp.ui.screens.quests

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
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun QuestsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: QuestViewModel = viewModel(factory = QuestViewModel.factory(repo))
    val quests by vm.quests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("📋 Задания",
            style = MaterialTheme.typography.headlineMedium)
        Text("Выполняй задания Дедушки Барсука — получай награды",
            fontSize = 13.sp, color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quests) { quest ->
                QuestItem(quest = quest)
            }
        }

        Spacer(Modifier.height(12.dp))
        BigActionButton("← Назад", PrimaryDark, Modifier.fillMaxWidth(), onClick = onBack)
    }
}

@Composable
private fun QuestItem(quest: QuestEntity) {
    val progress = if (quest.target > 0) (quest.progress.toFloat() / quest.target).coerceIn(0f, 1f) else 0f
    val topicIcon = when (quest.topic) {
        "PLANNING" -> "📅"
        "SAVING" -> "🐷"
        "SPENDING" -> "💳"
        else -> "📋"
    }
    val topicLabel = when (quest.topic) {
        "PLANNING" -> "Планирование"
        "SAVING" -> "Сбережения"
        "SPENDING" -> "Покупки"
        else -> quest.topic
    }

    AppCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("$topicIcon ${quest.title}",
                Modifier.weight(1f),
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("+${quest.reward} ₡",
                fontWeight = FontWeight.Bold, color = Accent)
        }
        Spacer(Modifier.height(8.dp))
        Text(topicLabel, fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            color = Primary,
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text("${quest.progress} / ${quest.target}",
            fontSize = 12.sp, color = TextSecondary)
    }
}