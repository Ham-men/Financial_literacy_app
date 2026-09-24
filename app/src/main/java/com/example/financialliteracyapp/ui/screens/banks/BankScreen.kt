package com.example.financialliteracyapp.ui.screens.banks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.ui.theme.*
import com.example.financialliteracyapp.ui.navigation.Routes
import kotlinx.coroutines.launch

private data class PlanOption(
    val id: String,
    val emoji: String,
    val text: String,
    val color: Color,
    val isTop: Boolean = false
)

private val options = listOf(
    PlanOption("want", "🚛💸", "деньги в желаемое", Color(0xFF2196F3), true),
    PlanOption("need", "👖💵", "деньги в карман\nна нужное", Color(0xFF4CAF50)),
    PlanOption("save", "🏦🤲💵", "деньги в копилку", Color(0xFFFFC107))
)

@Composable
fun BankScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: BankViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = BankViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()

    val planSet = (wallet?.needPlan ?: 0) + (wallet?.wantPlan ?: 0) + (wallet?.savePlan ?: 0) > 0

    if (wallet != null && planSet) {
        LockedPlanView(wallet = wallet!!, onBack = onBack)
        return
    }

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
                Text("${wallet?.needPlan ?: 0}  \\  ${wallet?.wantPlan ?: 0}  \\  ${wallet?.savePlan ?: 0}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Three options centered vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            options.forEachIndexed { index, option ->
                PlanOptionCard(
                    option = option,
                    isTop = option.isTop,
                    onClick = {
                        // TODO: handle selection
                    }
                )
                if (index == 0) {
                    // Add extra space before bottom row
                    Spacer(Modifier.height(16.dp))
                    // Bottom row with two options side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        options.drop(1).forEach { opt ->
                            PlanOptionCard(
                                option = opt,
                                isTop = false,
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanOptionCard(
    option: PlanOption,
    isTop: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTop) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black
                ) {
                    Text(option.emoji, fontSize = 48.sp, modifier = Modifier.padding(12.dp))
                }
            } else {
                Text(option.emoji, fontSize = 56.sp)
            }
        }
        Text(
            option.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

/** План уже зафиксирован на этот день: показываем цифры. */
@Composable
private fun LockedPlanView(wallet: WalletEntity, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("🎯 План на день", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            "Ты уже разделил деньги по 3 банкам. План зафиксирован — в этот день его не меняют.",
            fontSize = 13.sp, color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LockedBankRow("🍖 Нужное", wallet.needPlan, wallet.needFact, Color(0xFF4CAF50))
            LockedBankRow("🎀 Желаемое", wallet.wantPlan, wallet.wantFact, Color(0xFF2196F3))
            LockedBankRow("🐷 Копилка", wallet.savePlan, wallet.saveFact, Color(0xFFFFC107))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "В магазине «Нужное» списывается из зелёной банки, «Желаемое» — из синей. Копилка пополняет цели.",
            fontSize = 12.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("← Назад", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LockedBankRow(label: String, plan: Int, fact: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.weight(1f))
        Text("$plan ₡", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        if (fact > 0) {
            Spacer(Modifier.width(8.dp))
            Text("потрачено $fact ₡", fontSize = 12.sp, color = Color.Gray)
        }
    }
}