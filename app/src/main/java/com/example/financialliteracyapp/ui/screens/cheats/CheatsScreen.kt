package com.example.financialliteracyapp.ui.screens.cheats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

/** Читы для теста: деньги в банки, дата и время, повтор обучения. */
@Composable
fun CheatsScreen(
    onRestartTutorial: () -> Unit,
    embedded: Boolean = false
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: CheatsViewModel = viewModel(factory = CheatsViewModel.factory(repo, prefs))

    val wallet by vm.wallet.collectAsState()
    val day by vm.day.collectAsState()
    val gameMinute by vm.gameMinute.collectAsState()
    val message by vm.message.collectAsState()

    var needText by remember { mutableStateOf("") }
    var wantText by remember { mutableStateOf("") }
    var saveText by remember { mutableStateOf("") }
    var dayText by remember { mutableStateOf(day.toString()) }
    var timeText by remember { mutableStateOf(GameRules.timeLabel(gameMinute)) }

    // embedded=true: часть скролла родителя — не делаем собственный скролл/фон.
    Column(
        modifier = if (embedded) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        }.padding(16.dp)
    ) {
        if (!embedded) {
            Text("🎁 Читы — для теста",
                style = MaterialTheme.typography.headlineMedium)
            Text("Добавляй деньги в банки и меняй дату/время, чтобы проверить функционал",
                fontSize = 13.sp, color = TextSecondary)

            Spacer(Modifier.height(16.dp))
        }

        // Текущее состояние
        AppCard {
            Column {
                Text("Текущее состояние", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "День $day (${GameRules.dateForDay(day)}) · время ${GameRules.timeLabel(gameMinute)}",
                    fontSize = 13.sp, color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Карман: ${wallet?.needPlan ?: 0} ₡ · Желаемое: ${wallet?.wantPlan ?: 0} ₡ · Копилка: ${wallet?.savePlan ?: 0} ₡",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Деньги в банки
        Text("Добавить деньги в банки", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        BankCheatRow(
            label = "👖 Карман",
            color = Color(0xFF5B8DEF),
            value = needText,
            onValue = { needText = it },
            onAdd = { vm.cheatAddMoney("NEED", needText); needText = "" }
        )
        Spacer(Modifier.height(8.dp))
        BankCheatRow(
            label = "🚛 Желаемое",
            color = Color(0xFFF57C00),
            value = wantText,
            onValue = { wantText = it },
            onAdd = { vm.cheatAddMoney("WANT", wantText); wantText = "" }
        )
        Spacer(Modifier.height(8.dp))
        BankCheatRow(
            label = "🐷 Копилка",
            color = Color(0xFF7CB342),
            value = saveText,
            onValue = { saveText = it },
            onAdd = { vm.cheatAddMoney("SAVE", saveText); saveText = "" }
        )

        Spacer(Modifier.height(20.dp))

        // Дата и время
        Text("Установить дату и время", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        AppCard {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("День", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = dayText,
                            onValueChange = { dayText = it.filter { c -> c.isDigit() } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Время (ЧЧ:ММ)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = { timeText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                val minuteFromTime = parseTimeToMinute(timeText)
                BigActionButton(
                    text = if (minuteFromTime != null) "⏰ Установить: день $dayText, " +
                        GameRules.timeLabel(minuteFromTime) else "⏰ Установить дату и время",
                    color = Primary,
                    onClick = {
                        vm.cheatSetTime(dayText, minuteFromTime?.toString() ?: "")
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Например: день 3, время 14:30 → День 3, ${GameRules.timeLabel(870)}",
                    fontSize = 11.sp, color = TextSecondary
                )
            }
        }

        message?.let { msg ->
            Spacer(Modifier.height(12.dp))
            Text(
                msg,
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onRestartTutorial,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(min = 220.dp).height(46.dp)
        ) {
            Text("🎓 Повторить обучение", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BankCheatRow(
    label: String,
    color: Color,
    value: String,
    onValue: (String) -> Unit,
    onAdd: () -> Unit
) {
    AppCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { onValue(it.filter { c -> c.isDigit() }) },
                    singleLine = true,
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(72.dp)
            ) {
                Text("+ в банку", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

/** "HH:MM" -> минута дня (600 = 10:00). null при некорректном вводе. */
private fun parseTimeToMinute(text: String): Int? {
    val s = text.trim()
    val (h, m) = if (s.contains(":")) {
        val parts = s.split(":")
        if (parts.size != 2) return null
        parts[0].toIntOrNull() to parts[1].toIntOrNull()
    } else if (s.length == 3 || s.length == 4) {
        val h = s.dropLast(2).toIntOrNull()
        val m = s.takeLast(2).toIntOrNull()
        h to m
    } else {
        s.toIntOrNull() to 0
    } ?: return null
    if (h == null || m == null || h !in 0..23 || m !in 0..59) return null
    val minute = h * 60 + m
    if (minute > GameRules.BED_TIME_MINUTE) return GameRules.BED_TIME_MINUTE
    return minute
}