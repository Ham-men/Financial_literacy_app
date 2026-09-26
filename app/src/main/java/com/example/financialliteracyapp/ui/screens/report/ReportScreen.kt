package com.example.financialliteracyapp.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.PeriodEntity
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.*

@Composable
fun ReportScreen() {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: ReportViewModel = viewModel(factory = ReportViewModel.factory(repo, prefs))
    val day by vm.day.collectAsState()
    val periods by vm.periods.collectAsState()

    // Главный отчёт — последний завершённый день; текущий день (ещё не закрыт) — рядом.
    val lastPeriod = periods.lastOrNull()
    val hasClosedPeriod = lastPeriod != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val titleDay = lastPeriod?.day ?: day
        Text("📊 Отчёт · день $titleDay (${GameRules.dateForDay(titleDay)})",
            style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(8.dp))

        if (!hasClosedPeriod) {
            AppCard {
                Text("День ещё не завершён", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("Отчёт появится, когда закончится день: ляг спать на сцене ДОМ (18:00–23:00).",
                    color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            PlanFactorCard(lastPeriod!!)
            MoneyCard(lastPeriod!!)
            StatusCard(lastPeriod!!, day)
            HistoryCard(periods)
        }

        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.height(24.dp))
    }
}

/** План vs факт: сколько выделено в банки и сколько реально потрачено/отложено. */
@Composable
private fun PlanFactorCard(period: PeriodEntity) {
    AppCard {
        Text("План ↔ факт", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Банка", Modifier.weight(1f), color = TextSecondary, fontSize = 12.sp)
            Text("План", Modifier.width(60.dp), color = TextSecondary, fontSize = 12.sp)
            Text("Факт", Modifier.width(60.dp), color = TextSecondary, fontSize = 12.sp)
        }
        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        factRow("👖 Нужное", period.needPlan, period.needFact)
        factRow("🚛 Желаемое", period.wantPlan, period.wantFact)
        factRow("🐷 Копилка", period.savePlan, period.saveFact)
    }
}

@Composable
private fun factRow(label: String, plan: Int, fact: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("$plan ₡", Modifier.width(60.dp), fontSize = 14.sp, color = TextPrimary)
        Text("$fact ₡", Modifier.width(60.dp), fontSize = 14.sp,
            color = if (plan == 0 || fact <= plan) Primary else Danger, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MoneyCard(period: PeriodEntity) {
    AppCard {
        Text("Движение денег за день", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        ReportRow("Доход:", "+${period.income} ₡", Primary)
        ReportRow("Расход:", "−${period.expense} ₡", Danger)
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        val bal = period.balance
        ReportRow("Итог:", "${if (bal >= 0) "+" else ""}$bal ₡", if (bal >= 0) Primary else Danger, big = true)
    }
}

@Composable
private fun StatusCard(period: PeriodEntity, currentDay: Int) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (period.success) "🎉" else "📋", fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                if (period.success) "План соблюдён! Что-то осталось и в копилке — так растёт твоя цель."
                else "День был потрачен без плана: разложи деньги по банкам и попробуй откладывать в копилку.",
                fontSize = 14.sp, color = TextSecondary
            )
        }
    }
}

@Composable
private fun HistoryCard(periods: List<PeriodEntity>) {
    val lastFew = periods.takeLast(7).reversed()
    AppCard {
        Text("История", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        lastFew.forEach { p ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("день ${p.day}", Modifier.weight(1f), fontSize = 13.sp,
                    color = if (p.success) Primary else TextSecondary, fontWeight = if (p.success) FontWeight.Bold else FontWeight.Normal)
                Text("доход +${p.income}", Modifier.width(110.dp), fontSize = 12.sp, color = TextSecondary)
                Text("в копилку ${p.savePlan}₡", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color, big: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            Modifier.weight(1f),
            fontSize = if (big) 16.sp else 14.sp,
            fontWeight = if (big) FontWeight.Bold else FontWeight.Normal)
        Text(value,
            fontSize = if (big) 18.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor)
    }
}