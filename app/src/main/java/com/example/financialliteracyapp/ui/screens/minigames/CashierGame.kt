package com.example.financialliteracyapp.ui.screens.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.screens.shop.ShopViewModel
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CashierGame(onFinish: () -> Unit, onCancel: () -> Unit = onFinish, embedded: Boolean = false) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val shopVm: ShopViewModel = viewModel(factory = ShopViewModel.factory(repo))
    val shop by shopVm.shop.collectAsState()
    val shopPrice = shop?.price ?: 8
    var customerIndex by remember { mutableIntStateOf(1) }
    var earned by remember { mutableIntStateOf(0) }
    var soldCount by remember { mutableIntStateOf(0) }
    var timer by remember { mutableFloatStateOf(20f) }
    var correctChange by remember { mutableIntStateOf(0) }
    val totalCustomers = 10
    val scope = rememberCoroutineScope()

    fun finishDay() {
        scope.launch { repo.runDay(soldCount) }
        onFinish()
    }

    // Генерация клиента
    fun newCustomer() {
        val extra = listOf(0, 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100).random()
        val paid = shopPrice + extra
        correctChange = paid - shopPrice
    }

    LaunchedEffect(customerIndex) {
        newCustomer()
        // 20 секунд на одного клиента
        timer = 20f
        while (timer > 0f && customerIndex <= totalCustomers) {
            delay(1000)
            timer -= 1f
        }
        if (customerIndex <= totalCustomers) {
            customerIndex++
        } else {
            finishDay()
        }
    }

    val options = remember(customerIndex) {
        val correct = correctChange
        (listOf(
            correct,
            correct + 50,
            correct + 100,
            correct - 20,
            correct - 40,
            correct + 20
        ).map { it.coerceAtLeast(0) }).distinct().shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!embedded) {
                Surface(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
            }
            Text("🧾 Касса",
                style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (timer / 20f).coerceIn(0f, 1f) },
            color = Danger,
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text("Клиент $customerIndex / $totalCustomers",
            fontWeight = FontWeight.Bold, fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        Text("🧑", fontSize = 80.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        AppCard {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Клиент даёт: ${correctChange + shopPrice} ₡", fontSize = 18.sp)
                Text("Лимонад: $shopPrice ₡", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text("Сдача?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Accent)
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(options) { option ->
                Button(
                    onClick = {
                        if (option == correctChange) {
                            earned += shopPrice
                            soldCount++
                        }
                        if (customerIndex < totalCustomers) customerIndex++ else finishDay()
                    },
                    modifier = Modifier.height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("$option ₡", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Заработано: $earned ₡",
            fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))
    }
}