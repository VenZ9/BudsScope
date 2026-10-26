package dev.budsscope.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.budsscope.app.R
import dev.budsscope.app.ui.dashboard.DashboardScreen
import dev.budsscope.app.ui.dashboard.DashboardViewModel
import dev.budsscope.app.ui.history.BatteryHistoryScreen
import dev.budsscope.app.ui.history.BatteryHistoryViewModel
import dev.budsscope.app.ui.player.NowPlayingScreen
import dev.budsscope.app.ui.player.NowPlayingViewModel
import dev.budsscope.app.ui.radar.ReconnectRadarScreen
import dev.budsscope.app.ui.radar.ReconnectRadarViewModel
import dev.budsscope.app.ui.settings.DiagnosticsScreen
import dev.budsscope.app.ui.settings.SettingsScreen
import dev.budsscope.app.ui.settings.SettingsViewModel
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.BgDark
import dev.budsscope.app.ui.theme.BudsScopeTheme
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.SurfaceGraphite
import dev.budsscope.app.ui.theme.TextMuted
import dev.budsscope.app.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val nowPlayingViewModel: NowPlayingViewModel by viewModels()
    private val historyViewModel: BatteryHistoryViewModel by viewModels()
    private val radarViewModel: ReconnectRadarViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        dashboardViewModel.refresh()
        radarViewModel.loadLastSeenTimes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()

        setContent {
            BudsScopeTheme {
                MainAppNavigation(
                    dashboardViewModel = dashboardViewModel,
                    nowPlayingViewModel = nowPlayingViewModel,
                    historyViewModel = historyViewModel,
                    radarViewModel = radarViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.refresh()
        radarViewModel.loadLastSeenTimes()
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}

sealed class Screen(val route: String, val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Default.Headphones)
    object NowPlaying : Screen("now_playing", R.string.nav_now_playing, Icons.Default.MusicNote)
    object History : Screen("history", R.string.nav_battery_history, Icons.Default.Timeline)
    object Radar : Screen("radar", R.string.nav_radar, Icons.Default.BluetoothSearching)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
    object Diagnostics : Screen("diagnostics", R.string.diag_title, Icons.Default.Settings)
}

@Composable
fun MainAppNavigation(
    dashboardViewModel: DashboardViewModel,
    nowPlayingViewModel: NowPlayingViewModel,
    historyViewModel: BatteryHistoryViewModel,
    radarViewModel: ReconnectRadarViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.NowPlaying,
        Screen.History,
        Screen.Radar,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        bottomBar = {
            if (currentRoute != Screen.Diagnostics.route) {
                NavigationBar(
                    containerColor = SurfaceGraphite,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = stringResource(screen.titleRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(screen.titleRes),
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentCyan,
                                selectedTextColor = AccentCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextMuted,
                                indicatorColor = SurfaceGraphite
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToRadar = { navController.navigate(Screen.Radar.route) }
                )
            }
            composable(Screen.NowPlaying.route) {
                NowPlayingScreen(viewModel = nowPlayingViewModel)
            }
            composable(Screen.History.route) {
                BatteryHistoryScreen(viewModel = historyViewModel)
            }
            composable(Screen.Radar.route) {
                ReconnectRadarScreen(viewModel = radarViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) }
                )
            }
            composable(Screen.Diagnostics.route) {
                DiagnosticsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
