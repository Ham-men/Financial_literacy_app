package com.example.financialliteracyapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinish = {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.MAIN) {
            MainScreen(
                onOpenBanks    = { navController.navigate(Routes.BANKS) },
                onOpenMarket   = { navController.navigate(Routes.MAP) },
                onOpenKiosk    = { navController.navigate(Routes.KIOSK) },
                onOpenGoals    = { navController.navigate(Routes.GOALS) },
                onOpenQuests   = { navController.navigate(Routes.QUESTS) },
                onOpenReport   = { navController.navigate(Routes.REPORT) },
                onOpenAdult    = { navController.navigate(Routes.ADULT) },
                onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                onOpenReference = { navController.navigate(Routes.REFERENCE) }
            )
        }
        composable(Routes.BANKS) {
            BankScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MAP) {
            MapScreen(
                onBack = { navController.popBackStack() },
                onOpenKiosk = { navController.navigate(Routes.KIOSK) },
                onOpenMarket = { navController.navigate(Routes.MARKET) },
                onOpenGoals = { navController.navigate(Routes.GOALS) },
                onOpenQuests = { navController.navigate(Routes.QUESTS) },
                onOpenReport = { navController.navigate(Routes.REPORT) },
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
            MarketScreen(
                onBack = { navController.popBackStack() },
                onOpenKiosk = { navController.navigate(Routes.KIOSK) }
            )
        }
        composable(Routes.KIOSK) {
            KioskScreen(
                onBack = { navController.popBackStack() },
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
            CashierGame(onFinish = {
                navController.navigate(Routes.REPORT) {
                    popUpTo(Routes.KIOSK) { inclusive = true }
                }
            })
        }
        composable(Routes.REPORT) {
            ReportScreen(
                onNextDay = { navController.navigate(Routes.PET) {
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
    }
}