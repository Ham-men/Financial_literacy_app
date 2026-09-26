package com.example.financialliteracyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.ui.navigation.Routes

data class NavItem(
    val route: String,
    val icon: String,
    val label: String,
    val contentDescription: String
)

@Composable
fun NavSidebar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = remember {
        listOf(
            NavItem(Routes.MAIN, "🏠", "ДОМ", "Дом"),
            NavItem(Routes.BANKS, "💰", "ПЛАН", "План"),
            NavItem(Routes.MAP, "🗺️", "КАРТА", "Карта"),
            NavItem(Routes.ENTERTAINMENT, "🧸", "ИГРЫ", "Развлечения"),
            NavItem(Routes.CHEATS, "🎁", "ЧИТЫ", "Читы")
        )
    }

    Column(
        modifier
            .width(70.dp)
            .fillMaxHeight()
            .background(Color(0xFF1A1A1A))
            .border(width = 4.dp, color = Color(0xFF444444), shape = RoundedCornerShape(0.dp))
            .padding(vertical = 6.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        navItems.forEach { item ->
            val isActive = currentRoute.startsWith(item.route)
            Surface(
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp)
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) Color(0xFF0D47A1) else Color(0xFF2A2A2A),
                border = androidx.compose.foundation.BorderStroke(
                    width = 3.dp,
                    color = if (isActive) Color(0xFF42A5F5) else Color(0xFF555555)
                ),
                onClick = { onNavigate(item.route) }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.icon, fontSize = 24.sp)
                    Text(
                        item.label,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.3.sp,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}