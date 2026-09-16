package com.example.financialliteracyapp.ui.screens.minigames

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.screens.shop.ShopViewModel
import com.example.financialliteracyapp.ui.theme.*

private data class SupplierUi(
    val name: String,
    val pricePerUnit: Double,
    val quality: Int,
    val risk: String
)

private val suppliersUi = listOf(
    SupplierUi("🐿 Сорока-Поставщик", 2.0, 2, "высокий"),
    SupplierUi("🦫 Бобёр-Завод", 3.0, 5, "низкий"),
    SupplierUi("🦊 Лис-Оптовик", 2.5, 4, "средний")
)

@Composable
fun SuppliersGame(onFinish: () -> Unit, embedded: Boolean = false) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()
    var selected by remember { mutableIntStateOf(-1) }
    val units = 50
    val cash = wallet?.cash ?: 0
    val canAfford = selected >= 0 && cash >= (suppliersUi[selected].pricePerUnit * units).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!embedded) {
                Surface(
                    onClick = onFinish,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
            }
            Text("🧾 Закупка у поставщика",
                style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text("Нужно купить $units лимонадов по лучшей цене",
            color = TextSecondary, fontSize = 14.sp)
        Text("Доступно: $cash ₡", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        suppliersUi.forEachIndexed { index, s ->
            SupplierCard(
                supplier = s,
                selected = selected == index,
                onClick = { selected = index }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))

        if (selected >= 0) {
            Text(
                "Стоимость: ${(suppliersUi[selected].pricePerUnit * units).toInt()} ₡" +
                        if (!canAfford) " — не хватает!" else "",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Accent,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))
        }

        BigActionButton(
            text = "✅ Купить",
            color = Primary,
            modifier = Modifier.fillMaxWidth(),
            enabled = canAfford
        ) {
            val s = suppliersUi[selected]
            vm.purchaseStock(s.pricePerUnit, units)
            onFinish()
        }
    }
}

@Composable
private fun SupplierCard(
    supplier: SupplierUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (selected)
            androidx.compose.foundation.BorderStroke(2.dp, Primary) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(supplier.name,
                    Modifier.weight(1f),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${supplier.pricePerUnit} ₡/шт.",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Качество: ${"★".repeat(supplier.quality)}${"☆".repeat(5 - supplier.quality)} " +
                        "· Риск брака: ${supplier.risk}",
                fontSize = 12.sp, color = TextSecondary
            )
        }
    }
}