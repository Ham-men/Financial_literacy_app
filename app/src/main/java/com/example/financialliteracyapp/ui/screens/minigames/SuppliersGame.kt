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
import kotlin.math.roundToInt

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
fun SuppliersGame(
    onFinish: () -> Unit,
    embedded: Boolean = false,
    buildingId: Long = 0L
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()
    // Целевое здание (если buildingId не задан — стартовый «Продукты»)
    val allBuildings by repo.observeBuildings().collectAsState(initial = emptyList())
    val target = allBuildings.firstOrNull { it.id == buildingId }
        ?: allBuildings.firstOrNull { it.type == "PRODUCTS" }
    val stock = target?.stock ?: 0
    var selected by remember { mutableIntStateOf(-1) }
    var units by remember { mutableFloatStateOf(20f) }
    val qty = units.toInt()
    val cash = wallet?.cash ?: 0
    val needPlan = wallet?.needPlan ?: 0
    val wantPlan = wallet?.wantPlan ?: 0
    val savePlan = wallet?.savePlan ?: 0
    // Бюджет «нужное»: если план разложен — банка нужное + мешок, иначе весь мешок
    val planSet = needPlan + wantPlan + savePlan > 0
    val needAvailable = if (planSet) needPlan + cash else cash
    val cost = if (selected >= 0) (suppliersUi[selected].pricePerUnit * qty).toInt() else 0
    val canAfford = selected >= 0 && qty > 0 && needAvailable >= cost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!embedded) {
                Surface(
                    onClick = onFinish,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("◀", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
            }
            Text("🧾 Закупка у поставщика",
                style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "На складе: $stock шт · Бюджет «нужное»: $needAvailable ₡ (нужное $needPlan + мешок $cash)",
            color = TextSecondary, fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))

        // Два блока: слева поставщик, справа количество + покупка
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // --- Блок 1: выбор поставщика ---
            Column(Modifier.weight(1f)) {
                Text("1️⃣ Поставщик", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                suppliersUi.forEachIndexed { index, s ->
                    CompactSupplierCard(
                        supplier = s,
                        selected = selected == index,
                        onClick = { selected = index }
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.width(10.dp))

            // --- Блок 2: количество и покупка ---
            Column(Modifier.weight(1f)) {
                Text("2️⃣ Количество — $qty шт", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                AppCardCompact {
                    Slider(
                        value = units,
                        onValueChange = { units = it.roundToInt().toFloat() },
                        valueRange = 0f..50f,
                        steps = 49
                    )
                    Text("От 0 до 50 шт", fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(Modifier.height(8.dp))

                Text(
                    "Стоимость: $cost ₡",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Accent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (selected < 0) "Выбери поставщика 🙋"
                    else if (qty <= 0) "Укажи количество товара"
                    else if (!canAfford) "Не хватает денег!"
                    else "Итого: берем $qty шт за $cost ₡",
                    fontSize = 12.sp,
                    color = if (canAfford) TextSecondary else Danger
                )
                Spacer(Modifier.height(8.dp))
                BigActionButton(
                    text = "✅ Купить",
                    color = Primary,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canAfford
                ) {
                    if (selected >= 0 && qty > 0) {
                        val s = suppliersUi[selected]
                        val bid = target?.id ?: 0L
                        if (bid > 0L) vm.purchaseStockForBuilding(bid, s.pricePerUnit, qty)
                        else vm.purchaseStock(s.pricePerUnit, qty)
                        onFinish()
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCardCompact(content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) { content() }
}

@Composable
private fun CompactSupplierCard(
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(supplier.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text("${supplier.pricePerUnit} ₡/шт",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Primary)
            Spacer(Modifier.height(2.dp))
            Text(
                "Качество: ${"★".repeat(supplier.quality)}${"☆".repeat(5 - supplier.quality)} · Риск: ${supplier.risk}",
                fontSize = 9.sp, color = TextSecondary
            )
        }
    }
}