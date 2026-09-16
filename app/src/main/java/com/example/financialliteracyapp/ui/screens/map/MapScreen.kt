package com.example.financialliteracyapp.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onOpenKiosk: () -> Unit,
    onOpenMarket: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenLot: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: MapViewModel = viewModel(factory = MapViewModel.factory(repo))
    val walletState by vm.wallet.collectAsState()
    val selectedDistrict by vm.selectedDistrict.collectAsState()
    val lotPurchased by prefs.lotPurchased.collectAsState(initial = false)

    val cash = walletState?.cash ?: 500

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Finni + balance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🦝", fontSize = 32.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Копилкино", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Район: Рынок", fontSize = 11.sp, color = Color(0xFFBBDEFB))
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4E342E),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).width(80.dp).height(32.dp)
            ) {
                Text(
                    "💰 $cash ₡",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF8F0),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
                )
            }
        }

        // Simplified Map - clickable sectors using Cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Row 1: Богатый | Центр
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SimpleDistrictCard(
                        name = "Богатый",
                        color = Color(0xFFF9A825),
                        textColor = Color(0xFF4E342E),
                        icon = "💎",
                        unlocked = true,
                        modifier = Modifier.weight(1f)
                    )
                    SimpleDistrictCard(
                        name = "Центр",
                        color = Color(0xFF8E24AA),
                        textColor = Color.White,
                        icon = "🏙️",
                        unlocked = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Рынок | Спальный
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).clickable { onOpenLot() }) {
                        SimpleDistrictCard(
                            name = "Рынок",
                            color = Color(0xFFFB8C00),
                            textColor = Color.White,
                            icon = if (lotPurchased) "🍋" else "🏗️",
                            unlocked = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    SimpleDistrictCard(
                        name = "Спальный",
                        color = Color(0xFF43A047),
                        textColor = Color.White,
                        icon = "🏠",
                        unlocked = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Промзона | Техно-парк
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SimpleDistrictCard(
                        name = "Промзона",
                        color = Color(0xFF607D8B),
                        textColor = Color.White,
                        icon = "🏭",
                        unlocked = false,
                        modifier = Modifier.weight(1f)
                    )
                    SimpleDistrictCard(
                        name = "Техно-парк",
                        color = Color(0xFF0288D1),
                        textColor = Color.White,
                        icon = "💻",
                        unlocked = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Legend
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFE8F5E9))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(14.dp).background(Color(0xFFF9E79F), CircleShape))
                    Text("Богатые районы", fontSize = 11.sp, color = Color(0xFF4E342E))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(14.dp).background(Color(0xFFB0BEC5), CircleShape))
                    Text("Бедные районы", fontSize = 11.sp, color = Color(0xFF4E342E))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(18.dp).background(Color(0xFF4E342E), CircleShape))
                    Text("Участок для продажи", fontSize = 11.sp, color = Color(0xFF4E342E))
                }
            }
        }

        // Bottom navigation
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠") }, label = { Text("Дом") })
            NavigationBarItem(selected = false, onClick = onOpenKiosk, icon = { Text("🍋") }, label = { Text("Ларёк") })
            NavigationBarItem(selected = false, onClick = onOpenMarket, icon = { Text("🏪") }, label = { Text("Магазин") })
            NavigationBarItem(selected = false, onClick = onOpenGoals, icon = { Text("🐷") }, label = { Text("Копилка") })
            NavigationBarItem(selected = false, onClick = onOpenQuests, icon = { Text("📋") }, label = { Text("Задания") })
            NavigationBarItem(selected = false, onClick = onOpenReport, icon = { Text("📊") }, label = { Text("Отчёт") })
        }
    }
}

@Composable
fun SimpleDistrictCard(
    name: String,
    color: Color,
    textColor: Color,
    icon: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = if (unlocked) 1f else 0.4f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text("🔒", fontSize = 48.sp)
            if (!unlocked) {
                Text("Скоро", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.7f))
            }
        }
    }
}