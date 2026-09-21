package com.example.financialliteracyapp.ui.screens.construction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke

/**
 * Сцена «Стройматериалы» — магазин инструментов и стройматериалов.
 * Соответствует стройматериалы.html: касса, закупка, полки с инструментами.
 */
@Composable
fun ConstructionScreen(
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
            // Касса (левый верх)
            Column(
                modifier = Modifier
                    .offset { IntOffset(6, 12) }
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧾", fontSize = 36.sp)
                Text("касса", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("нанять кассира", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Купить товар (левый низ)
            Column(
                modifier = Modifier
                    .offset { IntOffset(6, 0) }
                    .align(Alignment.BottomStart)
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📋📦", fontSize = 30.sp)
                Text("купить товар", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Верхняя полка с инструментами (правый верх)
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, 14) }
                    .align(Alignment.TopEnd)
                    .width(110.dp)
                    .height(130.dp)
            ) {
                // Покупатель слева от полки
                Text(
                    "🧑",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = (-30).dp)
                )
                // Полка справа
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(70.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFA07A55), RoundedCornerShape(2.dp))
                        .border(BorderStroke(2.dp, Color(0xFF7A5A3A)), RoundedCornerShape(2.dp))
                ) {
                    // Инструменты внутри полки
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔧", fontSize = 18.sp)
                            Text("🪛", fontSize = 18.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🗜️", fontSize = 18.sp)
                            Text("🔨", fontSize = 18.sp)
                        }
                    }
                }
            }

            // Нижняя полка с инструментами (правый низ)
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, 45) }
                    .align(Alignment.TopEnd)
                    .width(110.dp)
                    .height(130.dp)
            ) {
                // Покупатель слева от полки
                Text(
                    "🧑",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = (-30).dp)
                )
                // Полка справа
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(70.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFA07A55), RoundedCornerShape(2.dp))
                        .border(BorderStroke(2.dp, Color(0xFF7A5A3A)), RoundedCornerShape(2.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔧", fontSize = 18.sp)
                            Text("🪛", fontSize = 18.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🗜️", fontSize = 18.sp)
                            Text("🔨", fontSize = 18.sp)
                        }
                    }
                }
            }

            // Кнопка найма
            Button(
                onClick = { /* Найм кладовщика */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 12.dp)
                    .height(32.dp)
            ) {
                Text("👤 Нанять кладовщика", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
