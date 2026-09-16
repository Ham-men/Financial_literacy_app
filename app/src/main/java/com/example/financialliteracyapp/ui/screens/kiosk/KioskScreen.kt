package com.example.financialliteracyapp.ui.screens.kiosk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.screens.minigames.CashierGame
import com.example.financialliteracyapp.ui.screens.minigames.PricerGame
import com.example.financialliteracyapp.ui.screens.minigames.SuppliersGame
import com.example.financialliteracyapp.ui.screens.shop.AccountingScreen
import com.example.financialliteracyapp.ui.screens.shop.MarketScreen
import com.example.financialliteracyapp.ui.screens.shop.ShopViewModel
import com.example.financialliteracyapp.ui.theme.*

/** Сцена ларька (разбор.txt) — хаб: комната-сцена + вкладки Витрина/Закупка/Цена/Касса/Учёт/Найм. */
@Composable
fun KioskScreen(
    onBack: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenShelves: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val shop by vm.shop.collectAsState()
    val wallet by vm.wallet.collectAsState()
    val cashierHired by prefs.cashierHired.collectAsState(initial = false)

    val cash = wallet?.cash ?: 500
    val stock = shop?.stock ?: 0
    val priceInt = shop?.price ?: 8

    var tab by remember { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Заголовок
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "🍋 Ларёк с лимонадом",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
        }

        // Подвкладки ларька
        TabRow(selectedTabIndex = tab) {
            listOf(
                "🏬" to "Ларёк",
                "🛍" to "Витрина",
                "📦" to "Закупка",
                "🏷" to "Цена",
                "🧾" to "Касса",
                "📊" to "Учёт",
                "👩" to "Найм"
            ).forEachIndexed { i, (emoji, label) ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 14.sp)
                            Text(label, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                )
            }
        }

        when (tab) {
            // --- Комната ларька (вид сбоку) ---
            0 -> Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFFF2E3C6))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .align(Alignment.TopCenter)
                            .background(Color(0xFFFFE0B2))
                    )
                    Box(
                        Modifier
                            .width(120.dp)
                            .height(80.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 26.dp)
                            .background(Color(0xFFB3E5FC), RoundedCornerShape(10.dp))
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                            .width(80.dp)
                            .height(130.dp)
                            .clickable { tab = 2 }
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                                .align(Alignment.TopCenter)
                                .background(Color(0xFF795548), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        ) {
                            Text("🚪", Modifier.align(Alignment.Center), fontSize = 40.sp)
                        }
                        Column(
                            Modifier.align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📦", fontSize = 24.sp)
                            HotspotTag("Закупить товар", enabled = true)
                        }
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 130.dp)
                            .clickable(enabled = stock > 0) { onOpenShelves() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            Modifier
                                .width(110.dp)
                                .height(88.dp)
                                .background(Color(0xFF4E342E), RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (stock > 0) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("🧃"); Text("🧃"); Text("🧃") }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("🧃"); Text("🧃"); Text("🧃") }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("🧃"); Text("🧃"); Text("🧃") }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("▫️"); Text("▫️"); Text("▫️") }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("▫️"); Text("▫️"); Text("▫️") }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text("▫️"); Text("▫️"); Text("▫️") }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        HotspotTag(
                            if (stock > 0) "Полки: расставить" else "Полки пусты: закупись",
                            enabled = stock > 0
                        )
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 100.dp, bottom = 38.dp)
                            .clickable { tab = 6 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (cashierHired) "👩" else "🪑", fontSize = 30.sp)
                        HotspotTag(if (cashierHired) "Мила · касса" else "Нанять кассира", enabled = true)
                    }
                    Text(
                        "🦝",
                        fontSize = 46.sp,
                        modifier = Modifier.align(Alignment.Center).padding(bottom = 90.dp)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF8D6E63))
                    )
                    Column(
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = 44.dp)
                            .clickable { tab = 3 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏷️", fontSize = 30.sp)
                        HotspotTag("Ценник", enabled = true)
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 38.dp)
                            .clickable { tab = 4 },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (cashierHired) "👩‍💻" else "🧾", fontSize = 30.sp)
                        HotspotTag(
                            if (cashierHired) "Касса · Мила" else "Касса",
                            enabled = true
                        )
                    }
                }

                Text(
                    if (stock > 0)
                        "Полки с товаром. Продажи — во вкладке «Касса» 🧾, цену меняй в «Цена» 🏷️." +
                        (if (cashierHired) "\nМила-кассир 👩 на кассе: работает быстрее." else "\nСтул у кассы пустует — вкладка «Найм» 👩.")
                    else
                        "Полки пусты. «Закупка» 📦 — купи лимонад, потом расставь его на полки." +
                        (if (cashierHired) "\nМила-кассир 👩 уже на месте, но товара нет." else "\nСтул у кассы пустует — вкладка «Найм» 👩."),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
                )

                AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("📦 Склад и цена", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("Товара в наличии", Modifier.weight(1f))
                        Text("$stock шт.", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Text("Себест.: ${shop?.costPrice ?: 3} ₡", color = TextSecondary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text("Цена за единицу", Modifier.weight(1f))
                        Text("$priceInt ₡", fontWeight = FontWeight.Bold, color = Accent)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Сменить цену — подвкладка «Цена» 🏷️.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
            // --- Витрина покупок (бывший Магазин) ---
            1 -> MarketScreen(onBack = {}, embedded = true)
            // --- Закупка у поставщика ---
            2 -> SuppliersGame(onFinish = { tab = 0 }, embedded = true)
            // --- Поставь цену ---
            3 -> PricerGame(onFinish = { tab = 0 }, embedded = true)
            // --- Касса ---
            4 -> CashierGame(embedded = true, onFinish = onOpenReport, onCancel = { tab = 0 })
            // --- Учёт ларька (касса/склад/P&L) ---
            5 -> AccountingScreen(onBack = {}, embedded = true, onOpenReport = onOpenReport)
            // --- Найм кассира ---
            6 -> HireScreen(onBack = { tab = 0 }, embedded = true)
        }
    }
}

@Composable
private fun HotspotTag(label: String, enabled: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}