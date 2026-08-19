package com.example.citymind.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.citymind.ui.home.HomeScreen
import com.example.citymind.ui.report.ReportScreen
import com.example.citymind.ui.complaints.ComplaintsScreen
import com.example.citymind.ui.complaints.ComplaintDetailScreen
import com.example.citymind.ui.navigation.Screen
import com.example.citymind.viewmodel.ReportViewModel
import com.example.citymind.viewmodel.ComplaintViewModel

@Composable
fun MainScreen(
    reportViewModel: ReportViewModel,
    complaintViewModel: ComplaintViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = remember {
        listOf(
            Triple(Screen.Home, "Home", Icons.Default.Home),
            Triple(Screen.Report, "Report", Icons.Default.Add),
            Triple(Screen.Complaints, "Complaints", Icons.Default.List),
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { (screen, label, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(navController, complaintViewModel) 
            }
            composable(Screen.Report.route) {
                ReportScreen(navController, reportViewModel)
            }
            composable(Screen.Complaints.route) { 
                ComplaintsScreen(navController, complaintViewModel) 
            }
            composable(Screen.ComplaintDetail.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                ComplaintDetailScreen(id, navController, complaintViewModel)
            }
        }
    }
}
