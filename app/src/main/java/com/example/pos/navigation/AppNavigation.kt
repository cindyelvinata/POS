package com.example.pos.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pos.ui.theme.DashboardScreen
import com.example.pos.ui.theme.LoginScreen
import com.example.pos.ui.theme.RegisterScreen
import com.example.pos.ui.theme.kas.AddKasScreen
import com.example.pos.ui.theme.kas.KasScreen
import com.example.pos.viewmodel.AuthCheckState
import com.example.pos.viewmodel.AuthUiState
import com.example.pos.viewmodel.AuthViewModel
import com.example.pos.viewmodel.KasViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val authCheckState = authViewModel.authCheckState.collectAsStateWithLifecycle()

    when (authCheckState.value) {

        is AuthCheckState.Checking -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AuthCheckState.Authenticated -> {
            MainNavHost(
                authViewModel = authViewModel,
                startDestination = Screen.Dashboard.route
            )
        }

        is AuthCheckState.NotAuthenticated -> {
            MainNavHost(
                authViewModel = authViewModel,
                startDestination = Screen.Login.route
            )
        }
    }
}

@Composable
fun MainNavHost(
    authViewModel: AuthViewModel,
    startDestination: String
) {
    val navController = rememberNavController()

    /*
     * KasViewModel dibuat di sini agar instance-nya sama
     * di KasScreen, AddKasScreen, dan DashboardScreen.
     * Tidak perlu buat ulang tiap pindah halaman.
     */
    val kasViewModel: KasViewModel = viewModel()

    val fullName = authViewModel.fullName.collectAsStateWithLifecycle()
    val email = authViewModel.email.collectAsStateWithLifecycle()
    val password = authViewModel.password.collectAsStateWithLifecycle()
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.value) {
        if (uiState.value is AuthUiState.Success) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            authViewModel.resetState()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /* =============================================
         * LOGIN SCREEN
         * ============================================= */
        composable(Screen.Login.route) {
            LoginScreen(
                email = email.value,
                password = password.value,
                uiState = uiState.value,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onLoginClick = { authViewModel.login() },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        /* =============================================
         * REGISTER SCREEN
         * ============================================= */
        composable(Screen.Register.route) {
            RegisterScreen(
                fullName = fullName.value,
                email = email.value,
                password = password.value,
                uiState = uiState.value,
                onFullNameChange = authViewModel::onFullNameChange,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onRegisterClick = { authViewModel.register() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        /* =============================================
         * DASHBOARD SCREEN
         * ============================================= */
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToKas = {
                    navController.navigate(Screen.Kas.route)
                },
                kasViewModel = kasViewModel
            )
        }

        /* =============================================
         * KAS SCREEN - Daftar semua kas
         * ============================================= */
        composable(Screen.Kas.route) {
            KasScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddKas = {
                    navController.navigate(Screen.AddKas.route)
                },
                onNavigateToDetailKas = { kasId ->
                    navController.navigate(Screen.DetailKas.createRoute(kasId))
                },
                kasViewModel = kasViewModel
            )
        }

        /* =============================================
         * ADD KAS SCREEN - Tambah kas baru
         * ============================================= */
        composable(Screen.AddKas.route) {
            AddKasScreen(
                cashAccountId = null,
                onNavigateBack = { navController.popBackStack() },
                kasViewModel = kasViewModel
            )
        }

        /* =============================================
         * DETAIL KAS SCREEN - Detail, edit, transaksi
         * ============================================= */
        composable(Screen.DetailKas.route) { backStackEntry ->
            val kasId = backStackEntry.arguments?.getString("kasId")
            AddKasScreen(
                cashAccountId = kasId,
                onNavigateBack = { navController.popBackStack() },
                kasViewModel = kasViewModel
            )
        }
    }
}