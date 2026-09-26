package com.example.financialliteracyapp.ui.screens.adult

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.screens.cheats.CheatsScreen
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun AdultScreen(onRestartTutorial: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: AdultViewModel = viewModel(factory = AdultViewModel.factory(repo, prefs))

    var accessGranted by remember { mutableStateOf(false) }

    if (!accessGranted) {
        AdultGate(onPass = { accessGranted = true })
        return
    }

    // После проверки — профиль ребёнка, демо/сброс и встроенные ЧИТЫ (один общий скролл).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        val pet by vm.pet.collectAsState()
        val wallet by vm.wallet.collectAsState()
        val goals by vm.goals.collectAsState()
        val quests by vm.quests.collectAsState()
        val periods by vm.periods.collectAsState()
        val day by vm.day.collectAsState()
        val demoMode by vm.demoMode.collectAsState()

        val activeGoal = goals.find { it.isActive } ?: goals.firstOrNull()
        val doneQuests = quests.count { it.completed }
        val successfulDays = periods.count { it.success }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("👨‍👩‍👧 Для взрослых",
                style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)

            Spacer(Modifier.height(14.dp))

            AppCard {
                Text("Профиль ребёнка", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                adultRow("👤 Питомец", pet?.let { "${it.name} — ${GameRules.growthStageName(it.level)} (ур. ${it.level})" } ?: "—")
                adultRow("📅 Игровой день", "$day (${GameRules.dateForDay(day)})")
                adultRow("💰 В мешке", "${wallet?.cash ?: 0} ₡")
                adultRow("🐷 В копилке", "${wallet?.savePlan ?: 0} ₡")
                adultRow("🎯 Цель", activeGoal?.let { "${it.title} (${it.currentAmount}/${it.targetAmount} ₡)" } ?: "нет")
                adultRow("📋 Задания", "$doneQuests из ${quests.size}")
                adultRow("✅ Успешных дней", "$successfulDays")
                adultRow("🥗 Дней с заботой", pet?.let { "${it.successfulDays} (накоплено ${it.totalSaved} ₡)" } ?: "—")
            }

            Spacer(Modifier.height(16.dp))

            // Демо-режим
            AppCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (demoMode) "🧪 Демо-режим ВКЛ" else "🧪 Демо-режим ВЫКЛ",
                        Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                    Switch(
                        checked = demoMode,
                        onCheckedChange = { vm.setDemo(it) }
                    )
                }
                Text("Ускоренные часы, свободный сон и +${Balance.DEMO_DAILY_ALLOWANCE} ₡ в день.",
                    fontSize = 12.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            BigActionButton("🔄 Сбросить профиль", Danger, Modifier.fillMaxWidth()) { vm.resetProfile() }
            Spacer(Modifier.height(10.dp))
            Text("Все данные (питомец, банки, магазины, отчёты) будут удалены — игра начнётся заново.",
                fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)

            Spacer(Modifier.height(20.dp))

            // Встроенные ЧИТЫ (embedded=true — без собственного скролла, скроллится вместе с этим экраном)
            CheatsScreen(onRestartTutorial = onRestartTutorial, embedded = true)

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Арифметическая проверка «это взрослый»: без неё в настройки/читы не попасть. */
@Composable
private fun AdultGate(onPass: () -> Unit) {
    var mathAnswer by remember { mutableStateOf("") }
    val correctAnswer = 12 - 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("👨‍👩‍👧 Для взрослых",
            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text("Здесь настройки профиля и тестовые читы",
            fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = mathAnswer,
            onValueChange = { mathAnswer = it.filter { c -> c.isDigit() }.take(4) },
            label = { Text("Введите ответ: 12 − 5 = ?") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().width(220.dp),
            singleLine = true
        )
        Spacer(Modifier.height(14.dp))
        BigActionButton("Проверить", Primary, Modifier.fillMaxWidth()) {
            if (mathAnswer.trim() == correctAnswer.toString()) onPass()
        }
        Spacer(Modifier.height(10.dp))
        Text("Защита от детей: чтобы менять настройки и читы, реши пример.",
            fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun adultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(1f), fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}