package com.example.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pos.navigation.AppNavigation
import com.example.pos.ui.theme.POSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            POSTheme {
                /*
                 * AppNavigation menjadi root utama aplikasi.
                 * Dari sini, aplikasi bisa pindah ke Login, Register, dan Dashboard.
                 */
                AppNavigation()
            }
        }
    }
}