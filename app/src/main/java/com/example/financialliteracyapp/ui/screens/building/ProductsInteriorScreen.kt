package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.R
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.theme.*

/** Продуктовый ларёк: витрина в стиле СТО — две полки с товарами слева, кассир справа.
 *  Сотрудник не показывается, пока не нанят; при найме «Касса» блокируется (работает сам). */
@Composable
fun ProductsInteriorScreen(
    buildingId: Long,
    onExit: () -> Unit,
    onOpenSuppliers: () -> Unit,
    onOpenCashier: () -> Unit,
    onOpenHire: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val verticalActions = LocalConfiguration.current.screenWidthDp < LocalConfiguration.current.screenHeightDp

    val building by repo.observeBuildings().collectAsState(initial = emptyList())
    val stock = building.firstOrNull { it.id == buildingId }?.stock ?: 0
    val price = building.firstOrNull { it.id == buildingId }?.price ?: 8
    val hiredBuildings by prefs.hiredBuildings.collectAsState(initial = emptySet())
    val hired = buildingId in hiredBuildings

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Door at top center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(Color.White)
                    .border(width = 2.dp, color = Color(0xFFCCCCCC))
                    .padding(top = 10.dp),
                contentAlignment = Alignment.TopCenter
            ) {}

            Spacer(Modifier.height(12.dp))

            // Scene area (weight): полки слева + сотрудник справа; поверх — авто-касса при найме
            Box(Modifier.fillMaxWidth().weight(1f)) {
                // Two goods shelves (left) + cashier (right) — same layout as STO
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left area - product shelves with stock
                    Column(
                        modifier = Modifier
                            .width(108.dp)
                            .padding(start = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GoodsShelfBay(stock = stock)
                        GoodsShelfBay(stock = stock)
                    }

                    Spacer(Modifier.weight(1f))

                    // Right area - hired worker (или пусто до найма)
                    HiredWorkerSlot(hired = hired, label = if (hired) "кассир" else "место")
                }

                // Авто-касса нанятого сотрудника (боты платят без сдачи)
                if (hired) {
                    AutoCashierAnimation(
                        buildingId = buildingId,
                        stock = stock,
                        price = price,
                        goodsEmoji = "🥤",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Bottom controls: товар (left), касса + нанять (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "на полках: $stock 🥤",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D4C00)
                    )
                    Button(
                        onClick = onOpenSuppliers,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8D6E63),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📦 Товар", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                ShopActionButtons(
                    vertical = verticalActions,
                    hired = hired,
                    onOpenCashier = onOpenCashier,
                    onOpenHire = onOpenHire
                )
            }
        }
    }
}

/** Полка с товарами: рамка как подъёмник в СТО, ячейки товара по остатку склада. */
@Composable
private fun GoodsShelfBay(stock: Int) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .height(84.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFA07A55))
                .border(width = 3.dp, color = Color(0xFF7A5A3A))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            val filled = stock.coerceIn(0, 9)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(3) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(3) { col ->
                            val idx = row * 3 + col
                            Text(if (idx < filled) "🥤" else "⬜", fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HiredWorkerSlot(hired: Boolean, label: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (hired) {
            Image(
                painterResource(R.drawable.ic_bot_worker),
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        } else {
            Text(
                "🪑",
                fontSize = 24.sp,
                color = Color(0xFFBDBDBD)
            )
        }
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
    }
}