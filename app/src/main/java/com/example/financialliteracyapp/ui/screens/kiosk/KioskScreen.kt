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
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.screens.shop.ShopViewModel
import com.example.financialliteracyapp.ui.theme.*

/** Сцена ларька (День 17): комната с прилавком, полками, кассой, дверью, стулом кассира. */
@Composable
fun KioskScreen(
    onBack: () -> Unit,
    onBuyStock: () -> Unit,
    onOpenPricer: () -> Unit,
    onOpenCashier: () -> Unit,
    onOpenShelves: () -> Unit,
    onOpenOffice: () -> Unit,
    onOpenHire: () -> Unit
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

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
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

        // --- Комната ларька (вид сбоку) ---
        Box(
            Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFF2E3C6))
        ) {
            // Стена
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFFFE0B2))
            )
            // Окно
            Box(
                Modifier
                    .width(120.dp)
                    .height(80.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 26.dp)
                    .background(Color(0xFFB3E5FC), RoundedCornerShape(10.dp))
            )
            // Дверь + ящик закупки (слева)
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .width(80.dp)
                    .height(130.dp)
                    .clickable { onBuyStock() }
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
                    HotspotTag("Закупка", enabled = true)
                }
            }
            // Полки (справа) — пустые, пока нет закупки; тап — мини-игра «Расставь»
            Column(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 42.dp)
                    .clickable(enabled = stock > 0) { onOpenShelves() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier
                        .width(120.dp)
                        .height(92.dp)
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
            // Стул кассира (найм — день 19). Если Мила нанята — она стоит здесь.
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 118.dp, bottom = 40.dp)
                    .clickable { onOpenHire() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (cashierHired) "👩" else "🪑", fontSize = 30.sp)
                HotspotTag(if (cashierHired) "Мила · касса" else "Нанять кассира", enabled = true)
            }
            // Финни в зале
            Text(
                "🦝",
                fontSize = 46.sp,
                modifier = Modifier.align(Alignment.Center).padding(bottom = 74.dp)
            )
            // Прилавок
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF8D6E63))
            )
            // Ценник и касса на прилавке — горячие точки
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 42.dp),
                horizontalArrangement = Arrangement.spacedBy(44.dp)
            ) {
                HotspotButton("🏷️", "Ценник") { onOpenPricer() }
                HotspotButton(
                    if (cashierHired) "👩‍💻" else "🧾",
                    if (cashierHired) "Касса · Мила" else "Касса"
                ) { onOpenCashier() }
            }
        }

        // Подсказка про полки и кассира
        Text(
            if (stock > 0)
                "Полки с товаром. Продажи — за кассой 🧾, цену меняй на ценнике 🏷️." +
                (if (cashierHired) "\nМила-кассир 👩 помогает: касса работает быстрее." else "\nСтул у кассы пустует — тапни и наними кассира 👩.")
            else
                "Полки пусты. Закупи лимонад у ящика у двери 📦, потом расставь на полки." +
                (if (cashierHired) "\nМила-кассир 👩 уже на месте, но товара нет." else "\nСтул у кассы пустует — тапни и наними кассира 👩."),
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)
        )

        // Склад и цена
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
                "Сменить цену — тапни по ценнику 🏷️ на прилавке.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(16.dp))

        // Учёт ларька (back office)
        BigActionButton(
            "📊 Учёт ларька",
            PrimaryDark,
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            onClick = onOpenOffice
        )

        Spacer(Modifier.height(24.dp))
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

@Composable
private fun HotspotButton(icon: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(icon, fontSize = 30.sp)
        HotspotTag(label, enabled = true)
    }
}