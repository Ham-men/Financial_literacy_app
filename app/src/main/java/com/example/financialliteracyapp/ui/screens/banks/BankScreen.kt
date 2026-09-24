package com.example.financialliteracyapp.ui.screens.banks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.theme.*

private data class BankOption(
    val id: String,
    val emoji: String,
    val text: String,
    val color: Color
)

private val banks = listOf(
    BankOption("NEED", "👖", "в карман", Color(0xFF4CAF50)),
    BankOption("WANT", "🚛", "в желаемое", Color(0xFF2196F3)),
    BankOption("SAVE", "🐷", "в копилку", Color(0xFFFFC107))
)

@Composable
fun BankScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: BankViewModel = viewModel(factory = BankViewModel.factory(repo))
    val wallet by vm.wallet.collectAsState()
    val cash = wallet?.cash ?: 0

    // Выбранная монета в руке (50 ₡ или остаток)
    var picked by remember { mutableStateOf<Int?>(null) }

    val (fullCoins, remainder) = GameRules.bagCoins(cash)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Шапка: счёт мешка (все заработанные деньги сюда)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "💼 Мешок",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$cash ₡",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF795548)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxSize()) {
            // Левая часть: монеты в мешке
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (cash == 0) {
                    Text("Мешок пуст", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Заработай в лареке, СТО или стройке,\nденьги придут сюда.",
                        fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )
                } else {
                    // Монеты по 50 ₡ (показываем до 6 шт., иначе «×N»)
                    val shownCoins = if (fullCoins in 1..6) fullCoins else 1
                    if (fullCoins > 0) {
                        if (fullCoins > 6) {
                            CoinIcon(50, picked == 50) {
                                picked = if (picked == 50) null else 50
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "50 ₡ ×$fullCoins",
                                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF795548)
                            )
                        } else {
                            repeat(shownCoins) {
                                CoinIcon(50, picked == 50) {
                                    picked = if (picked == 50) null else 50
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                    if (remainder > 0) {
                        CoinIcon(remainder, picked == remainder) {
                            picked = if (picked == remainder) null else remainder
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (picked != null) "🫳 Монета $picked ₡ — жми банку"
                        else "👆 Жми монету, потом банку",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (picked != null) Primary else Color.Gray
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Правая часть: банки столбиком
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                banks.forEachIndexed { index, bank ->
                    val balance = when (bank.id) {
                        "NEED" -> wallet?.needPlan ?: 0
                        "WANT" -> wallet?.wantPlan ?: 0
                        else -> wallet?.savePlan ?: 0
                    }
                    BankCard(
                        bank = bank,
                        balance = balance,
                        dropActive = picked != null,
                        onClick = {
                            val coin = picked
                            if (coin != null) {
                                vm.moveToBank(bank.id, coin)
                                picked = null
                            }
                        }
                    )
                    if (index < banks.lastIndex) Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(12.dp))
                if (wallet?.needPlan != 0 || wallet?.wantPlan != 0 || wallet?.savePlan != 0) {
                    OutlinedButton(
                        onClick = { vm.withdrawAllToBag() },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth().widthIn(min = 150.dp).height(42.dp)
                    ) {
                        Text("🎒 Вывести всё в мешок", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth().widthIn(min = 150.dp).height(42.dp)
                ) {
                    Text("← Назад", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Копилка растёт на 5% каждый день.",
                    fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CoinIcon(value: Int, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFFFFC107) else Color(0xFFF5E9CD),
        border = BorderStroke(2.dp, if (selected) Color(0xFFFF9800) else Color(0xFFD4B97A)),
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪙", fontSize = 22.sp)
                Text("$value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D4C00))
            }
        }
    }
}

@Composable
private fun BankCard(
    bank: BankOption,
    balance: Int,
    dropActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (dropActive) bank.color.copy(alpha = 0.30f) else bank.color.copy(alpha = 0.12f),
        border = BorderStroke(if (dropActive) 3.dp else 2.dp, bank.color),
        modifier = Modifier.width(160.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bank.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    bank.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "$balance ₡",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = bank.color
            )
        }
    }
}