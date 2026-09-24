package com.example.financialliteracyapp.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.ui.theme.Primary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class Plot(
    val emoji: String,
    val isForSale: Boolean = false,
    val label: String? = null,
    val isComplex: Boolean = false,
    val miniGrid: List<String>? = null
)

private val cityMap = listOf(
    // Row 1
    Plot("🏠", label = "ДОМ"),
    Plot("", isForSale = true),
    Plot("", isForSale = true),
    // Row 2
    Plot("🏢"),
    Plot("", isComplex = true, miniGrid = listOf("🏠", "🏠", "🏠", "🏠"), label = "🏪"),
    Plot("🏭"),
    // Row 3
    Plot("🌳"),
    Plot("", isForSale = true),
    Plot("🏢"),
    // Row 4
    Plot("", isForSale = true),
    Plot("🏠"),
    Plot("🌳"),
    // Row 5
    Plot("🏢"),
    Plot("", isForSale = true),
    Plot("", isForSale = true),
    // Row 6
    Plot("", isForSale = true),
    Plot("🏭"),
    Plot("🌳"),
    // Row 7
    Plot("🏠"),
    Plot("", isForSale = true),
    Plot("🏢"),
    // Row 8
    Plot("", isForSale = true),
    Plot("🌳"),
    Plot("", isForSale = true),
    // Row 9
    Plot("🏢"),
    Plot("", isForSale = true),
    Plot("🏠"),
    // Row 10
    Plot("", isForSale = true),
    Plot("🏭"),
    Plot("", isForSale = true),
    // Row 11
    Plot("🌳"),
    Plot("", isForSale = true),
    Plot("🏢"),
    // Row 12
    Plot("", isForSale = true),
    Plot("🏠"),
    Plot("", isForSale = true),
)

@Composable
fun MapScreen(
    onGoHome: () -> Unit,
    onOpenBuilding: (Long) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()

    val wallet by repo.observeWallet().collectAsState(initial = null)
    val cash = wallet?.cash ?: 500

    // Real buildings from DB — used to map plot taps to buildings
    val buildings by repo.observeBuildings().collectAsState(initial = emptyList())
    fun buildingIdFor(type: String): Long? =
        buildings.firstOrNull { it.type == type }?.id

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
                "Дата 01.01.2020 12:00",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("нужное  \\  желаемое  \\  копилка", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("100  \\  200  \\  200", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
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
                    val targetId = when {
                        plot.label == "🏪" -> buildingIdFor("PRODUCTS")
                        plot.emoji == "🛒" -> buildingIdFor("PRODUCTS")
                        else -> buildingIdFor("PRODUCTS")
                    }
                    PlotItem(plot = plot, onClick = { targetId?.let(onOpenBuilding) })
                }
            }
        }
    }
}

@Composable
private fun PlotItem(
    plot: Plot,
    onClick: () -> Unit
) {
    val isForSale = plot.isForSale
    val isComplex = plot.isComplex
    val miniGrid = plot.miniGrid
    val label = plot.label

    if (isComplex && miniGrid != null) {
        // Complex plot with mini grid + shop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White)
                .border(width = 2.dp, color = Color(0xFF555555))
                .clickable(onClick = onClick)
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
                // Shop emoji
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .background(Color.Black)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label ?: "🏪", fontSize = 26.sp)
                }
            }
        }
    } else if (isForSale) {
        // For sale plot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFA3E6A3))
                .border(width = 2.dp, color = Color(0xFF555555))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text("продается", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
        }
    } else if (label != null) {
        // Plot with label (HOME)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White)
                .border(width = 2.dp, color = Color(0xFF555555))
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp, start = 6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(plot.emoji, fontSize = 32.sp)
                }
            }
        }
    } else {
        // Regular plot with emoji
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFE0E0E0))
                .border(width = 2.dp, color = Color(0xFF555555))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(plot.emoji, fontSize = 32.sp)
        }
    }
}