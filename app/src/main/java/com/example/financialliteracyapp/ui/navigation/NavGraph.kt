package com.example.financialliteracyapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.components.AppBottomBar
import com.example.financialliteracyapp.ui.screens.adult.AdultScreen
import com.example.financialliteracyapp.ui.screens.banks.BankScreen
import com.example.financialliteracyapp.ui.screens.building.BuildingCashierScreen
import com.example.financialliteracyapp.ui.screens.building.BuildingInteriorScreen
import com.example.financialliteracyapp.ui.screens.building.CleaningGameScreen
import com.example.financialliteracyapp.ui.screens.goals.GoalHubScreen
import com.example.financialliteracyapp.ui.screens.kiosk.HireScreen
import com.example.financialliteracyapp.ui.screens.kiosk.KioskScreen
import com.example.financialliteracyapp.ui.screens.lot.LotScreen
import com.example.financialliteracyapp.ui.screens.main.MainScreen
import com.example.financialliteracyapp.ui.screens.map.MapScreen
import com.example.financialliteracyapp.ui.screens.minigames.PricerGame
import com.example.financialliteracyapp.ui.screens.minigames.ShelvesGame
import com.example.financialliteracyapp.ui.screens.minigames.SuppliersGame
import com.example.financialliteracyapp.ui.screens.onboarding.OnboardingScreen
import com.example.financialliteracyapp.ui.screens.progress.ProgressScreen
import com.example.financialliteracyapp.ui.screens.reference.ReferenceScreen
import com.example.financialliteracyapp.ui.screens.report.ReportScreen
import com.example.financialliteracyapp.ui.screens.shop.AccountingScreen

/** Экран без нижней панели: онбординг, карта и полноэкранные мини-игры. */
private val HIDDEN_BAR_ROUTES = setOf(
    Routes.ONBOARDING,
    Routes.MAP,
    Routes.BUILDING,
    Routes.SHELVES,
    Routes.SUPPLIERS,
    Routes.PRICER,
    Routes.CASHIER,
    Routes.CLEANING,
    Routes.HIRE
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    val context = LocalContext.current
    val prefs = remember { AppContainer.prefs(context) }
    val onboardingDone by prefs.onboardingDone.collectAsState(initial = null)

    // Стартуем с онбординга только при первом запуске. Дальше — сразу на главную.
    var effectiveStart by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(onboardingDone) {
        if (onboardingDone != null) {
            effectiveStart = if (onboardingDone == true) Routes.MAIN else startDestination
        }
    }

    val start = effectiveStart
    if (start == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = currentRoute != null && currentRoute !in HIDDEN_BAR_ROUTES

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateToTab(navController, route) }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onFinish = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(Routes.MAIN) {
                MainScreen(
                    onOpenMap       = { navController.navigate(Routes.MAP) },
                    onOpenKiosk     = { navController.navigate(Routes.KIOSK) },
                    onOpenProgress  = { navController.navigate(Routes.PROGRESS) },
                    onOpenReference = { navController.navigate(Routes.REFERENCE) },
                    onOpenAdult     = { navController.navigate(Routes.ADULT) }
                )
            }
            composable(Routes.BANKS) {
                BankScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.MAP) {
                MapScreen(
                    onGoHome = { navController.navigate(Routes.MAIN) },
                    onOpenBuilding = { id -> navController.navigate("${Routes.BUILDING}/$id") }
                )
            }
            composable(Routes.LOT) {
                LotScreen(
                    onBack = { navController.popBackStack() },
                    onBuildingBuilt = { navController.popBackStack() }
                )
            }
            composable(Routes.KIOSK) {
                KioskScreen(
                    onBack = { navController.navigate(Routes.MAIN) },
                    onOpenReport = {
                        navController.navigate(Routes.REPORT) {
                            popUpTo(Routes.KIOSK) { inclusive = false }
                        }
                    },
                    onOpenShelves = { navController.navigate(Routes.SHELVES) }
                )
            }
            composable(
                route = "${Routes.BUILDING}/{buildingId}",
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val buildingId = backStackEntry.arguments?.getLong("buildingId") ?: 0L
                BuildingInteriorScreen(
                    buildingId = buildingId,
                    onExit = { navController.popBackStack() },
                    onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                    onOpenShelves = { navController.navigate(Routes.SHELVES) },
                    onOpenPricer = { navController.navigate(Routes.PRICER) },
                    onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                    onOpenCleaning = { navController.navigate("${Routes.CLEANING}/$buildingId") },
                    onOpenHire = { navController.navigate(Routes.HIRE) },
                    onOpenUpgrades = { navController.popBackStack() },
                    onOpenAccounting = { navController.navigate(Routes.SHOP) }
                )
            }
            composable(Routes.SUPPLIERS) {
                SuppliersGame(onFinish = { navController.popBackStack() })
            }
            composable(Routes.PRICER) {
                PricerGame(onFinish = { navController.popBackStack() })
            }
            composable(Routes.HIRE) {
                HireScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.CASHIER}/{buildingId}",
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cid = backStackEntry.arguments?.getLong("buildingId") ?: 0L
                BuildingCashierScreen(
                    buildingId = cid,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "${Routes.CLEANING}/{buildingId}",
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val cid = backStackEntry.arguments?.getLong("buildingId") ?: 0L
                CleaningGameScreen(
                    buildingId = cid,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SHOP) {
                AccountingScreen(
                    onBack          = { navController.popBackStack() },
                    onOpenReport    = {
                        navController.navigate(Routes.REPORT) {
                            popUpTo(Routes.SHOP) { inclusive = false }
                        }
                    }
                )
            }
            composable(Routes.SHELVES) {
                ShelvesGame(onFinish = { navController.popBackStack() })
            }
            composable(Routes.GOALS) {
                GoalHubScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REPORT) {
                ReportScreen(
                    onNextDay = { navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    } }
                )
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REFERENCE) {
                ReferenceScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ADULT) {
                AdultScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Переход по вкладке: чистим стек до Дома, сингл-топ, сохраняем состояние. */
private fun navigateToTab(navController: NavHostController, route: String) {
    if (navController.currentDestination?.route == route) return
    navController.navigate(route) {
        popUpTo(Routes.MAIN) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}