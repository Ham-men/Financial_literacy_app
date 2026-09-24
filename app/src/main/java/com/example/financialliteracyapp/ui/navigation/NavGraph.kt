package com.example.financialliteracyapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.local.entity.BuildingEntity
import com.example.financialliteracyapp.ui.components.NavSidebar
import com.example.financialliteracyapp.ui.screens.adult.AdultScreen
import com.example.financialliteracyapp.ui.screens.banks.BankScreen
import com.example.financialliteracyapp.ui.screens.building.AutoServiceInteriorScreen
import com.example.financialliteracyapp.ui.screens.building.BuildingCashierScreen
import com.example.financialliteracyapp.ui.screens.building.CleaningGameScreen
import com.example.financialliteracyapp.ui.screens.building.ConstructionInteriorScreen
import com.example.financialliteracyapp.ui.screens.building.ProductsInteriorScreen
import com.example.financialliteracyapp.ui.screens.food.FoodScreen
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

/** Полноэкранные экраны без боковой панели: онбординг и чистые мини-игры. */
private val HIDDEN_BAR_ROUTES = setOf(
    Routes.ONBOARDING,
    Routes.PRICER,
    Routes.SHELVES,
    Routes.CLEANING
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    val context = LocalContext.current
    val prefs = remember { AppContainer.prefs(context) }
    val onboardingDone by prefs.onboardingDone.collectAsState(initial = null)

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

    Row(Modifier.fillMaxSize()) {
        // Left navigation sidebar - always visible on main screens
        if (showBar) {
            NavSidebar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navigateToTab(navController, route)
                }
            )
        }

        // Main content area
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (showBar) 70.dp else 0.dp)
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
                    onOpenBuilding = { id -> navController.navigate("${Routes.BUILDING}/$id") },
                    onOpenLot = { plotId -> navController.navigate("${Routes.LOT}/$plotId") }
                )
            }
            composable(
                route = "${Routes.LOT}/{plotId}",
                arguments = listOf(navArgument("plotId") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val plotId = backStackEntry.arguments?.getString("plotId") ?: ""
                LotScreen(
                    plotId = plotId,
                    onBack = { navController.popBackStack() },
                    onBuildingBuilt = { navController.popBackStack() }
                )
            }
            // Kiosk - the "ЛАРЁК" tab now opens the products building interior (СТО-style scene)
            composable(Routes.KIOSK) {
                val ctx = LocalContext.current
                val kRepo = remember { AppContainer.repo(ctx) }
                val kBuildings: List<BuildingEntity> by kRepo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
                val kBuilding = kBuildings.firstOrNull { it.type == "PRODUCTS" }
                if (kBuilding != null) {
                    ProductsInteriorScreen(
                        buildingId = kBuilding.id,
                        onExit = { navController.navigate(Routes.MAIN) },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/${kBuilding.id}") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                }
            }
            // Building interior - routes to specialized screens based on building type
            composable(
                route = "${Routes.BUILDING}/{buildingId}",
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType })
            ) { backStackEntry ->
                val buildingId = backStackEntry.arguments?.getLong("buildingId") ?: 0L
                val context = LocalContext.current
                val repo = remember { AppContainer.repo(context) }
                val buildings: List<BuildingEntity> by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
                val building = buildings.firstOrNull { it.id == buildingId }

                val buildingType = building?.type ?: "PRODUCTS"

                when (buildingType) {
                    "PRODUCTS" -> ProductsInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                    "CONSTRUCTION" -> ConstructionInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                    "AUTO_SERVICE" -> AutoServiceInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                    else -> ProductsInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                }
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
            composable(Routes.FOOD) {
                FoodScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CONSTRUCTION) {
                val context = LocalContext.current
                val repo = remember { AppContainer.repo(context) }
                val buildings: List<BuildingEntity> by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
                val building = buildings.firstOrNull { it.type == "CONSTRUCTION" }
                if (building != null) {
                    ConstructionInteriorScreen(
                        buildingId = building.id,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/${building.id}") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                }
            }
            composable(Routes.AUTO_SERVICE) {
                val context = LocalContext.current
                val repo = remember { AppContainer.repo(context) }
                val buildings: List<BuildingEntity> by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
                val building = buildings.firstOrNull { it.type == "AUTO_SERVICE" }
                if (building != null) {
                    AutoServiceInteriorScreen(
                        buildingId = building.id,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/${building.id}") },
                        onOpenHire = { navController.navigate(Routes.HIRE) }
                    )
                }
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