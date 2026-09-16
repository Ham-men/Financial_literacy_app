package com.example.financialliteracyapp.ui.screens.kiosk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.financialliteracyapp.domain.economy.Balance
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.launch

/** Кабинет найма (День 19): стол, стул, Мила-кассир, зарплата 80 ₡/день. */
@Composable
fun HireScreen(onBack: () -> Unit, embedded: Boolean = false) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val scope = rememberCoroutineScope()

    val cashierHired by prefs.cashierHired.collectAsState(initial = false)
    val wallet by repo.observeWallet().collectAsState(initial = null)
    val cash = wallet?.cash ?: 500

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!embedded) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(
                "🏢 Кабинет найма",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
        }

        // --- Кабинет (вид сбоку) ---
        Box(
            Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFE8E2D4))
        ) {
            // Стена
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFDCD3C0))
            )
            // Табличка зарплаты на стене
            Surface(
                Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF8E1)
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💼 Вакансия: кассир", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Зарплата ${Balance.CASHIER_SALARY} ₡ в день", fontSize = 12.sp, color = TextSecondary)
                    Text("Сама стоит на кассе, ты отдыхаешь", fontSize = 12.sp, color = Accent)
                }
            }
            // Стол в центре
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .padding(horizontal = 40.dp)
                    .background(Color(0xFF6D4C41), RoundedCornerShape(8.dp))
            )
            // Стул руководителя (валидный собес по-взрослому)
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 118.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🪑", fontSize = 32.sp)
            }
            // Финни за столом (интервьюер)
            Text(
                "🦝",
                fontSize = 46.sp,
                modifier = Modifier.align(Alignment.Center).padding(bottom = 108.dp)
            )
            // Мила-кассир (справа, стоит с резюме)
            Text(
                "👩",
                fontSize = 46.sp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 28.dp, bottom = 108.dp)
            )
            Text(
                "📄",
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 74.dp, bottom = 108.dp)
            )
        }

        // Текущий статус
        AppCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                if (cashierHired) "👩 Мила работает кассиром" else "👩 Мила ждёт ответа",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (cashierHired)
                    "Мила сама считает сдачу на кассе — «Кассу» можно пропускать. Зарплата ${Balance.CASHIER_SALARY} ₡ списывается в конце дня, только если баланс позволяет (в минус не уходим)."
                else
                    "Мила — быстрый и честный кассир. Если нанять её, зарплата ${Balance.CASHIER_SALARY} ₡ в день станет расходом ларька.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(16.dp))

        if (cashierHired) {
            BigActionButton(
                "✅ Мила на кассе",
                PrimaryDark,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = onBack
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { scope.launch { prefs.setCashierHired(false) } },
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("❌ Уволить Милу")
            }
        } else {
            BigActionButton(
                "👩 Нанять Милу (${Balance.CASHIER_SALARY} ₡/день)",
                Primary,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                scope.launch { prefs.setCashierHired(true) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Пока сам")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Подсказка: найм выгоден на больших продажах — освобождает время\nи ускоряет обслуживание покупателей. Но зарплату надо платить каждый день.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(24.dp))
    }
}