package com.example.pos.navigation

/*
 * Sealed class juga bisa digunakan untuk route navigasi.
 * Tujuannya agar nama route tidak ditulis manual berkali-kali.
 */
sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Dashboard : Screen("dashboard")

    object InventoryLog : Screen("inventory_log")
}