package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Кнопки здания «🧾 Касса» + «👤 Нанять/Сотрудник».
 *  vertical=true (портрет): столбик — касса выше, сотрудник под ней (иначе не виден);
 *  vertical=false (ландшафт): рядом, как раньше. */
@Composable
fun ShopActionButtons(
    vertical: Boolean,
    hired: Boolean,
    onOpenCashier: () -> Unit,
    onOpenHire: () -> Unit
) {
    val cashierButton: @Composable () -> Unit = {
        Button(
            onClick = onOpenCashier,
            enabled = !hired,
            modifier = Modifier
                .width(168.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hired) Color(0xFFB0BEC5) else Color(0xFF007BB5),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (hired) "🧾 Касса недоступна" else "🧾 Касса", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
    val hireButton: @Composable () -> Unit = {
        Button(
            onClick = onOpenHire,
            modifier = Modifier
                .width(168.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (hired) "👤 Сотрудник" else "👤 Нанять", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }

    if (vertical) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            cashierButton()
            hireButton()
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cashierButton()
            hireButton()
        }
    }
}