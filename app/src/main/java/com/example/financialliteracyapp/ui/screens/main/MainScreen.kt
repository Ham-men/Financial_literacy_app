package com.example.financialliteracyapp.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.components.StatBar
import com.example.financialliteracyapp.ui.theme.*
import androidx.compose.ui.graphics.Color

@Composable
fun MainScreen(
    onOpenMap: () -> Unit,
    onOpenKiosk: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenReference: () -> Unit,
    onOpenAdult: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: MainViewModel = viewModel(factory = MainViewModel.factory(repo))

    val prefs = remember { AppContainer.prefs(context) }
    val currentDay by prefs.currentDay.collectAsState(initial = 1)

    val pet by vm.pet.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val activeQuest by vm.activeQuest.collectAsState(initial = null)
    val currentGoal by vm.currentGoal.collectAsState(initial = null)

    val hunger = pet?.hunger ?: 70
    val mood = pet?.mood ?: 80
    val energy = pet?.energy ?: 90
    val level = pet?.level ?: 1
    val petName = pet?.name ?: "Финни"
    val stageLabel = when (level) {
        1 -> "Малыш"
        2 -> "Подросток"
        3 -> "Хозяин ларька"
        else -> "Малыш"
    }

    val cash = wallet?.cash ?: 500
    val saveFact = wallet?.saveFact ?: 0
    
    // Эмодзи Финни по состоянию
    val petEmoji = when {
        hunger < 30 -> "😿"
        energy < 30 -> "😴"
        mood < 30 -> "🙀"
        hunger < 60 || mood < 60 -> "🦝"
        else -> "😺"
    }
    
    // Прогресс цели
    val goalProgress = currentGoal?.let { 
        if (it.targetAmount > 0) (it.currentAmount.toFloat() / it.targetAmount).coerceIn(0f, 1f) else 0f 
    } ?: 0f
    val goalTitle = currentGoal?.title ?: "Мячик для Финни"
    val goalCurrent = currentGoal?.currentAmount ?: 0
    val goalTarget = currentGoal?.targetAmount ?: 300
    
    // Активное задание
    val questTitle = activeQuest?.title ?: "Нет активного задания"
    val questReward = activeQuest?.reward ?: 0
    val questProgress = activeQuest?.let { 
        if (it.target > 0) (it.progress.toFloat() / it.target).coerceIn(0f, 1f) else 0f 
    } ?: 0f
    val questTopicLabel = activeQuest?.let { 
        when (it.topic) {
            "PLANNING" -> "Планирование"
            "SAVING" -> "Сбережения"
            "SPENDING" -> "Покупки"
            else -> it.topic
        }
    } ?: ""

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Верхняя панель: Финни + уровень + кэш + меню настроек
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(petName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("День $currentDay · $stageLabel", fontSize = 12.sp, color = TextSecondary)
            }
            Chip("Ур. $level")
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Text("⚙️", fontSize = 22.sp)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("📈 Мой прогресс") }, onClick = { showMenu = false; onOpenProgress() })
                    DropdownMenuItem(text = { Text("📖 Справочник") }, onClick = { showMenu = false; onOpenReference() })
                    DropdownMenuItem(text = { Text("👨‍👩‍👧 Взрослым") }, onClick = { showMenu = false; onOpenAdult() })
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Прогресс-бар дня (демо: 5 периодов)
        LinearProgressIndicator(
            progress = { (currentDay / 5f).coerceIn(0f, 1f) },
            color = Primary,
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Финни (крупно)
        Text(
            text = petEmoji,
            fontSize = 120.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Шкалы
        StatBar("🍖 Сытость", hunger, Hunger)
        StatBar("😊 Настроение", mood, Mood)
        StatBar("⚡ Бодрость", energy, Energy)
        
        Spacer(Modifier.height(16.dp))
        
        // Копилка + цель
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Копилка
            Surface(
                modifier = Modifier.weight(1f).padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Primary.copy(alpha = 0.1f)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐷 Копилка", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text("$saveFact ₡", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Primary)
                    Text("на цель", fontSize = 12.sp, color = TextSecondary)
                }
            }
            
            // Цель
            Surface(
                modifier = Modifier.weight(1f).padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯 $goalTitle", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Text("$goalCurrent / $goalTarget ₡", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Активное задание
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2196F3).copy(alpha = 0.1f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("📋 Задание: $questTitle", Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("+$questReward ₡", fontWeight = FontWeight.Bold, color = Accent, fontSize = 14.sp)
                }
                Text(questTopicLabel, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { questProgress },
                    color = Color(0xFF2196F3),
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
                Text("${activeQuest?.progress ?: 0} / ${activeQuest?.target ?: 1}", fontSize = 12.sp, color = TextSecondary)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Компактные входы: Мир и Мой ларёк (остальное — в меню ⚙️)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickTile("🗺️", "Мир", Color(0xFF26A69A), Modifier.weight(1f)) { onOpenMap() }
            QuickTile("🍋", "Мой ларёк", Color(0xFFEF6C00), Modifier.weight(1f)) { onOpenKiosk() }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

/** Маленькая плитка-кнопка в ряд. */
@Composable
private fun QuickTile(
    icon: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}