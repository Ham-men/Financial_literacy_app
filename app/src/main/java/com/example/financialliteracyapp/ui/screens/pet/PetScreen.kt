package com.example.financialliteracyapp.ui.screens.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Сцена «Дом» — комната с Финни. Соответствует кот.html:
 * - Окно, коврике, Финни на коврике, кровать
 * - Кнопка «Покормить Финни»
 * - Шапка с датой и параметрами
 */
@Composable
fun PetScreen(
    onOpenBanks: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenKiosk: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenReport: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()
    val vm: PetViewModel = viewModel(factory = PetViewModel.factory(repo))

    val pet by vm.pet.collectAsState()
    val wallet by vm.wallet.collectAsState()

    val hunger = pet?.hunger ?: 70
    val mood = pet?.mood ?: 80
    val energy = pet?.energy ?: 90
    val coins = wallet?.cash ?: 500
    val petName = pet?.name ?: "Финни"
    val level = pet?.level ?: 1

    val petEmoji = when {
        hunger < 30 -> "😿"
        energy < 30 -> "😴"
        mood < 30 -> "🙀"
        hunger < 60 || mood < 60 -> "🦝"
        else -> "🐱"
    }

    var showFeedConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Шапка: дата + параметры
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Дата 01.01.2020 12:00",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "нужное \\ желаемое \\ копилка",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    "${wallet?.needPlan ?: 100} \\ ${wallet?.wantPlan ?: 200} \\ ${wallet?.savePlan ?: 200}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text("параметры", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("сытость $hunger%", fontSize = 10.sp, color = TextSecondary)
                Text("настроение $mood%", fontSize = 10.sp, color = TextSecondary)
                Text("бодрость $energy%", fontSize = 10.sp, color = TextSecondary)
            }
        }

        // Комната
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Окно
            Box(
                modifier = Modifier
                    .offset { IntOffset(38, 10) }
                    .size(120.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            0f to Color(0xFF87CEEB),
                            1f to Color(0xFF7CFC00)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(10.dp, Color(0xFF6B4C3A), RoundedCornerShape(4.dp))
            ) {
                // Перекрестие окна
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.48f to Color.Transparent,
                                0.48f to Color(0xFF6B4C3A),
                                0.52f to Color(0xFF6B4C3A),
                                0.52f to Color.Transparent,
                                1f to Color.Transparent
                            )
                        )
                )
                Text("🪟", fontSize = 36.sp, modifier = Modifier.align(Alignment.Center))
            }

            // Кот на коврике
            Box(
                modifier = Modifier
                    .offset { IntOffset(12, 52) }
                    .size(180.dp, 65.dp)
            ) {
                // Коврик (овальный)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(0xFF5C3A21),
                            CircleShape
                        )
                        .border(7.dp, Color(0xFFD2B48C), CircleShape)
                )
                // Финни
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-38).dp)
                        .background(Color.Black, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Text(petEmoji, fontSize = 36.sp)
                }
            }

            // Кровать
            Text(
                "🛏️",
                fontSize = 56.sp,
                modifier = Modifier
                    .offset { IntOffset(62, 55) }
                    .graphicsLayer { rotationZ = -5f }
            )
        }

        // Кнопка кормления
        Button(
            onClick = { showFeedConfirm = true },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .height(36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("🍖 Покормить Финни", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    if (showFeedConfirm) {
        AlertDialog(
            onDismissRequest = { showFeedConfirm = false },
            title = { Text("Покормить Финни?") },
            text = { Text("Списать 20 ₡ из нужного?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vm.feed()
                        showFeedConfirm = false
                    }
                }) { Text("Покормить") }
            },
            dismissButton = {
                TextButton(onClick = { showFeedConfirm = false }) { Text("Отмена") }
            }
        )
    }
}
