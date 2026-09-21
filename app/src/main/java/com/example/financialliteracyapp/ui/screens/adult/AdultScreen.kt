package com.example.financialliteracyapp.ui.screens.adult

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun AdultScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AppContainer.prefs(context) }
    
    var showResetConfirm by remember { mutableStateOf(false) }
    var mathAnswer by remember { mutableStateOf("") }
    val correctAnswer = 12 - 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👨‍👩‍👧 Для взрослых",
            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text("Здесь можно посмотреть прогресс ребёнка и сбросить профиль",
            fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(24.dp))
        
        // Барьер: удержание 3с или пример
        if (!showResetConfirm) {
            OutlinedTextField(
                value = mathAnswer,
                onValueChange = { mathAnswer = it },
                label = { Text("Введите ответ: 12 − 5 = ?") },
                modifier = Modifier.fillMaxWidth().width(200.dp),
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
            )
            Spacer(Modifier.height(16.dp))
            BigActionButton("Проверить", Primary, Modifier.fillMaxWidth()) {
                if (mathAnswer.trim() == correctAnswer.toString()) {
                    showResetConfirm = true
                }
            }
        } else {
            Text("✅ Доступ разрешён", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(16.dp))
            
            // Информация о прогрессе
            Text("Информация о профиле ребёнка", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            
            // TODO: показать реальные данные из Room
            AppCard {
                Text("Имя Финни: Финни")
                Text("Уровень: 1 (Малыш)")
                Text("Дней подряд: 1")
                Text("Копилка: 0 ₡")
                Text("Текущая цель: Мячик (0/300)")
                Text("Выполнено заданий: 0/6")
            }
            
            Spacer(Modifier.height(24.dp))
            
            BigActionButton("🔄 Сбросить профиль", Danger, Modifier.fillMaxWidth()) {
                // TODO: сбросить профиль
            }
            Spacer(Modifier.height(12.dp))
            
            BigActionButton("🧪 Демо-режим: ВКЛ", PrimaryDark, Modifier.fillMaxWidth()) {
                // TODO: переключить демо-режим
            }
            Spacer(Modifier.height(12.dp))
            
            BigActionButton("← Назад", Primary, Modifier.fillMaxWidth()) { 
                showResetConfirm = false
                mathAnswer = ""
                onBack() 
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}