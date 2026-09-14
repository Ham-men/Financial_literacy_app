package com.example.financialliteracyapp.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun ReportScreen(onNextDay: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: ReportViewModel = viewModel(factory = ReportViewModel.factory(repo, prefs))
    val shop by vm.shop.collectAsState()
    val day by vm.day.collectAsState()

    val price = shop?.price ?: Balance.LEMONADE_BASE_PRICE
    val costPrice = shop?.costPrice ?: Balance.LEMONADE_COST
    val sold = shop?.soldToday ?: 0
    val revenue = shop?.revenueToday ?: (sold * price)
    val cost = sold * costPrice
    val rent = Balance.SHOP_RENT_MARKET
    val tax = (revenue * Balance.TAX_RATE).toInt()
    val profit = revenue - cost - rent - tax

    val visitors = 20 // MVP: 20 ботов
    val bought = sold
    val leftHighPrice = (visitors - bought).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("📊 Отчёт дня $day",
            style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        AppCard {
            Text("Итоги", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            ReportRow("Выручка:", "+$revenue ₡", Primary)
            ReportRow("Себестоимость:", "−$cost ₡", Danger)
            ReportRow("Аренда:", "−$rent ₡", Danger)
            ReportRow("Налог (13%):", "−$tax ₡", Danger)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            ReportRow("Чистая прибыль:", "${if (profit >= 0) "+" else ""}$profit ₡", if (profit >= 0) Primary else Danger, big = true)
        }

        Spacer(Modifier.height(12.dp))

        AppCard {
            Text("👥 Покупатели", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            ReportRow("Пришло:", "$visitors", TextPrimary)
            ReportRow("Купило:", "$bought", TextPrimary)
            ReportRow("Ушло (высокая цена):", "$leftHighPrice", Danger)
        }

        Spacer(Modifier.height(12.dp))

        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🦡", fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    if (profit > 0) "Молодец! Прибыль $profit ₡. Попробуй цену ${price - 1} ₡ — покупателей станет больше."
                    else "Убыток $profit ₡. Снизь цену или закупи дешевле у Сороки (2 ₡).",
                    fontSize = 14.sp, color = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        BigActionButton("▶ Следующий день", Primary,
            Modifier.fillMaxWidth(), onClick = { vm.nextDay(onNextDay) })

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color, big: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            Modifier.weight(1f),
            fontSize = if (big) 16.sp else 14.sp,
            fontWeight = if (big) FontWeight.Bold else FontWeight.Normal)
        Text(value,
            fontSize = if (big) 18.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor)
    }
}