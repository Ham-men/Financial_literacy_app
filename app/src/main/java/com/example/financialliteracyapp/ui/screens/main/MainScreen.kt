package com.example.financialliteracyapp.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.components.StatBar
import com.example.financialliteracyapp.ui.theme.*
import com.example.financialliteracyapp.ui.navigation.Routes
import kotlinx.coroutines.launch

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
    val prefs = remember { AppContainer.prefs(context) }
    val vm: MainViewModel = viewModel(factory = MainViewModel.factory(repo, prefs))

    val day by vm.day.collectAsState()
    val gameMinute by vm.gameMinute.collectAsState()
    val sleepMessage by vm.sleepMessage.collectAsState()

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
    val needPlan = wallet?.needPlan ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val savePlan = wallet?.savePlan ?: 0
    // Бюджет «нужное»: если план разложен — банка нужное + мешок, иначе весь мешок
    val planSet = needPlan + wantPlan + savePlan > 0
    val needAvailable = if (planSet) needPlan + cash else cash

    // Эмодзи Финни по состоянию
    val petEmoji = when {
        hunger < 30 -> "😿"
        energy < 30 -> "😴"
        mood < 30 -> "🙀"
        hunger < 60 || mood < 60 -> "🦝"
        else -> "😺"
    }

    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top row: Date/Time left, Stats right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "День $day — ${GameRules.dateForDay(day)}  ${GameRules.timeLabel(gameMinute)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("нужное  \\  желаемое  \\  копилка", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("$needPlan  \\  $wantPlan  \\  $savePlan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(4.dp))
                Text("параметры", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("уровень  $level ($stageLabel)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("сытость  $hunger", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("настроение  $mood", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("бодрость  $energy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Window at top center
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF87CEEB), Color(0xFF7CFC00)),
                            start = Offset.Zero,
                            end = Offset(0f, 120f)
                        )
                    )
                    .border(width = 4.dp, color = Color(0xFF6B4C3A), shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🪟", fontSize = 48.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Cat on rug - left side, Bed right side - more centered
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cat area left
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rug
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(50.dp)
                        .background(Color(0xFF5C3A21))
                        .clip(RoundedCornerShape(50.dp))
                        .border(width = 3.dp, color = Color(0xFFD2B48C))
                )
                // Cat on rug
                Box(
                    modifier = Modifier
                        .width(55.dp)
                        .height(55.dp)
                        .background(Color.Black)
                        .padding(6.dp)
                        .offset(y = (-35).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(petEmoji, fontSize = 38.sp)
                }
            }

            // Bed right side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛏️", fontSize = 60.sp, modifier = Modifier
                    .graphicsLayer { rotationZ = -5f }
                    .clickable { vm.sleep() }
                )
                Text(
                    if (GameRules.canSleep(gameMinute)) "Лечь спать → новый день" else "Спать: 18:00–23:00",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (GameRules.canSleep(gameMinute)) Color(0xFF2E7D32) else TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Сообщение о сне (список_scene)
        sleepMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(
                msg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Feed + Heal buttons at bottom center
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { scope.launch { vm.feed() } },
                enabled = needAvailable >= Balance.FEED_COST,
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🍖 Покормить — ${Balance.FEED_COST}₡", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { scope.launch { vm.heal() } },
                enabled = needAvailable >= Balance.HEAL_COST,
                modifier = Modifier
                    .height(48.dp)
                    .padding(horizontal = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("💊 Лечение — ${Balance.HEAL_COST}₡", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}