package com.armyknife.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.armyknife.tools.core.common.Constants
import com.armyknife.tools.features.dashboard.DashboardScreen
import com.armyknife.tools.features.favorites.FavoritesScreen
import com.armyknife.tools.features.search.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Adaptive navigation: Bottom nav on small screens, Rail on wide screens
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

    val wideThreshold = 600.dp
    val isWide = LocalConfiguration.current.screenWidthDp >= wideThreshold

    val currentRoute by navController.currentRouteAsState()
    val selectedIndex = navItems.indexOfFirst { it.route == currentRoute }

    if (isWide) {
        NavigationRailScaffold(
            rail = {
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
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Constants.ROUTE_DASHBOARD,
                modifier = Modifier.padding(it)
            ) {
                composable(Constants.ROUTE_DASHBOARD) { DashboardScreen() }
                composable(Constants.ROUTE_SEARCH) { SearchScreen() }
                composable(Constants.ROUTE_FAVORITES) { FavoritesScreen() }
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
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Constants.ROUTE_DASHBOARD,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Constants.ROUTE_DASHBOARD) { DashboardScreen() }
                composable(Constants.ROUTE_SEARCH) { SearchScreen() }
                composable(Constants.ROUTE_FAVORITES) { FavoritesScreen() }
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)
