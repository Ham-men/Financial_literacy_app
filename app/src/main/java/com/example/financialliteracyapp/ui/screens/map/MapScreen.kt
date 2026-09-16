package com.example.financialliteracyapp.ui.screens.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.theme.*
import kotlin.math.roundToInt

private data class SectorInfo(
    val name: String,
    val color: Color,
    val textColor: Color,
    val icon: String,
    val houses: List<String>,
    val lots: Int,
    val isLocked: Boolean
)

private val sectors = listOf(
    SectorInfo("Богатый", Color(0xFFF9A825), Color(0xFF4E342E), "🏰", listOf("🏠", "🏠", "🏠", "🏰", "🏚️"), 1, isLocked = false),
    SectorInfo("Центр",   Color(0xFF8E24AA), Color.White,           "🏙️", listOf("🏬", "🏦", "🏢", "🏪"), 1, isLocked = false),
    SectorInfo("Рынок",   Color(0xFFFB8C00), Color.White,           "🏪", listOf("🍋", "🧃", "🥕", "🧺"), 2, isLocked = false),
    SectorInfo("Спальный",Color(0xFF43A047), Color.White,           "🏡", listOf("🏠", "🏡", "🌳", "🌲"), 1, isLocked = false),
    SectorInfo("Промзона",Color(0xFF607D8B), Color.White,           "🏭", listOf("🏭", "🏗️", "⛽"), 2, isLocked = true),
    SectorInfo("Техно",   Color(0xFF0288D1), Color.White,           "💻", listOf("💻", "🔭", "🚀"), 3, isLocked = true)
)

@Composable
fun MapScreen(
    onBack: () -> Unit,
    onOpenLot: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val prefs = remember { AppContainer.prefs(context) }
    val vm: MapViewModel = viewModel(factory = MapViewModel.factory(repo))
    val walletState by vm.wallet.collectAsState()
    val selectedDistrict by vm.selectedDistrict.collectAsState()
    val lotPurchased by prefs.lotPurchased.collectAsState(initial = false)

    val cash = (walletState?.cash ?: 500) + (walletState?.needPlan ?: 0) + (walletState?.wantPlan ?: 0)

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1565C0))
    ) {
        // Header как в fragment_map_preview.xml: енот + название + монета-чип
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                color = Color(0x33FFFFFF)
            ) {
                Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp, color = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("🦝", fontSize = 26.sp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Копилкино", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Район: Рынок", fontSize = 11.sp, color = Color(0xFFBBDEFB))
            }
            // монета-чип как bg_coin_chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFE082),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).height(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text("💰", fontSize = 16.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("$cash ₡", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E342E))
                }
            }
        }

        // Доска карты (как bg_map_frame)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            val boardWidth = maxWidth.value / 2f

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Ряд 1: Богатый | Центр
                SectorRow(
                    sectors = listOf(sectors[0], sectors[1]),
                    selectedDistrict = selectedDistrict,
                    onSelect = { vm.onDistrictSelected(it.name) },
                    onOpenLot = onOpenLot,
                    lotPurchased = lotPurchased
                )
                RoadStrip(bot = "🐿️", direction = 1)
                // Ряд 2: Рынок | Спальный
                SectorRow(
                    sectors = listOf(sectors[2], sectors[3]),
                    selectedDistrict = selectedDistrict,
                    onSelect = { vm.onDistrictSelected(it.name) },
                    onOpenLot = onOpenLot,
                    lotPurchased = lotPurchased
                )
                RoadStrip(bot = "🦔", direction = -1)
                // Ряд 3: Промзона | Техно
                SectorRow(
                    sectors = listOf(sectors[4], sectors[5]),
                    selectedDistrict = selectedDistrict,
                    onSelect = { vm.onDistrictSelected(it.name) },
                    onOpenLot = onOpenLot,
                    lotPurchased = lotPurchased
                )
            }

            // Анимированные боты идут по вертикальной дороге
            MovingWorkers(boardWidth = boardWidth)
        }

        // Легенда как в XML
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFFFF))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(14.dp).background(Color(0xFFF9E79F)))
                Text("Богатые районы", fontSize = 11.sp, color = Color(0xFF212121),
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp))
                Box(modifier = Modifier.size(14.dp).background(Color(0xFFB0BEC5)))
                Text("Скромные", fontSize = 11.sp, color = Color(0xFF212121),
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp))
                Text("🏗️", fontSize = 14.sp)
                Text(" Участок на продажу", fontSize = 11.sp, color = Color(0xFF212121))
            }
        }
    }
}

@Composable
private fun SectorRow(
    sectors: List<SectorInfo>,
    selectedDistrict: String?,
    onSelect: (SectorInfo) -> Unit,
    onOpenLot: () -> Unit,
    lotPurchased: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sectors.forEach { sector ->
            Box(modifier = Modifier.weight(1f).height(120.dp)) {
                DistrictTile(
                    sector = sector,
                    isSelected = selectedDistrict == sector.name,
                    onClick = {
                        onSelect(sector)
                        if (sector.name == "Рынок") onOpenLot()
                    }
                )
            }
        }
    }
}

/** Плитка района: бейдж названия сверху, дома, участки снизу — как сектора в XML. */
@Composable
private fun DistrictTile(
    sector: SectorInfo,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(enabled = !sector.isLocked) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (sector.isLocked) sector.color.copy(alpha = 0.3f)
                else if (isSelected) sector.color
                else sector.color.copy(alpha = 0.75f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF4E342E)) else null
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp)) {
            // Дома/товары района — «как в жизни»
            Text(
                sector.houses.joinToString(""),
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            )
            // Иконка района
            Text(
                sector.icon,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 20.dp)
            )
            // Бейдж названия (bg_map_badge)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFFFFF).copy(alpha = 0.92f),
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
            ) {
                Text(
                    sector.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = sector.textColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            // Бейдж участков (map_lot_badge)
            if (!sector.isLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF9A825),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
                ) {
                    Text(
                        "🏗️ Участок ×${sector.lots}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                // Силуэт закрытого района — «скоро»
                Text(
                    "🔒 Скоро",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Метка выбранного района — где сейчас Финни
            if (isSelected) {
                Text("📍", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            }
        }
    }
}

/** Тонкая дорога между рядами + бот, гуляющий по ней. */
@Composable
private fun RoadStrip(bot: String, direction: Int) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Color(0xFFD7CCC8))
    ) {
        val transition = rememberInfiniteTransition(label = "bot_road_$bot")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress"
        )
        val x = if (direction > 0) progress else 1f - progress
        Text(
            bot,
            fontSize = 12.sp,
            modifier = Modifier.offset { IntOffset((x * maxWidth.value).roundToInt(), 0) }
        )
    }
}

/** Боты-работники, идущие по вертикальной дороге между колонками. */
@Composable
private fun MovingWorkers(boardWidth: Float) {
    val transition = rememberInfiniteTransition(label = "workers_move")
    val top by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "w_top"
    )
    val bottom by transition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "w_bottom"
    )
    // Первый работник идёт сверху вниз слева от центра
    Text(
        "🚶",
        fontSize = 14.sp,
        modifier = Modifier.offset {
            IntOffset((boardWidth - 24f).roundToInt(), (top * 300f).roundToInt())
        }
    )
    // Второй — снизу вверх справа от центра
    Text(
        "🐱",
        fontSize = 12.sp,
        modifier = Modifier.offset {
            IntOffset((boardWidth + 16f).roundToInt(), (bottom * 320f).roundToInt())
        }
    )
}