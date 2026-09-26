package com.example.financialliteracyapp.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.ui.screens.progress.ProgressScreen
import com.example.financialliteracyapp.ui.screens.quests.QuestsScreen
import com.example.financialliteracyapp.ui.screens.shop.GoalScreen

/** Блок «Цели» (разбор.txt): подвкладки [Цели/Задания/Прогресс] в одной вкладке нижней панели. */
@Composable
fun GoalHubScreen() {
    var subtab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = subtab) {
            Tab(selected = subtab == 0, onClick = { subtab = 0 },
                text = { Text("🎯 Цели", fontSize = 12.sp) })
            Tab(selected = subtab == 1, onClick = { subtab = 1 },
                text = { Text("📋 Задания", fontSize = 12.sp) })
            Tab(selected = subtab == 2, onClick = { subtab = 2 },
                text = { Text("📈 Прогресс", fontSize = 12.sp) })
        }

        when (subtab) {
            0 -> GoalScreen(onBack = {}, embedded = true)
            1 -> QuestsScreen(onBack = {}, embedded = true)
            2 -> ProgressScreen(onBack = {}, embedded = true)
        }
    }
}