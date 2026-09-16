package com.example.financialliteracyapp.ui.screens.pet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.components.StatBar
import com.example.financialliteracyapp.ui.theme.*

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
    val vm: PetViewModel = viewModel(factory = PetViewModel.factory(repo))

    val pet by vm.pet.collectAsState()
    val wallet by vm.wallet.collectAsState()

    val hunger = pet?.hunger ?: DefaultPet.hunger
    val mood = pet?.mood ?: DefaultPet.mood
    val energy = pet?.energy ?: DefaultPet.energy
    val coins = wallet?.cash ?: DefaultWallet.cash
    val petName = pet?.name ?: DefaultPet.name
    val level = pet?.level ?: 1

    // Вариант C (MVP): эмодзи вместо Lottie-ассета.
    // Lottie вернём в v0.2, когда будет готов pet_idle.json в res/raw.
    val petEmoji = when {
        hunger < 30 -> "😿"
        energy < 30 -> "😴"
        mood < 30 -> "🙀"
        hunger < 60 || mood < 60 -> "🦝"
        else -> "😺"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNav(
                onOpenBanks = onOpenBanks,
                onOpenMarket = onOpenMarket,
                onOpenKiosk = onOpenKiosk,
                onOpenQuests = onOpenQuests,
                onOpenReport = onOpenReport
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Верхняя панель
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(petName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                Chip("Ур. $level")
                Spacer(Modifier.width(8.dp))
                Chip("💰 $coins ₡")
            }

            Spacer(Modifier.height(16.dp))

            // Питомец (вариант C — эмодзи)
            Text(
                text = petEmoji,
                fontSize = 120.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // Шкалы
            StatBar("🍖 Голод", hunger, Hunger)
            StatBar("😊 Настроение", mood, Mood)
            StatBar("⚡ Энергия", energy, Energy)

            Spacer(Modifier.height(24.dp))

            // Кнопки действий (логика Дня 3: покормить −15 ₡, поиграть −10 ₡)
            Row(Modifier.fillMaxWidth()) {
                BigActionButton("🍖 Покормить", Hunger, Modifier.weight(1f)) { vm.feed() }
                Spacer(Modifier.width(8.dp))
                BigActionButton("🎾 Поиграть", Mood, Modifier.weight(1f)) { vm.play() }
                Spacer(Modifier.width(8.dp))
                BigActionButton("😴 Спать", Energy, Modifier.weight(1f)) { vm.rest() }
            }
        }
    }
}

@Composable
private fun BottomNav(
    onOpenBanks: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenKiosk: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenReport: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠") }, label = { Text("Дом") })
        NavigationBarItem(selected = false, onClick = onOpenBanks, icon = { Text("🏦") }, label = { Text("Банки") })
        NavigationBarItem(selected = false, onClick = onOpenMarket, icon = { Text("🏪") }, label = { Text("Магазин") })
        NavigationBarItem(selected = false, onClick = onOpenKiosk, icon = { Text("🍋") }, label = { Text("Ларёк") })
        NavigationBarItem(selected = false, onClick = onOpenQuests, icon = { Text("📋") }, label = { Text("Задания") })
        NavigationBarItem(selected = false, onClick = onOpenReport, icon = { Text("📊") }, label = { Text("Отчёт") })
    }
}