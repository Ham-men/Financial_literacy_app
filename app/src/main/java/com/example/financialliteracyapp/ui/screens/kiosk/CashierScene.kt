package com.example.financialliteracyapp.ui.screens.kiosk

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.theme.Accent
import com.example.financialliteracyapp.ui.theme.Danger
import com.example.financialliteracyapp.ui.theme.Primary
import com.example.financialliteracyapp.ui.theme.TextSecondary
import kotlin.math.roundToInt
import kotlin.random.Random

/** Оплата покупателя на кассе: сколько дал монетами и какая сдача. */
private data class Payment(val paid: Int, val change: Int)

/**
 * «Живая касса» ларька (вместо мини-игры «Касса»):
 * покупатель берёт товар на полках (сцена), игрок тащит товар в кассу,
 * видит сумму, бот платит, игрок выдаёт сдачу и завершает продажу.
 * Если нанят кассир — он пробивает очередь сам.
 */
@Composable
fun CashierScene(
    price: Int,
    queue: List<KioskCustomer>,
    cashierHired: Boolean,
    servedToday: Int,
    onServe: (KioskCustomer) -> Unit,
    onGoScene: () -> Unit,
    onOpenReport: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "🧾 Касса — пробивай покупки покупателей из сцены",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LinearProgressIndicator(
            progress = { (servedToday.toFloat() / 5f).coerceIn(0f, 1f) },
            color = Primary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(6.dp)
        )
        Text(
            "Продано за эту смену: $servedToday шт.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (cashierHired) {
            HiredCashier(queue, onOpenReport, servedToday)
        } else {
            ManualCashier(price, queue, servedToday, onServe, onGoScene, onOpenReport)
        }
    }
}

