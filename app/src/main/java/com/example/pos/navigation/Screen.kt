package com.example.pos.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Dashboard : Screen("dashboard")

    object InventoryLog : Screen("inventory_log")

    object Kas : Screen("kas")

    object AddKas : Screen("add_kas")

    object Product : Screen("product")

    object DetailKas : Screen("detail_kas/{kasId}") {

        fun createRoute(kasId: String): String {
            return "detail_kas/$kasId"
        }
    }
}