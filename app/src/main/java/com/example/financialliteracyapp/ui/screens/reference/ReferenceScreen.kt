package com.example.financialliteracyapp.ui.screens.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.ui.theme.*

/** Справочник терминов (День 21, §18 сюжета 3). Каждый термин — 1 предложение + картинка. */
@Composable
fun ReferenceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("📖 Справочник",
            style = MaterialTheme.typography.headlineMedium)
        Text("Коротко о главных словах денежного мира",
            fontSize = 13.sp, color = TextSecondary)

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(terms) { term ->
                ReferenceItem(term)
            }
        }
    }
}

private data class Term(
    val emoji: String,
    val word: String,
    val simple: String,
    val example: String
)

private val terms = listOf(
    Term("🍞", "Нужное", "то, без чего не прожить: еда, вода, дом.",
        "Корм для Финни и лимонад в ларьке — нужное."),
    Term("🎈", "Желаемое", "то, что очень хочется, но без него можно подождать.",
        "Мячик, бантик и торт — это желаемое."),
    Term("🐷", "Копилка", "место, где деньги копятся на большую цель.",
        "Положи монетку в копилку — цель станет ближе."),
    Term("📅", "План", "решение, как разделить деньги в начале дня.",
        "Сначала подумай, потом трать — это план дня."),
    Term("✅", "Факт", "сколько денег ты на самом деле потратил и получил.",
        "Факт показывает, получился ли план."),
    Term("🎯", "Цель", "большая покупка, к которой ты идёшь не спеша.",
        "Сберечь 300 ₡ на мячик — твоя цель."),
    Term("💸", "Сдача", "лишние монеты, которые надо вернуть покупателю.",
        "Дал монетку 100, товар 70 — сдача 30."),
    Term("🏷️", "Цена", "сколько монет стоит одна штука товара.",
        "Если цена выше — покупателей меньше."),
    Term("📊", "Выручка", "все деньги, что ларёк заработал за день.",
        "Продал 10 лимонадов по 8 ₡ — выручка 80 ₡.")
)

@Composable
private fun ReferenceItem(term: Term) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(term.emoji, fontSize = 30.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(term.word, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(term.example, fontSize = 11.sp, color = TextSecondary)
                }
                Spacer(Modifier.height(3.dp))
                Text(term.simple, fontSize = 14.sp, color = TextPrimary)
            }
        }
    }
}