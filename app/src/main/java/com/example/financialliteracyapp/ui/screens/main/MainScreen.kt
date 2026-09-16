package com.example.financialliteracyapp.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
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
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.components.StatBar
import com.example.financialliteracyapp.ui.theme.*
import androidx.compose.ui.graphics.Color

@Composable
fun MainScreen(
    onOpenBanks: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenKiosk: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenAdult: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenReference: () -> Unit
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNav(
                onOpenBanks = onOpenBanks,
                onOpenMarket = onOpenMarket,
                onOpenKiosk = onOpenKiosk,
                onOpenGoals = onOpenGoals,
                onOpenQuests = onOpenQuests,
                onOpenReport = onOpenReport,
                onOpenAdult = onOpenAdult
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Верхняя панель: Финни + уровень + кэш
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(petName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("День $currentDay · $stageLabel", fontSize = 12.sp, color = TextSecondary)
                }
                Chip("Ур. $level")
                Spacer(Modifier.width(8.dp))
                Chip("💰 $cash ₡")
            }
            
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
            
            // Крупные кнопки навигации
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                val navActions = listOf<NavAction>(
                    NavAction("📅 План дня", "Разложи деньги по банкам", Color(0xFF4CAF50)) { onOpenBanks() },
                    NavAction("🏪 Магазин", "Покупки для Финни", Color(0xFF2196F3)) { onOpenMarket() },
                    NavAction("🍋 Ларёк", "Заработок", Color(0xFFFF9800)) { onOpenKiosk() },
                    NavAction("🐷 Копилка / Цели", "Накопления", Color(0xFF4CAF50)) { onOpenGoals() },
                    NavAction("📋 Задания", "Получи награды", Color(0xFF2196F3)) { onOpenQuests() },
                    NavAction("📊 Отчёт", "Итог дня", Color(0xFF9C27B0)) { onOpenReport() },
                    NavAction("📈 Мой прогресс", "Задания, цели, итог", Color(0xFF00ACC1)) { onOpenProgress() },
                    NavAction("📖 Справочник", "Денежные слова", Color(0xFF795548)) { onOpenReference() },
                )
                items(navActions) { action ->
                    NavButton(action = action)
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NavButton(action: NavAction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, action.color.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .clickable { action.onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(action.icon, fontSize = 28.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(action.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(action.desc, fontSize = 12.sp, color = TextSecondary)
            }
            Text("→", fontSize = 20.sp, color = action.color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomNav(
    onOpenBanks: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenKiosk: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenAdult: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠") }, label = { Text("Дом") })
        NavigationBarItem(selected = false, onClick = onOpenBanks, icon = { Text("🏦") }, label = { Text("План") })
        NavigationBarItem(selected = false, onClick = onOpenMarket, icon = { Text("🏪") }, label = { Text("Магазин") })
        NavigationBarItem(selected = false, onClick = onOpenGoals, icon = { Text("🐷") }, label = { Text("Копилка") })
        NavigationBarItem(selected = false, onClick = onOpenQuests, icon = { Text("📋") }, label = { Text("Задания") })
        NavigationBarItem(selected = false, onClick = onOpenAdult, icon = { Text("👨‍👩‍👧") }, label = { Text("Взросл.") })
    }
}

private data class NavAction(
    val title: String,
    val desc: String,
    val color: androidx.compose.ui.graphics.Color,
    val onClick: () -> Unit
) {
    val icon: String
        get() = when (title) {
            "📅 План дня" -> "📅"
            "🏪 Магазин" -> "🏪"
            "🍋 Ларёк" -> "🍋"
            "🐷 Копилка / Цели" -> "🐷"
            "📋 Задания" -> "📋"
            "📊 Отчёт" -> "📊"
            "📈 Мой прогресс" -> "📈"
            "📖 Справочник" -> "📖"
            else -> "📌"
        }
}