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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.ui.components.AppBottomBar
import com.example.financialliteracyapp.ui.screens.banks.BankScreen
import com.example.financialliteracyapp.ui.screens.journal.JournalScreen
import com.example.financialliteracyapp.ui.screens.lot.LotScreen
import com.example.financialliteracyapp.ui.screens.main.MainScreen
import com.example.financialliteracyapp.ui.screens.adult.AdultScreen
import com.example.financialliteracyapp.ui.screens.kiosk.HireScreen
import com.example.financialliteracyapp.ui.screens.kiosk.KioskScreen
import com.example.financialliteracyapp.ui.screens.map.MapScreen
import com.example.financialliteracyapp.ui.screens.minigames.CashierGame
import com.example.financialliteracyapp.ui.screens.minigames.PricerGame
import com.example.financialliteracyapp.ui.screens.minigames.ShelvesGame
import com.example.financialliteracyapp.ui.screens.minigames.SuppliersGame
import com.example.financialliteracyapp.ui.screens.onboarding.OnboardingScreen
import com.example.financialliteracyapp.ui.screens.pet.PetScreen
import com.example.financialliteracyapp.ui.screens.progress.ProgressScreen
import com.example.financialliteracyapp.ui.screens.quests.QuestsScreen
import com.example.financialliteracyapp.ui.screens.reference.ReferenceScreen
import com.example.financialliteracyapp.ui.screens.report.ReportScreen
import com.example.financialliteracyapp.ui.screens.shop.GoalScreen
import com.example.financialliteracyapp.ui.screens.shop.InventoryScreen
import com.example.financialliteracyapp.ui.screens.shop.MarketScreen
import com.example.financialliteracyapp.ui.screens.shop.PnLScreen
import com.example.financialliteracyapp.ui.screens.shop.ShopScreen

/** Экран без нижней панели: онбординг и полные мини-игры. */
private val HIDDEN_BAR_ROUTES = setOf(
    Routes.ONBOARDING,
    Routes.SUPPLIERS,
    Routes.PRICER,
    Routes.SHELVES,
    Routes.CASHIER
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
                    onBack = { navController.popBackStack() },
                    onOpenLot = { navController.navigate(Routes.LOT) }
                )
            }
            composable(Routes.LOT) {
                LotScreen(
                    onBack = { navController.popBackStack() },
                    onBuildingBuilt = { navController.popBackStack() }
                )
            }
            composable(Routes.MARKET) {
                MarketScreen(onBack = { navController.navigate(Routes.MAIN) })
            }
            composable(Routes.KIOSK) {
                KioskScreen(
                    onBack = { navController.navigate(Routes.MAIN) },
                    onBuyStock = { navController.navigate(Routes.SUPPLIERS) },
                    onOpenPricer = { navController.navigate(Routes.PRICER) },
                    onOpenCashier = { navController.navigate(Routes.CASHIER) },
                    onOpenShelves = { navController.navigate(Routes.SHELVES) },
                    onOpenOffice = { navController.navigate(Routes.SHOP) },
                    onOpenHire = { navController.navigate(Routes.HIRE) }
                )
            }
            composable(Routes.HIRE) {
                HireScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SHOP) {
                ShopScreen(
                    onBack           = { navController.popBackStack() },
                    onOpenJournal    = { navController.navigate(Routes.JOURNAL) },
                    onOpenInventory  = { navController.navigate(Routes.INVENTORY) },
                    onOpenPnL        = { navController.navigate(Routes.PNL) },
                    onSimulateBots   = { navController.navigate(Routes.REPORT) { popUpTo(Routes.SHOP) { inclusive = false } } }
                )
            }
            composable(Routes.SUPPLIERS) {
                SuppliersGame(onFinish = { navController.popBackStack() })
            }
            composable(Routes.INVENTORY) {
                InventoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PNL) {
                PnLScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.GOALS) {
                GoalScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PRICER) {
                PricerGame(onFinish = { navController.popBackStack() })
            }
            composable(Routes.SHELVES) {
                ShelvesGame(onFinish = { navController.popBackStack() })
            }
            composable(Routes.CASHIER) {
                CashierGame(
                    onFinish = {
                        navController.navigate(Routes.REPORT) {
                            popUpTo(Routes.KIOSK) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Routes.REPORT) {
                ReportScreen(
                    onNextDay = { navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    } }
                )
            }
            composable(Routes.JOURNAL) {
                JournalScreen(onFinish = { navController.popBackStack() })
            }
            composable(Routes.QUESTS) {
                QuestsScreen(onBack = { navController.popBackStack() })
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
            composable(Routes.PET) {
                PetScreen(
                    onOpenBanks = { navController.navigate(Routes.BANKS) },
                    onOpenMarket = { navController.navigate(Routes.MARKET) },
                    onOpenKiosk = { navController.navigate(Routes.KIOSK) },
                    onOpenQuests = { navController.navigate(Routes.QUESTS) },
                    onOpenReport = { navController.navigate(Routes.REPORT) }
                )
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