/** Кассир пробивает очередь автоматически (продажи запускаются на уровне экрана, из любой вкладки). */
@Composable
private fun HiredCashier(
    queue: List<KioskCustomer>,
    onOpenReport: () -> Unit,
    servedToday: Int
) {
    val first = queue.firstOrNull()
    if (first != null) {
        AppCard(modifier = Modifier.padding(16.dp)) {
            Text("Кассир обслуживает очередь…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Осталось покупателей: ${queue.size}. Кассир берёт на кассе сам — ${first.units} шт. 🧃",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    } else {
        AppCard(modifier = Modifier.padding(16.dp)) {
            Text("Очередь пуста 😴", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                if (servedToday > 0) "Вся смена пробита. Можно завершить день и посмотреть итог."
                else "Пока никто не заглянул. Загляни в сцену — покупатели подойдут.",
                fontSize = 13.sp,
                color = TextSecondary
            )
            if (servedToday > 0) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onOpenReport,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("✅ Закончить день → отчёт", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Игрок ведёт кассу сам: тащит товар из корзины в кассу, выдаёт сдачу. */
@Composable
private fun ManualCashier(
    price: Int,
    queue: List<KioskCustomer>,
    servedToday: Int,
    onServe: (KioskCustomer) -> Unit,
    onGoScene: () -> Unit,
    onOpenReport: () -> Unit
) {
    val customer = queue.firstOrNull()
    if (customer == null) {
        AppCard(modifier = Modifier.padding(16.dp)) {
            Text("Никого в очереди 😴", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                if (servedToday > 0) "Смена пробита! Можно завершить день и посмотреть итог."
                else "Покупатели приходят, когда ты в сцене ларька. Загляни туда — боты подойдут к полкам.",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
            if (servedToday > 0) {
                Button(
                    onClick = onOpenReport,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("✅ Закончить день → отчёт", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onGoScene,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("🏬 Пойти к прилавку", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val rungSet = remember(customer.id) { mutableStateListOf<Int>() }
    val cartBounds = remember { mutableStateMapOf<Int, Rect>() }
    var boardOrigin by remember(customer.id) { mutableStateOf(Offset.Zero) }
    var registerRect by remember(customer.id) { mutableStateOf<Rect?>(null) }
    var draggedId by remember(customer.id) { mutableStateOf<Int?>(null) }
    var dropPos by remember(customer.id) { mutableStateOf(Offset.Zero) }
    var changeText by remember(customer.id) { mutableStateOf("") }
    var wrongChange by remember(customer.id) { mutableStateOf(false) }
    var payment by remember(customer.id) { mutableStateOf<Payment?>(null) }

    val total = rungSet.size * price
    val allRung = rungSet.size >= customer.units
    val change = payment?.change ?: 0

    LaunchedEffect(customer.id, rungSet.size) {
        if (allRung && payment == null) {
            payment = makePayment(total)
            changeText = ""
            wrongChange = false
        }
    }

    AppCard(modifier = Modifier.padding(16.dp)) {
        Text("Покупатель 🧑 — хочет ${customer.units} × 🧃", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Тащи бутылки слева в кассу справа. Сумма появится на дисплее.",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(240.dp)
            .background(Color(0xFFF2E3C6), RoundedCornerShape(16.dp))
            .onGloballyPositioned { boardOrigin = it.boundsInRoot().topLeft }
    ) {
        // Кассовый аппарат (справа)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
                .onGloballyPositioned { registerRect = it.boundsInRoot() },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF37474F),
            border = BorderStroke(2.dp, Color(0xFF90A4AE))
        ) {
            Column(
                Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Касса", fontSize = 12.sp, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text(
                    "$total ₡",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF176)
                )
                Spacer(Modifier.height(6.dp))
                if (rungSet.isEmpty()) {
                    Text("сюда 🧃", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                } else {
                    Text("+${rungSet.size}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        // Кнопки «готово» при завершении пробивки
        if (allRung) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val p = payment
                    if (p == null) {
                        Text("Считаю…", fontSize = 14.sp, color = TextSecondary)
                    } else {
                        Text("Сумма: ${p.paid - p.change} ₡", fontWeight = FontWeight.Bold)
                        Text("Бот даёт: ${p.paid} ₡", fontSize = 14.sp)
                        if (p.change == 0) {
                            Text("Сдачи не нужно ✓", fontSize = 14.sp, color = Accent)
                        } else {
                            Text("Сдача боту: ${p.change} ₡", fontSize = 14.sp, color = Accent)
                        }
                    }
                }
            }
        }

        // Корзина с товаром (слева) + летящая бутылка
        Row(
            Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (0 until customer.units).forEach { index ->
                if (index !in rungSet) {
                    CartJuice(
                        index,
                        isDragging = draggedId == index,
                        onBounds = { rect ->
                            cartBounds[index] = Rect(
                                rect.left - boardOrigin.x, rect.top - boardOrigin.y,
                                rect.right - boardOrigin.x, rect.bottom - boardOrigin.y
                            )
                        },
                        onDragStart = {
                            draggedId = index
                            val r = cartBounds[index]
                            dropPos = if (r != null) Offset(r.left + r.width / 2f, r.top + r.height / 2f)
                            else Offset.Zero
                        },
                        onDrag = { amount -> if (draggedId == index) dropPos += amount },
                        onDragEnd = {
                            val rootDrop = Offset(dropPos.x + boardOrigin.x, dropPos.y + boardOrigin.y)
                            val reg = registerRect
                            if (reg != null && reg.contains(rootDrop)) {
                                if (index !in rungSet) rungSet.add(index)
                            }
                            draggedId = null
                        },
                        onDragCancel = { if (draggedId == index) draggedId = null }
                    )
                }
            }
            val dragging = draggedId
            if (dragging != null) {
                Text(
                    "🧃",
                    fontSize = 30.sp,
                    modifier = Modifier
                        .zIndex(10f)
                        .align(Alignment.CenterVertically)
                        .offset {
                            IntOffset(
                                (dropPos.x - 18).roundToInt(),
                                (dropPos.y - 20).roundToInt()
                            )
                        }
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Прогресс пробивки
    LinearProgressIndicator(
        progress = { (rungSet.size.toFloat() / customer.units).coerceIn(0f, 1f) },
        color = Primary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(8.dp)
    )
    Text(
        "Пробито: ${rungSet.size} / ${customer.units}",
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    )

    if (allRung) {
        val p = payment
        if (p != null) {
            AppCard(modifier = Modifier.padding(16.dp)) {
                if (p.change == 0) {
                    Text("Бот дал ровно — сдачи нет. Заверши продажу.", fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onServe(customer) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("✅ Оплачено без сдачи", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Бот дал ${p.paid} ₡, товар ${p.paid - p.change} ₡. Выдай сдачу ${p.change} ₡.",
                        fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = changeText,
                        onValueChange = { value ->
                            changeText = value.filter { it.isDigit() }.take(4)
                            wrongChange = false
                        },
                        label = { Text("Сдача (₡)") },
                        singleLine = true,
                        isError = wrongChange,
                        supportingText = if (wrongChange) {
                            { Text("Проверь: ${p.paid} ₡ − ${p.paid - p.change} ₡ = ${p.change} ₡", color = Danger) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val entered = changeText.toIntOrNull()
                            if (entered == p.change) {
                                onServe(customer)
                            } else {
                                wrongChange = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("✅ Выдать сдачу", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun makePayment(total: Int): Payment {
    val bills = listOf(10, 20, 50, 100, 200)
    val paid = if (Random.nextBoolean()) {
        total
    } else {
        bills.firstOrNull { it >= total } ?: ((total / 100 + 1) * 100)
    }
    return Payment(paid = paid, change = (paid - total).coerceAtLeast(0))
}

@Composable
private fun CartJuice(
    id: Int,
    isDragging: Boolean,
    onBounds: (Rect) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(52.dp)
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .pointerInput(id) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = if (isDragging) Color(0xFFFFE0B2) else Color(0xFFFFFFFF),
        border = BorderStroke(1.dp, Color(0xFFBCAAA4))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("🧃", fontSize = 24.sp)
        }
    }
}