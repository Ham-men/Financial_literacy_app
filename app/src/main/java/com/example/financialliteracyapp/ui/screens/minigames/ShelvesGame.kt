package com.example.financialliteracyapp.ui.screens.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financialliteracyapp.ui.theme.*
import kotlin.math.roundToInt

private data class Bottle(val id: Int, val emoji: String, val name: String, val perishable: Boolean)

private val bottlesList = listOf(
    Bottle(0, "🍋", "Лимонад", perishable = true),
    Bottle(1, "🧃", "Морс", perishable = true),
    Bottle(2, "🥤", "Вода", perishable = false),
    Bottle(3, "🍶", "Квас", perishable = false)
)

// Ячейки полки: 0,1 — дальний ряд, 2,3 — ближний ряд (край)
private val cellsCount = 4

/** Мини-игра «Расставь» (День 18): перетащи товар на полку. Скоропорт — ближе к краю. */
@Composable
fun ShelvesGame(onFinish: () -> Unit) {
    val placed = remember { mutableStateMapOf<Int, Int>() }   // bottle -> cell
    val cellBounds = remember { mutableStateMapOf<Int, Rect>() }
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }
    var gameOrigin by remember { mutableStateOf(Offset.Zero) }
    var draggedId by remember { mutableStateOf<Int?>(null) }
    var dropPos by remember { mutableStateOf(Offset.Zero) }     // в координатах доски
    var hint by remember { mutableStateOf<String?>(null) }
    var flashCell by remember { mutableStateOf<Int?>(null) }

    val won = placed.size == bottlesList.size

    fun placeBottle(bottle: Bottle) {
        if (draggedId != bottle.id) return
        // dropPos в координатах доски, cellBounds в экранных — приводим к одному пространству
        val rootDrop = Offset(dropPos.x + gameOrigin.x, dropPos.y + gameOrigin.y)
        val target = (0 until cellsCount).firstOrNull { cellBounds[it]?.contains(rootDrop) == true }
        if (target == null) {
            hint = "Бутылка упала мимо полки. Попробуй ещё!"
            draggedId = null
            return
        }
        if (placed.values.contains(target)) {
            hint = "Там уже стоит бутылка. Выбери свободную ячейку."
            draggedId = null
            return
        }
        val isEdgeRow = target >= 2
        val ok = if (bottle.perishable) isEdgeRow else !isEdgeRow
        if (ok) {
            placed[bottle.id] = target
            hint = null
            flashCell = null
        } else {
            flashCell = target
            hint = if (bottle.perishable)
                "«${bottle.name}» — скоропорт! Так покупатель не найдёт. Поставь ближе к краю."
            else
                "«${bottle.name}» хранится долго — его место вглубь, подальше от покупателя."
        }
        draggedId = null
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🗄️ Расставь товар", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Surface(
                onClick = onFinish,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀ Назад", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                onClick = { placed.clear(); hint = null; flashCell = null; draggedId = null },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("🔄 Заново", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 13.sp)
            }
        }

        Text(
            "Правило: скоропорт (лимонад, морс) — ближе к краю. Вода и квас хранятся долго — вглубь.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Доска: полки сверху, тележка снизу
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(280.dp)
                .background(Color(0xFFF2E3C6), RoundedCornerShape(16.dp))
                .onGloballyPositioned { gameOrigin = it.boundsInRoot().topLeft }
        ) {
            // Полки
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ShelfLabel("Дальний ряд — вглубь, подальше от покупателя")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShelfCell(0, cellBounds, placed, flashCell)
                    ShelfCell(1, cellBounds, placed, flashCell)
                }
                Spacer(Modifier.height(8.dp))
                ShelfLabel("Ближний ряд — к краю, где видно покупателю")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShelfCell(2, cellBounds, placed, flashCell)
                    ShelfCell(3, cellBounds, placed, flashCell)
                }
            }

            // Тележка с товаром
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                bottlesList.forEach { bottle ->
                    CartSlot(
                        bottle = bottle,
                        placedCell = placed[bottle.id],
                        isDragging = draggedId == bottle.id,
                        onBounds = { rect ->
                            slotBounds[bottle.id] = Rect(rect.left - gameOrigin.x, rect.top - gameOrigin.y, rect.right - gameOrigin.x, rect.bottom - gameOrigin.y)
                        },
                        onDragStart = {
                            if (placed[bottle.id] == null) {
                                draggedId = bottle.id
                                val r = slotBounds[bottle.id]
                                dropPos = if (r != null) Offset(r.left + r.width / 2f, r.top + r.height / 2f)
                                else Offset.Zero
                            }
                        },
                        onDrag = { amount ->
                            if (draggedId == bottle.id) dropPos += amount
                        },
                        onDragEnd = { placeBottle(bottle) },
                        onDragCancel = { if (draggedId == bottle.id) draggedId = null }
                    )
                }
            }

            // Летящая бутылка (оверлей)
            val dragging = draggedId
            if (dragging != null) {
                val bottle = bottlesList.firstOrNull { it.id == dragging }
                if (bottle != null) {
                    Text(
                        bottle.emoji,
                        fontSize = 34.sp,
                        modifier = Modifier
                            .zIndex(10f)
                            .align(Alignment.TopStart)
                            .offset {
                                IntOffset(
                                    (dropPos.x - 24).roundToInt(),
                                    (dropPos.y - 28).roundToInt()
                                )
                            }
                    )
                }
            }
        }

        // Сообщение Финни
        Surface(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF3E0)
        ) {
            Text(
                when {
                    hint != null -> "🦝 Финни: $hint"
                    won -> "🦝🎉 Ура! Все бутылки на месте! Скоропорт у края — покупатели сразу найдут товар."
                    else -> "🦝 Финни: тяни бутылку и отпускай над ячейкой полки."
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (won) {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp).height(52.dp)
            ) {
                Text("Готово — возвращаемся к ларьку", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ShelfLabel(text: String) {
    Text(text, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun ShelfCell(
    cellId: Int,
    cellBounds: MutableMap<Int, Rect>,
    placed: MutableMap<Int, Int>,
    flashCell: Int?
) {
    val occupantEmoji = bottlesList.firstOrNull { placed[it.id] == cellId }?.emoji
    val highlight = flashCell == cellId
    Surface(
        modifier = Modifier
            .size(width = 104.dp, height = 64.dp)
            .onGloballyPositioned {
                val r = it.boundsInRoot()
                cellBounds[cellId] = Rect(r.left, r.top, r.right, r.bottom)
            },
        shape = RoundedCornerShape(10.dp),
        color = when {
            occupantEmoji != null -> Color(0xFFC8E6C9)
            highlight -> Color(0xFFFFCDD2)
            else -> Color(0xFFFFFFFF)
        },
        border = BorderStroke(2.dp, if (highlight) Danger else Color(0xFFA1887F))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(occupantEmoji ?: "·", fontSize = if (occupantEmoji != null) 26.sp else 20.sp)
        }
    }
}

@Composable
private fun CartSlot(
    bottle: Bottle,
    placedCell: Int?,
    isDragging: Boolean,
    onBounds: (Rect) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .onGloballyPositioned { onBounds(it.boundsInRoot()) }
            .pointerInput(bottle.id) {
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
            if (placedCell == null && !isDragging) {
                Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(bottle.emoji, fontSize = 24.sp)
                    Text(bottle.name, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
                }
            } else {
                Text("·", fontSize = 22.sp)
            }
        }
    }
}