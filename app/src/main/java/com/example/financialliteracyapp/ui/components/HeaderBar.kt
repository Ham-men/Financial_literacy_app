package com.example.financialliteracyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.data.prefs.UserPrefs
import com.example.financialliteracyapp.ui.navigation.Routes

/**
 * Левая навигационная панель — как в HTML-макетах.
 * 7 кнопок: Дом, План, Еда, Карта, Ларёк, Стройка, СТО.
 * Скроллится если экран узкий.
 */
@Composable
fun HeaderBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }

    val wallet by repo.observeWallet().collectAsState(initial = null)
    val currentDay by prefs.currentDay.collectAsState(initial = 1)
    val petName by prefs.petName.collectAsState(initial = "Финни")

    val date = remember(currentDay) {
        val base = java.time.LocalDate.of(2020, 1, 1)
        base.plusDays(currentDay - 1L).format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    // Навигационные кнопки — как в HTML
    val navItems = listOf(
        NavItem(Routes.MAIN, "🏠", "ДОМ"),
        NavItem(Routes.BANKS, "💰", "ПЛАН"),
        NavItem(Routes.FOOD, "🍕", "ЕДА"),
        NavItem(Routes.MAP, "🗺️", "КАРТА"),
        NavItem(Routes.KIOSK, "🛒", "ЛАРЁК"),
        NavItem(Routes.CONSTRUCTION, "🔧", "СТРОЙКА"),
        NavItem(Routes.AUTO_SERVICE, "🚗", "СТО")
    )

    Column(
        modifier
            .fillMaxHeight()
            .width(70.dp)
            .background(Color(0xFF1A1A1A))
            .padding(vertical = 6.dp)
    ) {
        // Дата и имя питомца сверху
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                date,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                petName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            // Три банки компактно
            val need = wallet?.needPlan ?: 0
            val want = wallet?.wantPlan ?: 0
            val save = wallet?.savePlan ?: 0
            Text("🍚$need", fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
            Text("🧸$want", fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
            Text("🐷$save", fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
        }

        Spacer(Modifier.height(8.dp))

        // Навигационные кнопки со скроллом
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            navItems.forEach { item ->
                val isActive = currentRoute == item.route
                Surface(
                    onClick = { onNavigate(item.route) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) Color(0xFF0D47A1) else Color(0xFF2A2A2A),
                    border = if (isActive) BorderStroke(2.dp, Color(0xFF42A5F5)) else BorderStroke(2.dp, Color(0xFF555555)),
                    modifier = Modifier
                        .width(56.dp)
                        .height(56.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(item.emoji, fontSize = 16.sp)
                        Text(item.label, fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

private data class NavItem(val route: String, val emoji: String, val label: String)
