package com.example.financialliteracyapp.ui.screens.building

import androidx.compose.foundation.background
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
import com.example.financialliteracyapp.ui.components.AppCard
import com.example.financialliteracyapp.ui.components.Chip
import com.example.financialliteracyapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Мини-игра «Уборка» (День 12): тапай по грязи, пока боты довольны. Грязь возвращается. */
@Composable
fun CleaningGameScreen(
    buildingId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { AppContainer.repo(context) }
    val scope = rememberCoroutineScope()

    val buildings by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
    val building = buildings.firstOrNull { it.id == buildingId }
    val wallet by repo.observeWallet().collectAsState(initial = null)
    val cash = wallet?.cash ?: 500

    var dirtNow by remember { mutableIntStateOf(((building?.dirtLevel ?: 20) / 8).coerceIn(2, 12)) }
    var cleaned by remember { mutableIntStateOf(0) }
    var rewarded by remember { mutableStateOf(false) }
    val cleanFlash = remember { mutableStateListOf<Int>() }

    // Грязь накапливается со временем — игра на скорость
    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            if (dirtNow < 12) {
                dirtNow++
            }
            scope.launch { repo.updateDirtLevel(buildingId, dirtNow * 8) }
        }
    }

    // Отражаем грязь в БД и ловим момент «всё чисто»
    LaunchedEffect(dirtNow) {
        scope.launch { repo.updateDirtLevel(buildingId, dirtNow * 8) }
        if (dirtNow == 0 && cleaned >= 6 && !rewarded) {
            rewarded = true
            scope.launch {
                repo.rewardCleaning(40)
                repo.updateDirtLevel(buildingId, 0)
            }
        }
    }

    fun cleanTile(index: Int) {
        if (index >= dirtNow) return
        dirtNow--
        cleaned++
        cleanFlash.add(index)
        scope.launch {
            delay(500)
            cleanFlash.remove(index)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Заголовок
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text("◀", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text("🧹 Уборка", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Chip("💰 $cash ₡")
        }

        Spacer(Modifier.height(8.dp))
        Text(
            if (rewarded) "✨ Чисто! Грязь снова возвращается — следи за полом."
            else "Тапай по 💩, пока боты не ушли. Грязь накапливается со временем: работай быстро!",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Чистота пола (шкала из тайлов)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            userScrollEnabled = false,
            modifier = Modifier.height(380.dp)
        ) {
            items((0 until 16).toList()) { index ->
                val isDirty = index < dirtNow
                val flashing = cleanFlash.contains(index)
                Surface(
                    onClick = { cleanTile(index) },
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        flashing -> Color(0xFFFFF59D)
                        isDirty -> Color(0xFF6D4C41)
                        else -> Color(0xFFE0E0E0)
                    },
                    modifier = Modifier.padding(4.dp).height(84.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            when {
                                flashing -> "✨"
                                isDirty -> "💩"
                                else -> "·"
                            },
                            fontSize = if (isDirty) 34.sp else 20.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            Text("💩 грязи: $dirtNow/12", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = if (dirtNow > 6) Danger else TextSecondary)
            Text("🧾 убрано: $cleaned")
        }
        Spacer(Modifier.height(8.dp))
        if (!rewarded && cleaned >= 6 && dirtNow == 0) {
            AppCard {
                Text("🎉 Пол вычищен! +40 ₡ за помощь. (Награда уже домечена.)", fontSize = 13.sp, color = Primary)
            }
        }
    }
}