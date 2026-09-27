package com.example.financialliteracyapp.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.data.clock.GameClock
import com.example.financialliteracyapp.data.local.entity.BuildingEntity
import com.example.financialliteracyapp.domain.economy.GameRules
import com.example.financialliteracyapp.game.PassiveSales
import com.example.financialliteracyapp.ui.components.NavSidebar
import com.example.financialliteracyapp.ui.components.TimeBankBar
import com.example.financialliteracyapp.ui.screens.adult.AdultScreen
import com.example.financialliteracyapp.ui.screens.appearance.AppearanceScreen
import com.example.financialliteracyapp.ui.screens.journal.JournalScreen
import com.example.financialliteracyapp.ui.screens.banks.BankScreen
import com.example.financialliteracyapp.ui.screens.building.AutoServiceInteriorScreen
import com.example.financialliteracyapp.ui.screens.building.BuildingCashierScreen
import com.example.financialliteracyapp.ui.screens.building.CleaningGameScreen
import com.example.financialliteracyapp.ui.screens.building.ConstructionInteriorScreen
import com.example.financialliteracyapp.ui.screens.building.ProductsInteriorScreen
import com.example.financialliteracyapp.ui.screens.entertainment.EntertainmentScreen
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
import com.example.financialliteracyapp.ui.tutorial.TutorialOverlay
import com.example.financialliteracyapp.ui.tutorial.TutorialViewModel

