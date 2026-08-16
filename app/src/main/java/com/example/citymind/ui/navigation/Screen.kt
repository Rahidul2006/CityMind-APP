package com.example.citymind.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Report : Screen("report")
    object Complaints : Screen("complaints")
    object Nearby : Screen("nearby")
    object Profile : Screen("profile")
    object ComplaintDetail : Screen("complaint_detail/{id}") {
        fun createRoute(id: String) = "complaint_detail/$id"
    }
}
