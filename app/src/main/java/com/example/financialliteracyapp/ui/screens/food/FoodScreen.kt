package com.example.financialliteracyapp.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.theme.*
import com.example.financialliteracyapp.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun FoodScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }

    val pet by repo.observePet().collectAsState(initial = null)
    val wallet by repo.observeWallet().collectAsState(initial = null)

    val hunger = pet?.hunger ?: 70
    val mood = pet?.mood ?: 80
    val energy = pet?.energy ?: 90
    val needPlan = wallet?.needPlan ?: 0

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "Дата 01.01.2020 12:00",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("нужное", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("$needPlan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Scene: cat top-left, fridge top-right, medkit middle-right, text center
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Cat top-left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp)
            ) {
                Text("🐱", fontSize = 48.sp)
            }

            // Fridge top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp)
            ) {
                Text("🧊", fontSize = 56.sp)
            }

            // Medkit middle-right
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Text("🧰", fontSize = 52.sp)
            }

            // Center text
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "команда для еды и лечения.\nденьги тратятся из банки нужное",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        // Action buttons at bottom center
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    scope.launch {
                        // Feed action - spend 20 from need
                        // TODO: implement feed logic
                    }
                },
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🍖 Покормить — 20₡", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    scope.launch {
                        // Heal action - spend 30 from need
                        // TODO: implement heal logic
                    }
                },
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("💊 Лечение — 30₡", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}