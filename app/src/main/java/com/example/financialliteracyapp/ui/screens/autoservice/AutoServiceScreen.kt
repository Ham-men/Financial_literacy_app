package com.example.financialliteracyapp.ui.screens.autoservice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.theme.*

/**
 * Сцена «СТО» — ремонт машин.
 * Соответствует СТО.html: подъёмники, машины, инструменты, работники.
 */
@Composable
fun AutoServiceScreen(
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val wallet by repo.observeWallet().collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Шапка
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Дата 01.01.2020 12:00", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "нужное \\ желаемое \\ копилка\n${wallet?.needPlan ?: 100} \\ ${wallet?.wantPlan ?: 200} \\ ${wallet?.savePlan ?: 200}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Дверь
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset(y = (-5).dp)
                .width(100.dp)
                .height(22.dp)
                .background(Color.White, RoundedCornerShape(2.dp))
                .border(BorderStroke(2.dp, Color(0xFFCCCCCC)), RoundedCornerShape(2.dp))
        )

        // Основная сцена
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Верхний подъёмник
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, 12) }
                    .align(Alignment.TopEnd)
                    .width(110.dp)
                    .height(110.dp)
            ) {
                // Подъёмник (синие стойки)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(BorderStroke(10.dp, Color(0xFF007BB5)), RoundedCornerShape(2.dp))
                )
                // Машина
                Text("🚙", fontSize = 36.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-38).dp))
                // Инструменты
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-35).dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔧", fontSize = 18.sp)
                    Text("🔨", fontSize = 18.sp)
                }
            }

            // Верхний работник
            Text(
                "👨‍🔧",
                fontSize = 36.sp,
                modifier = Modifier
                    .offset { IntOffset(6, 14) }
                    .align(Alignment.TopEnd)
            )

            // Метка "нанять работника"
            Text(
                "нанять работника",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.offset { IntOffset(18, 24) }
            )

            // Нижний подъёмник
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, 50) }
                    .align(Alignment.TopEnd)
                    .width(110.dp)
                    .height(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(BorderStroke(10.dp, Color(0xFF007BB5)), RoundedCornerShape(2.dp))
                )
                Text("🚙", fontSize = 36.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-38).dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-35).dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🔧", fontSize = 18.sp)
                    Text("🔨", fontSize = 18.sp)
                }
            }

            // Нижний работник
            Text(
                "👨‍🔧",
                fontSize = 36.sp,
                modifier = Modifier
                    .offset { IntOffset(6, 53) }
                    .align(Alignment.TopEnd)
            )

            // Метка "нанять работника"
            Text(
                "нанять работника",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.offset { IntOffset(20, 63) }
            )

            // Купить товар (левый низ)
            Column(
                modifier = Modifier
                    .offset { IntOffset(5, 0) }
                    .align(Alignment.BottomStart)
                    .padding(bottom = 10.dp)
            ) {
                Text("купить товар", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("📋", fontSize = 28.sp)
                    Text("📦", fontSize = 28.sp)
                }
            }

            // Кнопка найма
            Button(
                onClick = { /* Найм механика */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 12.dp)
                    .height(32.dp)
            ) {
                Text("👤 Нанять механика", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
