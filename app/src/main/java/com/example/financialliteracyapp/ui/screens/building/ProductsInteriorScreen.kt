package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
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
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.theme.*
import com.example.financialliteracyapp.ui.navigation.Routes
import kotlinx.coroutines.launch

/** Продуктовый ларёк: витрина в стиле СТО — две полки с товарами слева, кассиры справа. */
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
    val scope = rememberCoroutineScope()

    val wallet by repo.observeWallet().collectAsState(initial = null)
    val cash = wallet?.cash ?: 500
    val building by repo.observeBuildings().collectAsState(initial = emptyList())
    val stock = building.firstOrNull { it.id == buildingId }?.stock ?: 0

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

        Spacer(Modifier.height(16.dp))

        // Two goods shelves (left) + cashiers (right) — same layout as STO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left area - product shelves with stock
            Column(
                modifier = Modifier
                    .width(108.dp)
                    .padding(start = 6.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                GoodsShelfBay(stock = stock)
                GoodsShelfBay(stock = stock)
            }

            Spacer(Modifier.weight(1f))

            // Right area - cashiers and labels
            Column(
                modifier = Modifier
                    .width(92.dp)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(56.dp)
            ) {
                CashierWithLabel(label = "нанять кассира")
                CustomerWithLabel(label = "покупатель")
            }
        }

        Spacer(Modifier.weight(1f))

        // Bottom controls: товар (left), касса + нанять (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
// Buy goods button + stock label to the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenSuppliers,
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8D6E63),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("📦 Товар", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "на полках: $stock 🥤",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6D4C00)
            )
        }

            // Cashier + hire buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenCashier,
                    modifier = Modifier
                        .height(44.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007BB5),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🧾 Касса", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenHire,
                    modifier = Modifier
                        .height(44.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("👤 Нанять", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
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
            .height(92.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFA07A55))
                .border(width = 3.dp, color = Color(0xFF7A5A3A))
                .padding(5.dp),
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
                            Text(if (idx < filled) "🥤" else "▫️", fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        // Shelf label moved next to «Товар» button in the bottom controls
    }
}

@Composable
private fun CashierWithLabel(label: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text("🧑‍💼", fontSize = 34.sp)
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CustomerWithLabel(label: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text("🧑", fontSize = 34.sp)
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
    }
}