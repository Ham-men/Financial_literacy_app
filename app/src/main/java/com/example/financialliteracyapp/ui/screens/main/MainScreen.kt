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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
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
    val needPlan = wallet?.needPlan ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val savePlan = wallet?.savePlan ?: 0

    // Эмодзи Финни по состоянию
    val petEmoji = when {
        hunger < 30 -> "😿"
        energy < 30 -> "😴"
        mood < 30 -> "🙀"
        hunger < 60 || mood < 60 -> "🦝"
        else -> "😺"
    }

    var showMenu by remember { mutableStateOf(false) }

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
                "Дата 01.01.2020  12:00",
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
                    Text("🐱", fontSize = 38.sp)
                }
            }

            // Bed right side
            Text("🛏️", fontSize = 60.sp, modifier = Modifier
                .graphicsLayer { rotationZ = -5f }
            )
        }

        Spacer(Modifier.height(32.dp))

        // Feed button at bottom center - use Box with contentAlignment
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp).padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { /* TODO: feed action */ },
                modifier = Modifier
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🍖 Покормить Финни", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}