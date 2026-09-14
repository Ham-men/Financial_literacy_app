package com.example.financialliteracyapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.financialliteracyapp.ui.screens.banks.BankScreen
import com.example.financialliteracyapp.ui.screens.journal.JournalScreen
import com.example.financialliteracyapp.ui.screens.minigames.CashierGame
import com.example.financialliteracyapp.ui.screens.minigames.PricerGame
import com.example.financialliteracyapp.ui.screens.minigames.SuppliersGame
import com.example.financialliteracyapp.ui.screens.onboarding.OnboardingScreen
import com.example.financialliteracyapp.ui.screens.pet.PetScreen
import com.example.financialliteracyapp.ui.screens.quests.QuestsScreen
import com.example.financialliteracyapp.ui.screens.report.ReportScreen
import com.example.financialliteracyapp.ui.screens.shop.ShopScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinish = {
                navController.navigate(Routes.PET) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.PET) {
            PetScreen(
                onOpenBanks    = { navController.navigate(Routes.BANKS) },
                onOpenShop     = { navController.navigate(Routes.SHOP) },
                onOpenQuests   = { navController.navigate(Routes.QUESTS) },
                onOpenReport   = { navController.navigate(Routes.REPORT) }
            )
        }
        composable(Routes.BANKS) {
            BankScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SHOP) {
            ShopScreen(
                onBack           = { navController.popBackStack() },
                onBuyStock       = { navController.navigate(Routes.SUPPLIERS) },
                onOpenPricer     = { navController.navigate(Routes.PRICER) },
                onOpenCashier    = { navController.navigate(Routes.CASHIER) },
                onOpenJournal    = { navController.navigate(Routes.JOURNAL) },
                onSimulateBots   = { navController.navigate(Routes.REPORT) { popUpTo(Routes.SHOP) { inclusive = false } } }
            )
        }
        composable(Routes.SUPPLIERS) {
            SuppliersGame(onFinish = { navController.popBackStack() })
        }
        composable(Routes.PRICER) {
            PricerGame(onFinish = { navController.popBackStack() })
        }
        composable(Routes.CASHIER) {
            CashierGame(onFinish = {
                navController.navigate(Routes.REPORT) {
                    popUpTo(Routes.SHOP) { inclusive = true }
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
    }
}