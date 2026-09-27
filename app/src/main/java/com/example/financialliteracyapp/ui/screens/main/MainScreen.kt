package com.example.financialliteracyapp.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.R
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onOpenAppearance: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: MainViewModel = viewModel(factory = MainViewModel.factory(repo, prefs))

    val gameMinute by vm.gameMinute.collectAsState()
    val sleepMessage by vm.sleepMessage.collectAsState()

    val pet by vm.pet.collectAsState()
    val wallet by vm.wallet.collectAsState()
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

    // Кастомизация из сущности: тело (как выбрано в онбординге / внешний вид)
    val petBody = pet?.bodyType ?: 0
    val bodyIcons = listOf(R.drawable.ic_pet_raccoon, R.drawable.ic_pet_raccoon_gray, R.drawable.ic_pet_raccoon_pink)

    val cash = wallet?.cash ?: 500
    val needPlan = wallet?.needPlan ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val savePlan = wallet?.savePlan ?: 0
    // Бюджет «нужное»: если план разложен — банка нужное + мешок, иначе весь мешок
    val planSet = needPlan + wantPlan + savePlan > 0
    val needAvailable = if (planSet) needPlan + cash else cash

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top row: only level + stats (day/banks already shown in TimeBankBar HUD)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text("уровень  $level ($stageLabel)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text("сытость  $hunger", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("настроение  $mood", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("бодрость  $energy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Блок цели (остальные блоки убраны — банки видны в HUD TimeBankBar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val goal = currentGoal
            InfoChip(
                "🎯",
                if (goal != null) "Цель: ${goal.title} ${goal.currentAmount}/${goal.targetAmount}" else "Цель: нет цели"
            )
        }

        Spacer(Modifier.height(4.dp))

        // Pet on scene - left side, Bed right side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet area left
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Character: только тело (без аксессуара и без badge-эмодзи)
                Image(
                    painterResource(bodyIcons[petBody]),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.height(10.dp))
            }

            // Bed right side (not clickable — sleep via button below)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛏️", fontSize = 44.sp, modifier = Modifier
                    .graphicsLayer { rotationZ = -5f }
                )
                Spacer(Modifier.height(2.dp))
                Button(
                    onClick = { vm.sleep() },
                    enabled = GameRules.canSleep(gameMinute),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C6BC0),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFC5CAE9),
                        disabledContentColor = Color(0xFF5C6BC0)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("💤 Спать — новый день", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    if (GameRules.canSleep(gameMinute)) "Доступно сейчас" else "Спать: 18:00–23:00",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (GameRules.canSleep(gameMinute)) Color(0xFF2E7D32) else TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Сообщение о сне (список_scene)
        sleepMessage?.let { msg ->
            Spacer(Modifier.height(2.dp))
            Text(
                msg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        // Button row at bottom center: feed, heal, appearance (в портрете — в столбик, чтобы всё было видно)
        val feedButton: @Composable () -> Unit = {
            Button(
                onClick = { scope.launch { vm.feed() } },
                enabled = needAvailable >= Balance.FEED_COST,
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🍖 Корм — ${Balance.FEED_COST}₡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        val healButton: @Composable () -> Unit = {
            Button(
                onClick = { scope.launch { vm.heal() } },
                enabled = needAvailable >= Balance.HEAL_COST,
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("💊 Лечение — ${Balance.HEAL_COST}₡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        val appearanceButton: @Composable () -> Unit = {
            Button(
                onClick = onOpenAppearance,
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7E57C2),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🎨 Внешний вид", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        val configuration = LocalConfiguration.current
        if (configuration.screenWidthDp < configuration.screenHeightDp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp).padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                feedButton()
                healButton()
                appearanceButton()
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp).padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                feedButton()
                healButton()
                appearanceButton()
            }
        }

        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun RowScope.InfoChip(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE0F7FA),
        border = BorderStroke(1.dp, Color(0xFF80DEEA)),
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = "$icon $text",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .fillMaxWidth()
        )
    }
}