package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.screens.journal.JournalScreen
import com.example.financialliteracyapp.ui.theme.*

/** Сводный блок учёта ларька (разбор.txt): один экран с подвкладками [Касса/Склад/P&L]. */
@Composable
fun AccountingScreen(
    onBack: () -> Unit,
    embedded: Boolean = false,
    onOpenReport: () -> Unit = {}
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()
    val cash = wallet?.cash ?: 500
    var subtab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!embedded) {
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
                Text("📊 Учёт ларька",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                Surface(shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface) {
                    Text("💰 $cash ₡", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold, color = Accent)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        TabRow(selectedTabIndex = subtab) {
            Tab(selected = subtab == 0, onClick = { subtab = 0 },
                text = { Text("💰 Касса", fontSize = 12.sp) })
            Tab(selected = subtab == 1, onClick = { subtab = 1 },
                text = { Text("📦 Склад", fontSize = 12.sp) })
            Tab(selected = subtab == 2, onClick = { subtab = 2 },
                text = { Text("📈 P&L", fontSize = 12.sp) })
        }

        when (subtab) {
            0 -> JournalScreen(onFinish = {}, embedded = true)
            1 -> InventoryScreen(onBack = {}, embedded = true)
            2 -> PnLScreen(onBack = {}, embedded = true)
        }

        if (!embedded) {
            Spacer(Modifier.height(12.dp))
            BigActionButton(
                "🤖 Симулировать день (20 ботов)",
                Primary,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = { vm.simulateBots { onOpenReport() } }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}