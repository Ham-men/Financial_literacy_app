package com.example.financialliteracyapp.ui.screens.kiosk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.launch

/** Кабинет найма (v5): слева визуал «стол+стул+работник», справа кнопки и учёт магазина. */
@Composable
fun HireScreen(
    onBack: () -> Unit,
    buildingId: Long = 0L,
    buildingName: String = "магазин"
) {
    val context = LocalContext.current
    val repo = remember { com.example.financialliteracyapp.data.AppContainer.repo(context) }
    val prefs = remember { com.example.financialliteracyapp.data.AppContainer.prefs(context) }
    val scope = rememberCoroutineScope()

    val hiredBuildings by prefs.hiredBuildings.collectAsState(initial = emptySet())
    val hired = hiredBuildings.contains(buildingId)
    val building by repo.observeBuildingById(buildingId).collectAsState(initial = null)
    val shopCash = building?.cash ?: 0
    val stock = building?.stock ?: 0

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Заголовок
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "🏢 Найм сотрудника • $buildingName",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(6.dp))

        // Два блока: слева визуал, справа управление и учёт
        Row(Modifier.fillMaxSize()) {
            // Левый блок: стол, стул, работник
            Box(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(Color(0xFFE8E2D4), RoundedCornerShape(14.dp))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .align(Alignment.TopCenter)
                        .background(Color(0xFFDCD3C0), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                )
                Surface(
                    Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Column(
                        Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💼 Вакансия: работник", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Зарплата ${Balance.CASHIER_SALARY} ₡ в день", fontSize = 10.sp, color = TextSecondary)
                    }
                }
                // Стол
                Box(
                    Modifier
                        .fillMaxWidth(0.72f)
                        .height(34.dp)
                        .align(Alignment.Center)
                        .background(Color(0xFF6D4C41), RoundedCornerShape(6.dp))
                )
                if (hired) {
                    // Нанятый сотрудник сидит за столом
                    Text(
                        "🧑‍💼",
                        fontSize = 34.sp,
                        modifier = Modifier.align(Alignment.Center).offset(x = 0.dp, y = (-34).dp)
                    )
                    Text(
                        "✅ работает",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
                    )
                } else {
                    // Пустое место / кандидат
                    Text(
                        "🪑",
                        fontSize = 28.sp,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp)
                    )
                    Text(
                        "🧑‍💼",
                        fontSize = 30.sp,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp)
                    )
                    Text(
                        "ждёт собеседования",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF795548),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Правый блок: кнопки и учёт магазина — компактно под смартфон
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Учёт магазина
                Surface(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp
                ) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        Text(
                            if (hired) "👤 Сотрудник работает в «$buildingName»" else "👤 Свободное место в «$buildingName»",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📦 Товар: $stock шт", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("💰 Касса: $shopCash ₡", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (hired)
                                "Работник сам продаёт товар ботам, деньги копятся в кассе магазина."
                            else
                                "Нанятый сотрудник продаёт товар ботам: товар уходит со склада, деньги копятся в кассе.",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Кнопки
                if (hired) {
                    Button(
                        onClick = { scope.launch { prefs.setBuildingHired(buildingId, false) } },
                        Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger, contentColor = Color.White)
                    ) {
                        Text("❌ Уволить сотрудника", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { scope.launch { prefs.setBuildingHired(buildingId, true) } },
                        Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                    ) {
                        Text("👤 Нанять (${Balance.CASHIER_SALARY} ₡/день)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            repo.withdrawBuildingCash(buildingId)
                        }
                    },
                    Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = shopCash > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (shopCash > 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (shopCash > 0) Color.White else TextSecondary
                    )
                ) {
                    Text("💰 Забрать деньги — $shopCash ₡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Выручка копится в кассе этого магазина и НЕ попадает в мешок сама. «Забрать деньги» переведёт её в «💼 Мешок» (сцена ПЛАН).",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                )
            }
        }
    }
}