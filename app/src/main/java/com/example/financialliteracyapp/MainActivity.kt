package com.example.financialliteracyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.game.PetDecayWorker
import com.example.financialliteracyapp.ui.navigation.AppNavGraph
import com.example.financialliteracyapp.ui.theme.MiniEconomyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Day 2: seed Room (питомец, кошелёк, магазин, 20 ботов)
        CoroutineScope(Dispatchers.IO).launch {
            AppContainer.repo(this@MainActivity).ensureSeed()
        }

        // Day 4: падение шкал раз в час
        val decayRequest = PeriodicWorkRequestBuilder<PetDecayWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "pet_decay",
            ExistingPeriodicWorkPolicy.KEEP,
            decayRequest
        )

        enableEdgeToEdge()
        // Полноэкранный режим: скрыть системные панели (назад/домой/список)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MiniEconomyTheme {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavGraph(navController)
                }
            }
        }
    }
}