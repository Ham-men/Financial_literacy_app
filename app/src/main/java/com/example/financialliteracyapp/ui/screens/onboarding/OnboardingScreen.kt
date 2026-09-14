package com.example.financialliteracyapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialliteracyapp.ui.components.BigActionButton
import com.example.financialliteracyapp.ui.theme.Primary
import kotlinx.coroutines.launch

private data class OnboardingPage(val emoji: String, val title: String, val desc: String)

private val pages = listOf(
    OnboardingPage("🦝", "Познакомься с питомцем!",
        "Он мечтает стать предпринимателем. Помоги ему зарабатывать, копить и вкладывать."),
    OnboardingPage("🏪", "Управляй магазином",
        "Сам закупай товар, ставь цены и обслуживай покупателей."),
    OnboardingPage("💰", "Распределяй монеты",
        "Тратить, копить или вкладывать — решай сам и смотри на результат.")
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(page.emoji, fontSize = 120.sp)
                Spacer(Modifier.height(24.dp))
                Text(page.title, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text(page.desc, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center)
            }
        }

        // Точки-индикаторы
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            repeat(pages.size) { i ->
                val active = i == pagerState.currentPage
                Box(
                    Modifier
                        .size(if (active) 12.dp else 8.dp)
                        .background(
                            if (active) Primary else Color(0xFFCCCCCC),
                            CircleShape
                        )
                )
            }
        }

        BigActionButton(
            text = if (pagerState.currentPage == pages.lastIndex) "Начать →" else "Далее →",
            color = Primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (pagerState.currentPage == pages.lastIndex) {
                onFinish()
            } else {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }
}