package com.example.pos.navigation

/*
 * Sealed class juga bisa digunakan untuk route navigasi.
 * Tujuannya agar nama route tidak ditulis manual berkali-kali.
 */
sealed class Screen(val route: String) {
    /*
     * Route untuk halaman login.
     */
    object Login : Screen("login")

    /*
     * Route untuk halaman register.
     */
    object Register : Screen("register")

    /*
     * Route untuk halaman dashboard.
     */
    object Dashboard : Screen("dashboard")

    /*
    * Route untuk daftar semua kas.
    */
    object Kas : Screen("kas")

    /*
     * Route untuk form tambah kas baru.
     */
    object AddKas : Screen("add_kas")

    /*
     * Route untuk detail / edit satu kas.
     * Menggunakan argument {kasId} di URL.
     */
    object DetailKas : Screen("detail_kas/{kasId}") {
        fun createRoute(kasId: String) = "detail_kas/$kasId"
    }
}