package com.example.financialliteracyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    modifier: Modifier = Modifier,
    bottomBar: Boolean = false
) {
    val navItems = remember {
        listOf(
            NavItem(Routes.MAIN, "🏠", "ДОМ", "Дом"),
            NavItem(Routes.BANKS, "💰", "ПЛАН", "План"),
            NavItem(Routes.MAP, "🗺️", "КАРТА", "Карта"),
            NavItem(Routes.ENTERTAINMENT, "🧸", "ИГРЫ", "Развлечения"),
            NavItem(Routes.GOALS, "🎯", "ЦЕЛИ", "Цели и задания"),
            NavItem(Routes.REPORT, "📈", "ОТЧЁТ", "Отчёт дня"),
            NavItem(Routes.REFERENCE, "📖", "СПРАВКА", "Справка"),
            NavItem(Routes.ADULT, "👨‍👩‍👧", "ВЗРОСЛЫЙ", "Для взрослых")
        )
    }

    if (bottomBar) {
        // Портрет: панель снизу экрана, плитки в один ряд с горизонтальным скроллом.
        Row(
            modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .border(width = 3.dp, color = Color(0xFF444444), shape = RoundedCornerShape(0.dp))
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            navItems.forEach { item ->
                NavTile(item, currentRoute, onNavigate, compact = true)
            }
        }
        return
    }

    // Ландшафт: панель слева, плитки в столбик.
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
            NavTile(item, currentRoute, onNavigate, compact = false)
        }
    }
}

@Composable
private fun NavTile(
    item: NavItem,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    compact: Boolean
) {
    val isActive = currentRoute.startsWith(item.route)
    Surface(
        modifier = if (compact) {
            Modifier.width(56.dp).height(52.dp)
        } else {
            Modifier.width(56.dp).height(56.dp).padding(vertical = 3.dp)
        },
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
            Text(item.icon, fontSize = if (compact) 21.sp else 24.sp)
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