/** Полноэкранные экраны без боковой панели: онбординг и чистые мини-игры. */
private val HIDDEN_BAR_ROUTES = setOf(
    Routes.ONBOARDING,
    Routes.PRICER,
    Routes.SHELVES,
    Routes.CLEANING,
    "${Routes.CLEANING}/{buildingId}"
)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.ONBOARDING
) {
    val context = LocalContext.current
    val prefs = remember { AppContainer.prefs(context) }
    val onboardingDone by prefs.onboardingDone.collectAsState(initial = null)
    val tutorialDone by prefs.tutorialDone.collectAsState(initial = null)

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

    val configuration = LocalConfiguration.current
    val landscape = remember(configuration) { configuration.screenWidthDp >= configuration.screenHeightDp }

    val repo = remember { AppContainer.repo(context) }
    val wallet by repo.observeWallet().collectAsState(initial = null)
    val day by prefs.currentDay.collectAsState(initial = 1)
    val buildings: List<BuildingEntity> by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())

    // Игровые часы: 1 игр.мин = 1 реал.сек
    LaunchedEffect(Unit) {
        GameClock.start(context)
        PassiveSales.start(context)
    }
    val gameMinute by GameClock.minute.collectAsState()

    // День из prefs → репозиторий: транзакции и периоды пишутся с правильным днём.
    LaunchedEffect(day) {
        repo.syncDay(day)
    }

    // ===== Обучение при первом запуске =====
    val tutorialVm: TutorialViewModel = viewModel()
    val tutorialActive by tutorialVm.active.collectAsState()
    val tutorialStep by tutorialVm.currentStep.collectAsState()
    val tutorialIndex by tutorialVm.index.collectAsState()
    val tutorialSteps by tutorialVm.steps.collectAsState()
    val scope = rememberCoroutineScope()

    val activity = remember(context) { context.findActivity() }

    // Во время обучения — только портрет (переворот заблокирован); после — свободный поворот.
    LaunchedEffect(tutorialActive) {
        val act = activity
        if (act != null) {
            act.requestedOrientation = if (tutorialActive)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Автозапуск после онбординга при первом входе в игру
    LaunchedEffect(start, tutorialDone) {
        if (start == Routes.MAIN && tutorialDone == false) {
            tutorialVm.start()
        }
    }

    // Автонавигация по сценам обучения
    LaunchedEffect(tutorialActive, tutorialStep) {
        val step = tutorialStep
        if (!tutorialActive || step == null) return@LaunchedEffect
        val productsBuilding = buildings.firstOrNull { it.type == "PRODUCTS" } ?: buildings.firstOrNull()
        val fullRoute = when (step.route) {
            Routes.SUPPLIERS, Routes.CASHIER -> productsBuilding?.let { "${step.route}/${it.id}" }
            else -> step.route
        } ?: return@LaunchedEffect
        val pattern = when (step.route) {
            Routes.SUPPLIERS, Routes.CASHIER -> "${step.route}/{buildingId}"
            else -> step.route
        }
        val current = navController.currentDestination?.route
        if (current != pattern && current != step.route) {
            navigateToTab(navController, fullRoute)
        }
    }

    Box(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth >= maxHeight

            // Контент (TimeBankBar + NavHost) — единый для обеих ориентаций
            val content: @Composable () -> Unit = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    // Global HUD: дата, время, банки с зелёной подсветкой тратной на текущей сцене
                    if (showBar) {
                        TimeBankBar(
                            route = currentRoute,
                            day = day,
                            gameMinute = gameMinute,
                            wallet = wallet
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                    onOpenAppearance = { navController.navigate(Routes.APPEARANCE) }
                )
            }
            composable(Routes.BANKS) {
                BankScreen()
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
                    onBuildingBuilt = { navController.popBackStack() },
                    onOpenShop = { id -> navController.navigate("${Routes.BUILDING}/$id") }
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
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/${kBuilding.id}") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/${kBuilding.id}") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/${kBuilding.id}") }
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
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/$buildingId") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/$buildingId") }
                    )
                    "CONSTRUCTION" -> ConstructionInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/$buildingId") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/$buildingId") }
                    )
                    "AUTO_SERVICE" -> AutoServiceInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/$buildingId") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/$buildingId") }
                    )
                    else -> ProductsInteriorScreen(
                        buildingId = buildingId,
                        onExit = { navController.popBackStack() },
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/$buildingId") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/$buildingId") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/$buildingId") }
                    )
                }
            }
            composable(
                route = "${Routes.SUPPLIERS}/{buildingId}",
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val bid = backStackEntry.arguments?.getLong("buildingId") ?: 0L
                SuppliersGame(onFinish = { navController.popBackStack() }, buildingId = bid)
            }
            composable(Routes.PRICER) {
                PricerGame(onFinish = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.HIRE}/{buildingId}",
                arguments = listOf(navArgument("buildingId") { type = NavType.LongType; defaultValue = 0L })
            ) { backStackEntry ->
                val bid = backStackEntry.arguments?.getLong("buildingId") ?: 0L
                val context = LocalContext.current
                val repo = remember { AppContainer.repo(context) }
                val buildings: List<BuildingEntity> by repo.observeBuildingsByDistrict("Рынок").collectAsState(initial = emptyList())
                val bld = buildings.firstOrNull { it.id == bid }
                HireScreen(
                    onBack = { navController.popBackStack() },
                    buildingId = bid,
                    buildingName = bld?.let {
                        when (it.type) {
                            "CONSTRUCTION" -> "стройка"
                            "AUTO_SERVICE" -> "СТО"
                            else -> "ларёк"
                        }
                    } ?: "магазин"
                )
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
                GoalHubScreen()
            }
            composable(Routes.JOURNAL) {
                JournalScreen(onFinish = { navController.popBackStack() })
            }
            composable(Routes.ENTERTAINMENT) {
                EntertainmentScreen()
            }
            composable(Routes.APPEARANCE) {
                AppearanceScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REPORT) {
                ReportScreen()
            }
            composable(Routes.PROGRESS) {
                ProgressScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REFERENCE) {
                ReferenceScreen()
            }
            composable(Routes.ADULT) {
                AdultScreen(
                    onRestartTutorial = {
                        scope.launch { prefs.setTutorialDone(false) }
                        tutorialVm.start()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
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
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/${building.id}") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/${building.id}") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/${building.id}") }
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
                        onOpenSuppliers = { navController.navigate("${Routes.SUPPLIERS}/${building.id}") },
                        onOpenCashier = { navController.navigate("${Routes.CASHIER}/${building.id}") },
                        onOpenHire = { navController.navigate("${Routes.HIRE}/${building.id}") }
                    )
                }
            }
            }
            }
            }

            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    // Left navigation sidebar in landscape
                    if (showBar) {
                        NavSidebar(
                            currentRoute = currentRoute,
                            onNavigate = { route -> navigateToTab(navController, route) }
                        )
                    }
                    Column(Modifier.fillMaxHeight().weight(1f)) {
                        content()
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    // Content on top, nav panel at the bottom in portrait
                    Column(Modifier.fillMaxWidth().weight(1f)) {
                        content()
                    }
                    if (showBar) {
                        NavSidebar(
                            currentRoute = currentRoute,
                            onNavigate = { route -> navigateToTab(navController, route) },
                            modifier = Modifier.fillMaxWidth(),
                            bottomBar = true
                        )
                    }
                }
            }
        }

        // Уведомление внизу экрана: 23:00 — пора ложиться спать
        if (showBar && GameRules.isBedtime(gameMinute) && currentRoute != Routes.MAIN) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        horizontal = 14.dp,
                        vertical = if (landscape) 10.dp else 66.dp
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF37474F),
                border = BorderStroke(2.dp, Color(0xFF80DEEA)),
                onClick = { navigateToTab(navController, Routes.MAIN) }
            ) {
                Text(
                    "😴 ${GameRules.timeLabel(gameMinute)} — пора ложиться спать! " +
                        "Нажми на кровать 🛏️, чтобы начать новый день",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Обучение при первом запуске — поверх всех сцен
        val step = tutorialStep
        if (tutorialActive && step != null) {
            TutorialOverlay(
                step = step,
                index = tutorialIndex,
                total = tutorialSteps.size,
                onPrev = { tutorialVm.prev() },
                onNext = {
                    if (tutorialIndex >= tutorialSteps.lastIndex) {
                        scope.launch { prefs.setTutorialDone(true) }
                        tutorialVm.finish()
                        navController.navigate(Routes.MAIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        tutorialVm.next()
                    }
                },
                onFinish = {
                    scope.launch { prefs.setTutorialDone(true) }
                    tutorialVm.finish()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
private fun navigateToTab(navController: NavHostController, route: String) {
    if (navController.currentDestination?.route == route) return
    navController.navigate(route) {
        // Всегда возвращаемся к корню (MAIN) и открываем сцену заново,
        // без restoreState — иначе из открытого в карте магазина кнопка
        // «КАРТА» возвращала не на карту, а в восстановленное состояние магазина.
        popUpTo(Routes.MAIN)
        launchSingleTop = true
    }
}

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}