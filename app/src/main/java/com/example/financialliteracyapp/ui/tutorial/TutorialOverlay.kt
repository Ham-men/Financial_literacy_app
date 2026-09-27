package com.example.financialliteracyapp.ui.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Центр точки-цели стрелки обучения в долях экрана (0..1). */
private fun spotCenter(spot: Spot): Offset = when (spot) {
    // Обучение всегда в портрете: «меню слева» превращается в панель внизу экрана
    Spot.SIDEBAR -> Offset(0.5f, 0.9f)
    Spot.HUD -> Offset(0.5f, 0.08f)
    Spot.TOP_LEFT -> Offset(0.18f, 0.14f)
    Spot.TOP_CENTER -> Offset(0.5f, 0.14f)
    Spot.TOP_RIGHT -> Offset(0.84f, 0.12f)
    Spot.CENTER_LEFT -> Offset(0.22f, 0.45f)
    Spot.CENTER -> Offset(0.5f, 0.45f)
    Spot.CENTER_RIGHT -> Offset(0.8f, 0.45f)
    Spot.BOTTOM_LEFT -> Offset(0.22f, 0.7f)
    Spot.BOTTOM_CENTER -> Offset(0.5f, 0.74f)
    Spot.BOTTOM_RIGHT -> Offset(0.82f, 0.7f)
}

/** Перенос карточки на противоположную сторону от цели:
 *  цель внизу (y>0.5) → карточка сверху; цель вверху → карточка снизу. */
private fun cardAbove(spot: Spot): Boolean = spotCenter(spot).y > 0.5f

/** Оверлей обучения: затемнение, выделенная область, стрелка и карточка с текстом. */
@Composable
fun TutorialOverlay(
    step: TutorialStep,
    index: Int,
    total: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val cardAtTop = cardAbove(step.spot)

    Box(Modifier.fillMaxSize()) {
        // Затемняющий слой (блокирует тапы под собой)
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xB3000000))
                .pointerInput(Unit) { detectTapGestures { } }
        )

        // Стрелка + подсветка области
        Canvas(Modifier.fillMaxSize()) {
            val f = spotCenter(step.spot)
            val cx = f.x * size.width
            val cy = f.y * size.height

            // Подсвеченная область вокруг цели
            val d = min(size.width, size.height) * 0.16f
            val light = Rect(
                Offset(cx - d / 2, cy - d / 2),
                Size(d, d)
            )
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = light.topLeft,
                size = light.size,
                cornerRadius = CornerRadius(14f, 14f),
                alpha = 0.28f
            )
            drawRoundRect(
                color = Color(0xFFFFEB3B),
                topLeft = light.topLeft,
                size = light.size,
                cornerRadius = CornerRadius(14f, 14f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )

            // Стрелка от карточки к цели (карточка — на противоположной стороне от цели)
            val start = if (cardAtTop)
                Offset(size.width / 2f, size.height * 0.30f + 12.dp.toPx())
            else
                Offset(size.width / 2f, size.height - size.height * 0.30f - 12.dp.toPx())
            val end = Offset(cx, cy)
            val dx = end.x - start.x
            val dy = end.y - start.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy)
            if (len > 30f) {
                val ang = atan2(dy, dx)
                val headLen = min(size.width, size.height) * 0.045f
                val a1 = Offset(
                    end.x - headLen * cos(ang - 0.35f),
                    end.y - headLen * sin(ang - 0.35f)
                )
                val a2 = Offset(
                    end.x - headLen * cos(ang + 0.35f),
                    end.y - headLen * sin(ang + 0.35f)
                )
                // Пунктирная линия
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(start.x, start.y)
                    lineTo(end.x, end.y)
                    lineTo(a1.x, a1.y)
                    moveTo(end.x, end.y)
                    lineTo(a2.x, a2.y)
                }.let { path ->
                    drawPath(path, Color(0xFFFFEB3B), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                }
            }
        }

        // Карточка с описанием (на противоположной стороне от цели)
        Surface(
            modifier = Modifier
                .align(if (cardAtTop) Alignment.TopCenter else Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .border(2.dp, Color(0xFFFFEB3B), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1F1F1F),
            shadowElevation = 8.dp
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "📖 Обучение · шаг ${index + 1}/$total",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFEB3B)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    step.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    step.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPrev,
                        enabled = index > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("← Назад", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text(
                            if (index >= total - 1) "✅ Завершить" else "Далее →",
                            fontSize = 13.sp
                        )
                    }
                    TextButton(onClick = onFinish, modifier = Modifier.weight(0.8f)) {
                        Text("🎓", fontSize = 16.sp)
                    }
                }
                Text(
                    "«🎓» — пропустить обучение",
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}