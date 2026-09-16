package com.example.financialliteracyapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.prefs.UserPrefs
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.Primary
import kotlinx.coroutines.launch

private data class OnboardingDecision(
    val id: String,
    val title: String,
    val desc: String,
    val emoji: String,
    val color: Color
)

private val decisions = listOf(
    OnboardingDecision("need", "Нужное", "Без этого Финни грустит: корм, вода, уход, подстилка", "🍖", Color(0xFF4CAF50)),
    OnboardingDecision("want", "Желаемое", "Приятно, но можно подождать: игрушки, бантики, красота", "🎀", Color(0xFF2196F3)),
    OnboardingDecision("save", "Копилка", "На цель: мячик, палатка, набор художника", "🐷", Color(0xFFFFC107))
)

private val bodyTypes = listOf("🦝", "🦝", "🦝") // 0=рыжий, 1=серый, 2=пятнистый
private val accessories = listOf("", "🧣", "🧢") // 0=без, 1=шарф, 2=кепка
private val backgrounds = listOf("🏪", "🌳", "🌊") // 0=рынок, 1=парк, 2=речка

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { AppContainer.prefs(context) }
    val repo = remember { AppContainer.repo(context) }
    
    var currentStep by remember { mutableIntStateOf(0) } // 0=decisions, 1=customize, 2=name
    var selectedDecision by remember { mutableStateOf<String?>(null) }
    var selectedBody by remember { mutableIntStateOf(0) }
    var selectedAccessory by remember { mutableIntStateOf(0) }
    var selectedBackground by remember { mutableIntStateOf(0) }
    var nickname by remember { mutableStateOf("") }
    var finniName by remember { mutableStateOf("Финни") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentStep) {
            0 -> DecisionStep(
                onDecisionSelected = { decisionId ->
                    selectedDecision = decisionId
                },
                onNext = {
                    if (selectedDecision != null) currentStep = 1
                }
            )
            1 -> CustomizeStep(
                selectedBody = selectedBody,
                onBodyChange = { selectedBody = it },
                selectedAccessory = selectedAccessory,
                onAccessoryChange = { selectedAccessory = it },
                selectedBackground = selectedBackground,
                onBackgroundChange = { selectedBackground = it },
                onNext = { currentStep = 2 }
            )
            2 -> NameStep(
                nickname = nickname,
                onNicknameChange = { nickname = it },
                finniName = finniName,
                onFinniNameChange = { finniName = it },
                onFinish = {
                    // Сохраняем профиль
                    scope.launch {
                        prefs.setPetName(finniName)
                        prefs.setOnboardingDone(true)
                        val pet = PetEntity(
                            name = finniName,
                            bodyType = selectedBody,
                            accessory = selectedAccessory,
                            background = selectedBackground
                        )
                        repo.upsertPet(pet)
                    }
                    onFinish()
                }
            )
        }
    }
}

@Composable
private fun DecisionStep(
    onDecisionSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    var selectedDecisionLocal by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Как ты будешь делить деньги?",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Выбери одну банку — это твой приоритет на старте",
            fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        decisions.forEach { decision ->
            DecisionCard(
                decision = decision,
                selected = selectedDecisionLocal == decision.id,
                onClick = { onDecisionSelected(decision.id); selectedDecisionLocal = decision.id }
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(32.dp))
        BigActionButton(
            text = "Далее →",
            color = Primary,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDecisionLocal != null
        ) { onNext() }
    }
}

@Composable
private fun DecisionCard(
    decision: OnboardingDecision,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) decision.color.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected)
            androidx.compose.foundation.BorderStroke(2.dp, decision.color) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(decision.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Text(decision.title,
                    Modifier.weight(1f),
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (selected) {
                    androidx.compose.material3.Text(text = "✓", color = decision.color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(decision.desc,
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun CustomizeStep(
    selectedBody: Int,
    onBodyChange: (Int) -> Unit,
    selectedAccessory: Int,
    onAccessoryChange: (Int) -> Unit,
    selectedBackground: Int,
    onBackgroundChange: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Создай своего Финни",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Тело × Аксессуар × Фон = 9 уникальных комбинаций",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        // Preview
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(backgrounds[selectedBackground], fontSize = 120.sp)
                Spacer(Modifier.height(-40.dp))
                Row(horizontalArrangement = Arrangement.Center) {
                    Text(bodyTypes[selectedBody], fontSize = 80.sp)
                    if (accessories[selectedAccessory].isNotBlank()) {
                        Text(accessories[selectedAccessory], fontSize = 40.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Body selector
        SelectorRow("Тело", bodyTypes, selectedBody, onBodyChange) { text, sel ->
            Text(text, fontSize = 48.sp)
        }
        Spacer(Modifier.height(16.dp))
        SelectorRow("Аксессуар", accessories.map { if (it.isBlank()) "нет" else it }, selectedAccessory, onAccessoryChange) { text, sel ->
            if (text == "нет") Text("➖", fontSize = 32.sp) else Text(text, fontSize = 32.sp)
        }
        Spacer(Modifier.height(16.dp))
        SelectorRow("Фон", backgrounds, selectedBackground, onBackgroundChange) { text, sel ->
            Text(text, fontSize = 32.sp)
        }

        Spacer(Modifier.height(32.dp))
        BigActionButton(text = "Далее →", color = Primary, modifier = Modifier.fillMaxWidth()) { onNext() }
    }
}

@Composable
private fun SelectorRow(
    label: String,
    options: List<String>,
    selected: Int,
    onChange: (Int) -> Unit,
    content: @Composable (String, Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) Primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Primary)
                            else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onChange(index) },
                    contentAlignment = Alignment.Center
                ) {
                    content(option, isSelected)
                }
            }
        }
    }
}

@Composable
private fun NameStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    finniName: String,
    onFinniNameChange: (String) -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Как к тебе обращаться?",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Ник для профиля (не ФИО, не телефон)",
            fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("Твой ник") },
            modifier = Modifier.fillMaxWidth().width(300.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Text("Имя Финни:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = finniName,
            onValueChange = onFinniNameChange,
            label = { Text("Имя питомца") },
            modifier = Modifier.fillMaxWidth().width(300.dp),
            singleLine = true
        )

        Spacer(Modifier.height(32.dp))
        BigActionButton(text = "Начать игру!", color = Primary, modifier = Modifier.fillMaxWidth()) { onFinish() }
    }
}