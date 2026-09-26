package com.example.financialliteracyapp.ui.screens.appearance

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.ui.theme.Primary
import com.example.financialliteracyapp.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val bodyTypes = listOf("🦝", "🦝", "🦝") // 0=рыжий, 1=серый, 2=пятнистый
private val bodyNames = listOf("Рыжик", "Серый", "Пятнистый")
private val accessories = listOf("", "🧣", "🧢") // 0=без, 1=шарф, 2=кепка
private val accessNames = listOf("Без", "Шарф", "Кепка")
private val backgrounds = listOf("🏪", "🌳", "🌊") // 0=рынок, 1=парк, 2=речка
private val bgNames = listOf("Рынок", "Парк", "Речка")

/** Сцена «Внешний вид»: редактирование тела, аксессуара и фона Финни с живым предпросмотром. */
@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()

    val currentPet by repo.observePet().collectAsState(initial = null)

    var body by remember { mutableIntStateOf(0) }
    var accessory by remember { mutableIntStateOf(0) }
    var background by remember { mutableIntStateOf(0) }

    // Инициализация из БД при открытии
    LaunchedEffect(currentPet) {
        val pet = currentPet
        if (pet != null) {
            body = pet.bodyType
            accessory = pet.accessory
            background = pet.background
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок компактный
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp)
            }
            Spacer(Modifier.width(6.dp))
            Text(
                "🎨 Внешний вид Финни",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview слева
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(112.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 7.dp)
                        .background(Color(0xFF5C3A21), RoundedCornerShape(50.dp)),
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(backgrounds[background], fontSize = 28.sp)
                    Spacer(Modifier.height(-7.dp))
                    Row {
                        Text(bodyTypes[body], fontSize = 22.sp)
                        if (accessories[accessory].isNotBlank()) {
                            Text(accessories[accessory], fontSize = 11.sp)
                        }
                    }
                    Text(
                        "${bodyNames[body]} · ${accessNames[accessory]} · ${bgNames[background]}",
                        fontSize = 8.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Селекторы справа
            Column(Modifier.weight(1f)) {
                SelectorRow("Тело", bodyNames, body, { body = it }) { text, sel ->
                    Text(text, fontSize = 8.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                }
                Spacer(Modifier.height(4.dp))
                SelectorRow("Аксессуар", accessNames, accessory, { accessory = it }) { text, sel ->
                    Text(text, fontSize = 8.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                }
                Spacer(Modifier.height(4.dp))
                SelectorRow("Фон", bgNames, background, { background = it }) { text, sel ->
                    Text(text, fontSize = 8.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Button(
            onClick = {
                scope.launch {
                    val base = currentPet ?: PetEntity()
                    repo.upsertPet(
                        base.copy(
                            bodyType = body,
                            accessory = accessory,
                            background = background
                        )
                    )
                    onBack()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7E57C2),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(9.dp),
            modifier = Modifier.fillMaxWidth().height(30.dp)
        ) {
            Text("💾 Сохранить внешний вид", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(3.dp))
        Text(
            "Изменения сразу видны на сцене ДОМ и в онбординге",
            fontSize = 7.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
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
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Primary)
                            else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            RoundedCornerShape(8.dp)
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