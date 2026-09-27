package com.example.financialliteracyapp.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.R
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

private val bodyIcons = listOf( // 0=рыжий, 1=серый, 2=розовый
    R.drawable.ic_pet_raccoon,
    R.drawable.ic_pet_raccoon_gray,
    R.drawable.ic_pet_raccoon_pink
)
private val bodyNames = listOf("Рыжий", "Серый", "Розовый")
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Как ты будешь делить деньги?",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text("Выбери одну банку — твой приоритет на старте",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))

        decisions.forEach { decision ->
            DecisionCard(
                decision = decision,
                selected = selectedDecisionLocal == decision.id,
                onClick = { onDecisionSelected(decision.id); selectedDecisionLocal = decision.id }
            )
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(8.dp))
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
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) decision.color.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected)
            androidx.compose.foundation.BorderStroke(2.dp, decision.color) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(decision.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Text(decision.title,
                Modifier.weight(1f),
                fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (selected) {
                androidx.compose.material3.Text(text = "✓", color = decision.color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        Text(decision.desc,
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 7.dp))
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
        verticalArrangement = Arrangement.Center
    ) {
        Text("Создай своего Финни",
            fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(2.dp))
        Text("Тело × Аксессуар × Фон = 9 уникальных комбинаций",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))

        // Preview слева + селекторы справа — чтобы влезало в landscape
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(150.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(backgrounds[selectedBackground], fontSize = 44.sp)
                    Spacer(Modifier.height(-12.dp))
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(bodyIcons[selectedBody]),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                        if (accessories[selectedAccessory].isNotBlank()) {
                            Text(accessories[selectedAccessory], fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Селекторы
            Column(Modifier.weight(1f)) {
                SelectorRow("Тело", bodyNames, selectedBody, onBodyChange) { index, isSel ->
                    Image(
                        painterResource(bodyIcons[index]),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                SelectorRow("Аксессуар", accessories.map { if (it.isBlank()) "нет" else it }, selectedAccessory, onAccessoryChange) { i, isSel ->
                    if (accessories[i].isBlank()) Text("➖", fontSize = 14.sp) else Text(accessories[i], fontSize = 14.sp)
                }
                Spacer(Modifier.height(6.dp))
                SelectorRow("Фон", backgrounds, selectedBackground, onBackgroundChange) { i, isSel ->
                    Text(backgrounds[i], fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        BigActionButton(text = "Далее →", color = Primary, modifier = Modifier.fillMaxWidth()) { onNext() }
    }
}

@Composable
private fun SelectorRow(
    label: String,
    options: List<String>,
    selected: Int,
    onChange: (Int) -> Unit,
    content: @Composable (Int, Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { index, _ ->
                val isSelected = index == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Primary)
                            else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onChange(index) },
                    contentAlignment = Alignment.Center
                ) {
                    content(index, isSelected)
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
            fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text("Ник для профиля (не ФИО, не телефон)",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("Твой ник") },
            modifier = Modifier.fillMaxWidth().width(300.dp),
            singleLine = true
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = finniName,
            onValueChange = onFinniNameChange,
            label = { Text("Имя питомца") },
            modifier = Modifier.fillMaxWidth().width(300.dp),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))
        BigActionButton(text = "Начать игру!", color = Primary, modifier = Modifier.fillMaxWidth()) { onFinish() }
    }
}