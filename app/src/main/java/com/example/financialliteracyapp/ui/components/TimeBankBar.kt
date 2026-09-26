package com.example.financialliteracyapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.local.entity.WalletEntity
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.ui.navigation.Routes

/** Глобальная инфо-панель: дата, время и 3 банки с зелёной подсветкой тратной на текущей сцене. */
@Composable
fun TimeBankBar(
    route: String?,
    day: Int,
    gameMinute: Int,
    wallet: WalletEntity?
) {
    val need = wallet?.needPlan ?: 0
    val want = wallet?.wantPlan ?: 0
    val save = wallet?.savePlan ?: 0
    val spend = spendBankFor(route)

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${GameRules.dateForDay(day)}  ${GameRules.timeLabel(gameMinute)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1
            )
            Text(
                if (GameRules.isShopOpen(gameMinute)) "🟢 Рабочий день"
                else if (GameRules.canSleep(gameMinute)) "🌙 Вечер — можно спать"
                else "😴 Пора спать",
                fontSize = 10.sp,
                color = if (GameRules.isShopOpen(gameMinute)) Color(0xFF2E7D32) else TextSecondary,
                maxLines = 1
            )
        }
        BankDot("нужное", need, spend, SpendBank.NEED)
        Spacer(Modifier.width(6.dp))
        BankDot("желаемое", want, spend, SpendBank.WANT)
        Spacer(Modifier.width(6.dp))
        BankDot("копилка", save, spend, SpendBank.SAVE)
    }
}

@Composable
private fun BankDot(label: String, amount: Int, spend: SpendBank?, bank: SpendBank) {
    val green = spend == bank
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .background(
                if (green) Color(0xFFC8E6C9) else Color(0xFFF5F5F5),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 9.sp, color = if (green) Color(0xFF2E7D32) else TextSecondary)
        Text(
            "$amount ₡",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (green) Color(0xFF2E7D32) else Color.Black
        )
    }
}

enum class SpendBank { NEED, WANT, SAVE }

/** Какая банка тратится на текущей сцене — её подсвечиваем зелёным. */
fun spendBankFor(route: String?): SpendBank? {
    if (route == null) return null
    return when {
        route == Routes.MAIN -> SpendBank.NEED        // корм/лечение
        route == Routes.ENTERTAINMENT -> SpendBank.WANT  // игрушки
        route == Routes.MARKET -> SpendBank.WANT
        route == Routes.GOALS -> SpendBank.SAVE       // откладываем в цель
        route == Routes.MAP || route == Routes.LOT ||
            route == Routes.CONSTRUCTION || route == Routes.AUTO_SERVICE ||
            route.startsWith(Routes.BUILDING) -> SpendBank.NEED
        route == Routes.BANKS || route == Routes.REPORT ||
            route == Routes.PROGRESS || route == Routes.REFERENCE || route == Routes.ADULT -> null
        else -> SpendBank.NEED // мини-игры, касса, найм, закупка — «нужное»
    }
}

private val TextSecondary = Color(0xFF757575)