package com.example.financialliteracyapp.ui.screens.entertainment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.CatalogItemEntity
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

/** Комната развлечений: игрушки дают Финни опыт → уровень растит ставку копилки. */
@Composable
fun EntertainmentScreen() {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: EntertainmentViewModel = viewModel(factory = EntertainmentViewModel.factory(repo))

    val catalog by vm.catalog.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val pet by vm.pet.collectAsState()
    val quests by vm.quests.collectAsState()

    val toys = catalog.filter { it.category == "WANT" }
    val cash = wallet?.cash ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val planSet = (wallet?.needPlan ?: 0) + wantPlan + (wallet?.savePlan ?: 0) > 0
    val availableForWant = if (planSet) wantPlan + cash else cash

    val xp = pet?.xp ?: 0
    val level = GameRules.playLevel(xp)
    val interest = GameRules.playInterestPercent(level)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("🧸 Комната развлечений",
            style = MaterialTheme.typography.headlineMedium)
        Text("Игрушки дарят Финни опыт. Чем выше уровень — тем больше проценты в копилке",
            fontSize = 13.sp, color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        // Уровень развлечений и ставка копилки
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Уровень развлечений", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Опыт: $xp / ${GameRules.MAX_PLAY_LEVEL * GameRules.XP_PER_LEVEL}",
                        fontSize = 13.sp, color = TextSecondary)
                }
                Text("$level", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { xp.toFloat() / (GameRules.MAX_PLAY_LEVEL * GameRules.XP_PER_LEVEL) },
                color = Primary,
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (level >= GameRules.MAX_PLAY_LEVEL) "🎉 Максимальный уровень! Копилка даёт $interest% в день"
                else "Сейчас копилка даёт $interest% в день. Следующий уровень развлечений — ${interest + 1}%",
                fontSize = 13.sp, color = if (level >= GameRules.MAX_PLAY_LEVEL) Primary else TextSecondary
            )
        }

        Spacer(Modifier.height(12.dp))

        // Задания комнаты развлечений
        val playQuests = quests.filter { it.topic == "PLAY" }
        if (playQuests.isNotEmpty()) {
            AppCard {
                Text("📋 Задания развлечений", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                playQuests.forEach { q ->
                    val progress = if (q.target > 0) (q.progress.toFloat() / q.target).coerceIn(0f, 1f) else 0f
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(
                            if (q.completed) "✅" else "⬜",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(q.title, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        if (q.completed) Text("+${q.reward} ₡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                        else Text("${q.progress}/${q.target}", fontSize = 12.sp, color = TextSecondary)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Primary,
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Список игрушек
        Text("🎁 Игрушки", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (toys.isEmpty()) {
            Text("Игрушек пока нет", fontSize = 13.sp, color = TextSecondary)
        } else {
            toys.forEach { toy ->
                ToyCard(
                    toy = toy,
                    available = availableForWant,
                    onBuy = { vm.buy(toy.id) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ToyCard(
    toy: CatalogItemEntity,
    available: Int,
    onBuy: () -> Unit
) {
    val canAfford = available >= toy.price
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(toy.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    if (toy.description.isNotBlank()) {
                        Text(toy.description, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Text("${toy.price} ₡", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (toy.xpReward > 0) {
                    Text(
                        "Опыт +${toy.xpReward}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .background(Primary, RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (toy.moodEffect > 0) {
                    Text("Настроение +${toy.moodEffect}", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(10.dp))
            BigActionButton(
                text = if (canAfford) "Купить" else "Не хватает ${toy.price - available} ₡",
                color = if (canAfford) Primary else Color(0xFF9E9E9E),
                modifier = Modifier.fillMaxWidth(),
                enabled = canAfford
            ) { onBuy() }
        }
    }
}