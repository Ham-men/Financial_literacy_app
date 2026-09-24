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

/** Стройматериалы: витрина в стиле СТО — две полки с инструментами слева, работники справа. */
@Composable
fun ConstructionInteriorScreen(
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                "Дата 01.01.2020 12:00",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("нужное  \\  желаемое  \\  копилка", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("100  \\  200  \\  200", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

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

        // Two tool shelves (left) + workers (right) — same layout as STO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left area - tool shelves with stock
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ToolShelfBay(stock = stock)
                ToolShelfBay(stock = stock)
            }

            Spacer(Modifier.weight(1f))

            // Right area - workers and labels
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(64.dp)
            ) {
                WorkerWithLabel(label = "нанять работника")
                WorkerWithLabel(label = "нанять работника")
            }
        }

        Spacer(Modifier.weight(1f))

        // Bottom controls: товар (left), касса + нанять (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Buy goods button
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

/** Полка с инструментами: рамка как подъёмник в СТО, внутри сетка инструментов. */
@Composable
private fun ToolShelfBay(stock: Int) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .height(110.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFFA07A55))
                .border(width = 4.dp, color = Color(0xFF7A5A3A))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Tool grid 2x2
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🔧", fontSize = 22.sp)
                    Text("🪛", fontSize = 22.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🗜️", fontSize = 22.sp)
                    Text("🔨", fontSize = 22.sp)
                }
            }
        }
        // Stock label under the bay
        Text(
            "на полках: $stock",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 86.dp)
        )
    }
}

@Composable
private fun WorkerWithLabel(label: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("👷", fontSize = 42.sp)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
    }
}
