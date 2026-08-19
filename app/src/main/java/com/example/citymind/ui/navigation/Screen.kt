package com.example.citymind.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")

    object Complaints : Screen("complaints")
    object Report : Screen("report")
    object ComplaintDetail : Screen("complaint_detail/{id}") {
        fun createRoute(id: String) = "complaint_detail/$id"
    }
}
