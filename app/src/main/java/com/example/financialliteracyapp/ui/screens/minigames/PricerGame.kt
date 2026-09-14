package com.example.financialliteracyapp.ui.screens.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.financialliteracyapp.domain.economy.BotBrain
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.screens.shop.ShopViewModel
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun PricerGame(onFinish: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val vm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val shop by vm.shop.collectAsState()
    var price by remember(shop?.price) { mutableFloatStateOf((shop?.price ?: 8).toFloat()) }
    val cost = shop?.costPrice ?: 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Поставь цену",
            style = MaterialTheme.typography.headlineMedium)
        Text("Слишком дорого — клиенты уйдут, слишком дёшево — потеряешь прибыль",
            fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(24.dp))

        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🍋", fontSize = 48.sp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Лимонад", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Себестоимость: $cost ₡", fontSize = 13.sp, color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("${price.toInt()} ₡",
            fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Accent,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Slider(
            value = price,
            onValueChange = { price = it },
            valueRange = 1f..30f,
            steps = 57
        )

        Spacer(Modifier.height(16.dp))

        AppCard {
            Text("📊 Подсказки", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Средний покупатель готов платить: 9 ₡",
                fontSize = 13.sp, color = TextSecondary)
            Text("Конкуренты: 7, 8, 10 ₡",
                fontSize = 13.sp, color = TextSecondary)
            Text("Прогноз: ~${BotBrain.estimateBuyers(price)} покупателей/день",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
        }

        Spacer(Modifier.weight(1f))

        BigActionButton("✅ Установить цену", Primary,
            Modifier.fillMaxWidth(), onClick = {
                vm.setPrice(price.toInt())
                onFinish()
            })
    }
}