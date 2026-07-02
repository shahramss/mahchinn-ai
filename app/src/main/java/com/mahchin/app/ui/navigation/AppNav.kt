package com.mahchin.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mahchin.app.ui.screens.CalendarScreen
import com.mahchin.app.ui.screens.FinanceScreen
import com.mahchin.app.ui.screens.MonthlyTemplateScreen
import com.mahchin.app.ui.screens.MindMapScreen
import com.mahchin.app.ui.screens.ReportScreen
import com.mahchin.app.ui.screens.SettingsScreen
import com.mahchin.app.ui.screens.TodayScreen
import com.mahchin.app.ui.viewmodel.MainViewModel

sealed class BottomItem(val route: String, val title: String, val icon: @Composable () -> Unit) {
    data object Today : BottomItem("today", "امروز", { Icon(Icons.Default.Today, contentDescription = null) })
    data object Calendar : BottomItem("calendar", "تقویم", { Icon(Icons.Default.CalendarMonth, contentDescription = null) })
    data object Template : BottomItem("template", "قالب", { Icon(Icons.Default.ViewList, contentDescription = null) })
    data object MindMap : BottomItem("mindmap", "مایندمپ", { Icon(Icons.Default.ViewList, contentDescription = null) })
    data object Report : BottomItem("report", "گزارش", { Icon(Icons.Default.Assessment, contentDescription = null) })
    data object Finance : BottomItem("finance", "مالی", { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) })
    data object Settings : BottomItem("settings", "تنظیمات", { Icon(Icons.Default.Settings, contentDescription = null) })
}

@Composable
fun MahChinApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(BottomItem.Today, BottomItem.Calendar, BottomItem.Template, BottomItem.MindMap, BottomItem.Report, BottomItem.Finance, BottomItem.Settings)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = item.icon,
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomItem.Today.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomItem.Today.route) { TodayScreen(viewModel) }
            composable(BottomItem.Calendar.route) { CalendarScreen(viewModel) }
            composable(BottomItem.Template.route) { MonthlyTemplateScreen(viewModel) }
            composable(BottomItem.MindMap.route) { MindMapScreen(viewModel) }
            composable(BottomItem.Report.route) { ReportScreen(viewModel) }
            composable(BottomItem.Finance.route) { FinanceScreen(viewModel) }
            composable(BottomItem.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}
