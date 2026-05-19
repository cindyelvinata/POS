package com.example.pos.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pos.model.Product
import com.example.pos.ui.theme.DashboardScreen
import com.example.pos.ui.theme.LoginScreen
import com.example.pos.ui.theme.ProductScreen
import com.example.pos.ui.theme.RegisterScreen
import com.example.pos.ui.theme.kas.AddKasScreen
import com.example.pos.ui.theme.kas.KasScreen
import com.example.pos.viewmodel.AuthCheckState
import com.example.pos.viewmodel.AuthUiState
import com.example.pos.viewmodel.AuthViewModel
import com.example.pos.viewmodel.KasViewModel
import com.example.pos.viewmodel.ProductViewModel
import com.example.pos.ui.theme.InventoryLogScreen
import com.example.pos.viewmodel.InventoryLogViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {

    val authCheckState =
        authViewModel.authCheckState.collectAsStateWithLifecycle()

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

    val kasViewModel: KasViewModel = viewModel()

    val fullName =
        authViewModel.fullName.collectAsStateWithLifecycle()

    val email =
        authViewModel.email.collectAsStateWithLifecycle()

    val password =
        authViewModel.password.collectAsStateWithLifecycle()

    val uiState =
        authViewModel.uiState.collectAsStateWithLifecycle()

    val userRole =
        authViewModel.userRole.collectAsStateWithLifecycle()

    val isAdmin =
        userRole.value == "admin"

    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }

    LaunchedEffect(uiState.value) {

        if (uiState.value is AuthUiState.Success) {

            navController.navigate(Screen.Dashboard.route) {

                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }

            authViewModel.resetState()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /*
         * LOGIN
         */
        composable(Screen.Login.route) {

            LoginScreen(

                email = email.value,
                password = password.value,
                uiState = uiState.value,

                onEmailChange =
                    authViewModel::onEmailChange,

                onPasswordChange =
                    authViewModel::onPasswordChange,

                onLoginClick = {

                    authViewModel.login()
                },

                onNavigateToRegister = {

                    navController.navigate(Screen.Register.route)
                }
            )
        }

        /*
         * REGISTER
         */
        composable(Screen.Register.route) {

            RegisterScreen(

                fullName = fullName.value,
                email = email.value,
                password = password.value,
                uiState = uiState.value,

                onFullNameChange =
                    authViewModel::onFullNameChange,

                onEmailChange =
                    authViewModel::onEmailChange,

                onPasswordChange =
                    authViewModel::onPasswordChange,

                onRegisterClick = {

                    authViewModel.register()
                },

                onNavigateToLogin = {

                    navController.popBackStack()
                }
            )
        }

        /*
         * DASHBOARD
         */
        composable(Screen.Dashboard.route) {

            DashboardScreen(

                isAdmin = isAdmin,

                kasViewModel = kasViewModel,

                onLogoutClick = {

                    authViewModel.logout()

                    navController.navigate(Screen.Login.route) {

                        popUpTo(Screen.Dashboard.route) {
                            inclusive = true
                        }
                    }
                },

                onNavigateToKas = {

                    navController.navigate(Screen.Kas.route)
                },

                onNavigateToProduct = {

                    navController.navigate(Screen.Product.route)
                }
            )
        }

        /*
         * LIST KAS
         */
        composable(Screen.Kas.route) {

            KasScreen(

                isAdmin = isAdmin,

                kasViewModel = kasViewModel,

                onNavigateBack = {

                    navController.popBackStack()
                },

                onNavigateToAddKas = {

                    navController.navigate(Screen.AddKas.route)
                },

                onNavigateToDetailKas = { kasId ->

                    navController.navigate(
                        Screen.DetailKas.createRoute(kasId)
                    )
                }
            )
        }

        /*
         * TAMBAH KAS
         */
        composable(Screen.AddKas.route) {

            AddKasScreen(

                cashAccountId = null,

                isAdmin = isAdmin,

                kasViewModel = kasViewModel,

                onNavigateBack = {

                    navController.popBackStack()
                }
            )
        }

        /*
         * DETAIL KAS
         */
        composable(Screen.DetailKas.route) { backStackEntry ->

            val kasId =
                backStackEntry.arguments?.getString("kasId")

            AddKasScreen(

                cashAccountId = kasId,

                isAdmin = isAdmin,

                kasViewModel = kasViewModel,

                onNavigateBack = {

                    navController.popBackStack()
                }
            )
        }

        /*
         * PRODUCT
         */
        composable(Screen.Product.route) {

            val productViewModel: ProductViewModel = viewModel()

            val productUiState =
                productViewModel.productUiState
                    .collectAsStateWithLifecycle()

            val name =
                productViewModel.name
                    .collectAsStateWithLifecycle()

            val price =
                productViewModel.price
                    .collectAsStateWithLifecycle()

            val stock =
                productViewModel.stock
                    .collectAsStateWithLifecycle()

            val isEditMode =
                productViewModel.isEditMode
                    .collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {

                productViewModel.loadProducts()
            }

            ProductScreen(

                name = name.value,

                price = price.value,

                stock = stock.value,

                isEditMode = isEditMode.value,

                productUiState = productUiState.value,

                onNameChange =
                    productViewModel::onNameChange,

                onPriceChange =
                    productViewModel::onPriceChange,

                onStockChange =
                    productViewModel::onStockChange,

                onAddClick = {

                    productViewModel.addProduct()
                },

                onUpdateClick = {

                    productViewModel.updateProduct()
                },

                onEditClick = { product ->

                    productViewModel.fillForm(product)
                },

                onDeleteClick = { product ->

                    productViewModel.deleteProduct(product.id)
                },

                onDetailClick = { product ->

                    selectedProduct = product
                },

                onInventoryLogClick = {

                    navController.navigate(
                        Screen.InventoryLog.route
                    )
                },

                selectedProduct = selectedProduct,

                onDismissDetail = {

                    selectedProduct = null
                },

                onLogoutClick = {

                    authViewModel.logout()

                    navController.navigate(Screen.Login.route) {

                        popUpTo(Screen.Dashboard.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        /*
 * INVENTORY LOG
 */
        composable(Screen.InventoryLog.route) {

            val inventoryLogViewModel: InventoryLogViewModel =
                viewModel()

            val inventoryLogUiState =
                inventoryLogViewModel
                    .inventoryLogUiState
                    .collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {

                inventoryLogViewModel.loadLogs()
            }

            InventoryLogScreen(
                inventoryLogUiState =
                    inventoryLogUiState.value
            )
        }
    }
}