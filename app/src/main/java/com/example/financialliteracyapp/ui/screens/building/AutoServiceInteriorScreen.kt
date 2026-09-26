package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.foundation.background
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

/** СТО: два подъёмника слева, механик-продавец справа.
 *  Сотрудник не показывается, пока не нанят; при найме «Касса» блокируется (работает сам). */
@Composable
fun AutoServiceInteriorScreen(
    buildingId: Long,
    onExit: () -> Unit,
    onOpenSuppliers: () -> Unit,
    onOpenCashier: () -> Unit,
    onOpenHire: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }

    val building by repo.observeBuildings().collectAsState(initial = emptyList())
    val stock = building.firstOrNull { it.id == buildingId }?.stock ?: 0
    val price = building.firstOrNull { it.id == buildingId }?.price ?: 30
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

            // Scene area (weight): подъёмники слева + сотрудник справа; поверх — авто-касса при найме
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left area - lift bays with stock
                    Column(
                        modifier = Modifier
                            .width(108.dp)
                            .padding(start = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LiftBayStock(stock = stock)
                        LiftBayStock(stock = stock)
                    }

                    Spacer(Modifier.weight(1f))

                    // Right area - hired worker (или пусто до найма)
                    HiredWorkerSlot(hired = hired, workerEmoji = "👨‍🔧", label = if (hired) "механик" else "место")
                }

                if (hired) {
                    AutoCashierAnimation(
                        buildingId = buildingId,
                        stock = stock,
                        price = price,
                        goodsEmoji = "🔧",
                        workerEmoji = "👨‍🔧",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Bottom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    Text(
                        "на полках: $stock 🔧",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D4C00)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenCashier,
                        enabled = !hired,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hired) Color(0xFFB0BEC5) else Color(0xFF007BB5),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (hired) "🧾 Касса недоступна" else "🧾 Касса", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onOpenHire,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (hired) "👤 Сотрудник" else "👤 Нанять", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiftBayStock(stock: Int) {
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
                .border(width = 8.dp, color = Color(0xFF007BB5))
        ) {
            Text("🚙", fontSize = 30.sp, modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-26).dp))
            Text("🔧🔨", fontSize = 17.sp, modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp))
        }
    }
}

@Composable
private fun HiredWorkerSlot(hired: Boolean, workerEmoji: String, label: String) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            if (hired) workerEmoji else "🪑",
            fontSize = if (hired) 30.sp else 24.sp,
            color = if (hired) Color.Unspecified else Color(0xFFBDBDBD)
        )
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
    }
}