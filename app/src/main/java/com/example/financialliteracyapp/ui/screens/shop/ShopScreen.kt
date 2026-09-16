package com.example.financialliteracyapp.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

/** Back office ларька: журнал, инвентарь, P&L, симуляция дня. */
@Composable
fun ShopScreen(
    onBack: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenPnL: () -> Unit,
    onSimulateBots: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val shop by vm.shop.collectAsState()
    val wallet by vm.wallet.collectAsState()

    val cash = wallet?.cash ?: 500
    val stock = shop?.stock ?: 100
    val costPrice = shop?.costPrice ?: 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📊 Учёт ларька",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f))
            Surface(shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface) {
                Text("💰 $cash ₡", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold, color = Accent)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Склад
        AppCard {
            Text("📦 Склад", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row {
                Text("Лимонад", Modifier.weight(1f))
                Text("$stock шт.", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                Text("Себест.: $costPrice ₡", color = TextSecondary)
            }
        }

        Spacer(Modifier.height(12.dp))

        BigActionButton("🧾 Кассовая книга", PrimaryDark,
            Modifier.fillMaxWidth(), onClick = onOpenJournal)
        Spacer(Modifier.height(8.dp))
        BigActionButton("📦 Товарный журнал", PrimaryDark,
            Modifier.fillMaxWidth(), onClick = onOpenInventory)
        Spacer(Modifier.height(8.dp))
        BigActionButton("📈 P&L (Прибыли и убытки)", PrimaryDark,
            Modifier.fillMaxWidth(), onClick = onOpenPnL)
        Spacer(Modifier.height(8.dp))
        BigActionButton("🤖 Симулировать день (20 ботов)", Primary,
            Modifier.fillMaxWidth(), onClick = {
                vm.simulateBots { onSimulateBots?.invoke() }
            })

        Spacer(Modifier.height(24.dp))
    }
}