package com.example.financialliteracyapp.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.financialliteracyapp.data.local.entity.BuildingEntity
import com.example.financialliteracyapp.ui.theme.Primary

/** Район на карте города. Кликабельны: ДОМ, свои магазины, зелёные «продаётся». */
data class Plot(
    val id: String,
    val emoji: String = "",
    val isForSale: Boolean = false,
    val isHome: Boolean = false,
    val isComplex: Boolean = false,
    val miniGrid: List<String>? = null,
    val buildingType: String? = null   // если на этом участке стоит магазин игрока
)

/** Эмодзи магазина игрока по типу здания. */
private fun buildingEmoji(type: String) = when (type) {
    "PRODUCTS" -> "🛒"
    "CONSTRUCTION" -> "🔧"
    "AUTO_SERVICE" -> "🚗"
    else -> "🏪"
}

/** Название магазина игрока по типу здания. */
private fun buildingName(type: String) = when (type) {
    "PRODUCTS" -> "Ларёк"
    "CONSTRUCTION" -> "Стройматериалы"
    "AUTO_SERVICE" -> "СТО"
    else -> "Магазин"
}

/** Карта района «Рынок»: ДОМ и ЛАРЁК в верхнем ряду, остальное — свободные (серые) и продающиеся участки (СТО/стройка покупаются позже). */
private val cityMap = listOf(
    // Row 1 — ДОМ слева, ЛАРЁК справа от него, свободный участок
    Plot(id = "h1", emoji = "🏠", isHome = true),
    Plot(id = "b2", buildingType = "PRODUCTS"),
    Plot(id = "s1", isForSale = true),
    // Row 2 — участки под будущие магазины
    Plot(id = "s2", isForSale = true),
    Plot(id = "s3", isForSale = true),
    Plot(id = "s4", isForSale = true),
    // Row 3
    Plot(id = "g1", emoji = "🌳"),
    Plot(id = "s5", isForSale = true),
    Plot(id = "g2", emoji = "🏢"),
    // Row 4
    Plot(id = "s6", isForSale = true),
    Plot(id = "g3", emoji = "🏠"),
    Plot(id = "g4", emoji = "🌳"),
    // Row 5
    Plot(id = "g5", emoji = "🏢"),
    Plot(id = "s7", isForSale = true),
    Plot(id = "s8", isForSale = true),
    // Row 6
    Plot(id = "s9", isForSale = true),
    Plot(id = "g6", emoji = "🏭"),
    Plot(id = "g7", emoji = "🌳"),
    // Row 7
    Plot(id = "g8", emoji = "🏠"),
    Plot(id = "s10", isForSale = true),
    Plot(id = "g9", emoji = "🏢"),
    // Row 8
    Plot(id = "s11", isForSale = true),
    Plot(id = "g10", emoji = "🌳"),
    Plot(id = "s12", isForSale = true),
    // Row 9
    Plot(id = "g11", emoji = "🏢"),
    Plot(id = "s13", isForSale = true),
    Plot(id = "g12", emoji = "🏠"),
    // Row 10
    Plot(id = "s14", isForSale = true),
    Plot(id = "g13", emoji = "🏭"),
    Plot(id = "s15", isForSale = true),
    // Row 11
    Plot(id = "g14", emoji = "🌳"),
    Plot(id = "s16", isForSale = true),
    Plot(id = "g15", emoji = "🏢"),
    // Row 12
    Plot(id = "s17", isForSale = true),
    Plot(id = "g16", emoji = "🌳"),
    Plot(id = "s18", isForSale = true),
)

@Composable
fun MapScreen(
    onGoHome: () -> Unit,
    onOpenBuilding: (Long) -> Unit,
    onOpenLot: (String) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }

    /// Real buildings of the player from DB
    val buildings: List<BuildingEntity> by repo.observeBuildings().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header - fixed at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Карта района — Рынок",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Map — LazyVerticalGrid scrolls itself (no nested verticalScroll: causes crash)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .background(Color(0xFF1A1A1A))
                .border(width = 4.dp, color = Color(0xFF333333))
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(10.dp)
            ) {
                items(cityMap) { plot ->
                    // ===== Правила кликабельности =====
                    // Стартовые здания привязаны к участку типом (b1/b2/b3),
                    // купленные магазины — по plotId (s1..s16).
                    val ownedBuilding = plot.buildingType?.let { bt ->
                        buildings.firstOrNull { it.type == bt }
                    } ?: buildings.firstOrNull { it.plotId == plot.id }
                    val purchasedHere = plot.isForSale && ownedBuilding?.isPurchased == true

                    val onClick: (() -> Unit)? = when {
                        plot.isHome -> onGoHome
                        ownedBuilding != null -> ({ onOpenBuilding(ownedBuilding.id) })
                        plot.isForSale -> ({ onOpenLot(plot.id) })
                        else -> null   // серый район / нет магазина — не кликабелен
                    }

                    PlotItem(
                        plot = plot,
                        ownedBuilding = ownedBuilding,
                        purchasedHere = purchasedHere,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PlotItem(
    plot: Plot,
    ownedBuilding: BuildingEntity?,
    purchasedHere: Boolean,
    onClick: (() -> Unit)?
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick!!) else Modifier

    val isForSale = plot.isForSale
    val isComplex = plot.isComplex
    val miniGrid = plot.miniGrid

    // Магазин игрока на этом участке (своё здание или купленный магазин)
    val storeEmoji = ownedBuilding?.let { buildingEmoji(it.type) } ?: if (purchasedHere) "🏪" else null
    val storeName = ownedBuilding?.let { buildingName(it.type) } ?: if (purchasedHere) "Магазин" else null

    // == Сложный участок с мини-сеткой жилого двора + магазином игрока ==
    if (isComplex && miniGrid != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White)
                .border(width = 2.dp, color = Color(0xFF555555))
                .then(clickableModifier)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mini grid 2x2 — plain Rows (no nested LazyGrid)
                miniGrid.chunked(2).forEach { rowEmojis ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowEmojis.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
                // Магазин игрока
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .background(Color.Black)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(storeEmoji ?: "🏪", fontSize = 20.sp)
                        storeName?.let {
                            Text(it, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("куплено", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                        }
                    }
                }
            }
        }
        return
    }

    // == Участок с магазином игрока (своё здание) ==
    if (ownedBuilding != null || purchasedHere) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFFFF9C4))
                .border(width = 2.dp, color = if (ownedBuilding != null) Primary else Color(0xFF2E7D32))
                .then(clickableModifier),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(storeEmoji ?: "🏪", fontSize = 32.sp)
                storeName?.let {
                    Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                    Text("куплено", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
        }
        return
    }

    // == ДОМ — всегда кликабелен, возвращает на сцену дома ==
    if (plot.isHome) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White)
                .border(width = 2.dp, color = Color(0xFF555555))
                .clickable(onClick = onClick!!),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏠", fontSize = 32.sp)
                Text("ДОМ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
        return
    }

    // == Участок продаётся — зелёный, кликабелен → сцена покупки ==
    if (isForSale && onClick != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFA3E6A3))
                .border(width = 2.dp, color = Color(0xFF555555))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("продается", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
        return
    }

    // == Остальное (серые, занятые) — НЕ кликабельно ==
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFFE0E0E0))
            .border(width = 2.dp, color = Color(0xFF555555)),
        contentAlignment = Alignment.Center
    ) {
        Text(if (plot.emoji.isEmpty()) "·" else plot.emoji, fontSize = 32.sp)
    }
}