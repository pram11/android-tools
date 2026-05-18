package com.armyknife.tools

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.armyknife.tools.core.common.Constants
import com.armyknife.tools.features.dashboard.DashboardScreen
import com.armyknife.tools.features.favorites.FavoritesScreen
import com.armyknife.tools.features.hardware.*
import com.armyknife.tools.features.media.*
import com.armyknife.tools.features.search.SearchScreen
import com.armyknife.tools.features.sensor.*
import com.armyknife.tools.features.utility.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val navItems = listOf(
        NavigationItem(
            route = Constants.ROUTE_DASHBOARD,
            label = stringResource(R.string.nav_dashboard),
            icon = { Icon(Icons.Default.Home, contentDescription = null) }
        ),
        NavigationItem(
            route = Constants.ROUTE_SEARCH,
            label = stringResource(R.string.nav_search),
            icon = { Icon(Icons.Default.Search, contentDescription = null) }
        ),
        NavigationItem(
            route = Constants.ROUTE_FAVORITES,
            label = stringResource(R.string.nav_favorites),
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
        )
    )

    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    var currentRoute by remember { mutableStateOf<String?>(Constants.ROUTE_DASHBOARD) }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            currentRoute = entry.destination.route
        }
    }

    val isToolRoute = currentRoute != Constants.ROUTE_DASHBOARD &&
        currentRoute != Constants.ROUTE_SEARCH &&
        currentRoute != Constants.ROUTE_FAVORITES

    val selectedIndex = navItems.indexOfFirst { it.route == currentRoute }

    // Shared NavHost content
    val navHostContent: @Composable (Modifier) -> Unit = { modifier ->
        NavHost(
            navController = navController,
            startDestination = Constants.ROUTE_DASHBOARD,
            modifier = modifier
        ) {
            composable(Constants.ROUTE_DASHBOARD) {
                DashboardScreen(onToolClick = { navController.navigate(it) })
            }
            composable(Constants.ROUTE_SEARCH) {
                SearchScreen(onToolClick = { navController.navigate(it) })
            }
            composable(Constants.ROUTE_FAVORITES) {
                FavoritesScreen(onToolClick = { navController.navigate(it) })
            }
            composable(Constants.ROUTE_COMPASS) { CompassScreen() }
            composable(Constants.ROUTE_BUBBLE_LEVEL) { BubbleLevelScreen() }
            composable(Constants.ROUTE_SOUND_METER) { SoundMeterScreen() }
            composable(Constants.ROUTE_LUX_METER) { LuxMeterScreen() }
            composable(Constants.ROUTE_METAL_DETECTOR) { MetalDetectorScreen() }
            composable(Constants.ROUTE_SPEEDOMETER) { SpeedometerScreen() }
            composable(Constants.ROUTE_QR_SCANNER) { QrScannerScreen() }
            composable(Constants.ROUTE_MAGNIFIER) { MagnifierScreen() }
            composable(Constants.ROUTE_FLASHLIGHT) { FlashlightScreen() }
            composable(Constants.ROUTE_MIRROR) { MirrorScreen() }
            composable(Constants.ROUTE_APK_EXTRACTOR) { ApkExtractorScreen() }
            composable(Constants.ROUTE_IMAGE_CONVERTER) { ImageConverterScreen() }
            composable(Constants.ROUTE_PDF_UTILITY) { PdfUtilityScreen() }
            composable(Constants.ROUTE_VOICE_RECORDER) { VoiceRecorderScreen() }
            composable(Constants.ROUTE_UNIT_CONVERTER) { UnitConverterScreen() }
            composable(Constants.ROUTE_CALCULATORS) { CalculatorsScreen() }
            composable(Constants.ROUTE_TEXT_CRYPTO) { TextCryptoScreen() }
            composable(Constants.ROUTE_MORSE_CODE) { MorseCodeScreen() }
            composable(Constants.ROUTE_RANDOM_GENERATOR) { RandomGeneratorScreen() }
        }
    }

    if (isWide) {
        Row {
            NavigationRail {
                navItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        selected = index == selectedIndex,
                        onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                        icon = item.icon,
                        label = { Text(item.label) }
                    )
                }
            }
            Scaffold(
                topBar = {
                    if (isToolRoute) {
                        TopAppBar(
                            title = { Text("Tool") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                navHostContent(Modifier.fillMaxSize().padding(innerPadding))
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                            icon = item.icon,
                            label = { Text(item.label) }
                        )
                    }
                }
            },
            topBar = {
                if (isToolRoute) {
                    TopAppBar(
                        title = { Text("Tool") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            navHostContent(Modifier.padding(paddingValues))
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)
