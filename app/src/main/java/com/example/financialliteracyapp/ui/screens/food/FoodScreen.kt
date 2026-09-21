package com.example.financialliteracyapp.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.screens.pet.PetViewModel
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Сцена «Еда» — кормление и лечение Финни.
 * Соответствует еда.html: холодильник, аптечка, кнопки действий.
 * Связана с кормлением зверька через PetViewModel.
 */
@Composable
fun FoodScreen(
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()
    val vm: PetViewModel = viewModel(factory = PetViewModel.factory(repo))

    val pet by vm.pet.collectAsState()
    val wallet by repo.observeWallet().collectAsState(initial = null)

    val hunger = pet?.hunger ?: 70
    val mood = pet?.mood ?: 80
    val energy = pet?.energy ?: 90
    val cash = wallet?.cash ?: 500

    var showFeedDialog by remember { mutableStateOf(false) }
    var showHealDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Шапка
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Дата 01.01.2020 12:00",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                "нужное\n${wallet?.needPlan ?: 100}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Основная сцена
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Финни (котёнок)
            Box(
                modifier = Modifier
                    .offset { IntOffset(8, 12) }
                    .background(Color.Black, RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                val petEmoji = when {
                    hunger < 30 -> "😿"
                    energy < 30 -> "😴"
                    mood < 30 -> "🙀"
                    hunger < 60 || mood < 60 -> "🦝"
                    else -> "🐱"
                }
                Text(petEmoji, fontSize = 36.sp)
            }

            // Холодильник
            Text(
                "🧊",
                fontSize = 56.sp,
                modifier = Modifier
                    .offset { IntOffset(60, 15) }
                    .align(Alignment.TopEnd)
                    .clickable { showFeedDialog = true }
            )

            // Аптечка
            Text(
                "🧰",
                fontSize = 44.sp,
                modifier = Modifier
                    .offset { IntOffset(60, 55) }
                    .align(Alignment.TopEnd)
                    .clickable { showHealDialog = true }
            )

            // Центральный текст
            Text(
                "команда для еды и лечения.\nденьги тратятся из банки нужное",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset { IntOffset(8, 55) }
                    .width(200.dp)
            )

            // Кнопки действий
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { showFeedDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("🍖 Покормить — 20₡", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = { showHealDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("💊 Лечение — 30₡", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Диалог кормления
    if (showFeedDialog) {
        AlertDialog(
            onDismissRequest = { showFeedDialog = false },
            title = { Text("🍖 Покормить Финни?") },
            text = { Text("Списать 20 ₡ из нужного. Сытость +15.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vm.feed()
                        showFeedDialog = false
                    }
                }) { Text("Покормить") }
            },
            dismissButton = {
                TextButton(onClick = { showFeedDialog = false }) { Text("Отмена") }
            }
        )
    }

    // Диалог лечения
    if (showHealDialog) {
        AlertDialog(
            onDismissRequest = { showHealDialog = false },
            title = { Text("💊 Лечение Финни?") },
            text = { Text("Списать 30 ₡ из нужного. Настроение +20.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vm.play()
                        showHealDialog = false
                    }
                }) { Text("Лечить") }
            },
            dismissButton = {
                TextButton(onClick = { showHealDialog = false }) { Text("Отмена") }
            }
        )
    }
}
