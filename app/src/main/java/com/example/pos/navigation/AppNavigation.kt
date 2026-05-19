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
import com.example.pos.ui.theme.LoginScreen
import com.example.pos.ui.theme.ProductScreen
import com.example.pos.ui.theme.RegisterScreen
import com.example.pos.viewmodel.AuthCheckState
import com.example.pos.viewmodel.AuthUiState
import com.example.pos.viewmodel.AuthViewModel
import com.example.pos.viewmodel.ProductViewModel

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

    val fullName =
        authViewModel.fullName.collectAsStateWithLifecycle()

    val email =
        authViewModel.email.collectAsStateWithLifecycle()

    val password =
        authViewModel.password.collectAsStateWithLifecycle()

    val uiState =
        authViewModel.uiState.collectAsStateWithLifecycle()

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
         * DASHBOARD / PRODUCT
         */
        composable(Screen.Dashboard.route) {

            val productViewModel: ProductViewModel = viewModel()

            val productUiState =
                productViewModel.productUiState.collectAsStateWithLifecycle()

            val name =
                productViewModel.name.collectAsStateWithLifecycle()

            val price =
                productViewModel.price.collectAsStateWithLifecycle()

            val stock =
                productViewModel.stock.collectAsStateWithLifecycle()

            val isEditMode =
                productViewModel.isEditMode.collectAsStateWithLifecycle()

            var selectedProduct by remember {
                mutableStateOf<Product?>(null)
            }

            LaunchedEffect(Unit) {

                productViewModel.loadProducts()
            }

            ProductScreen(

                name = name.value,
                price = price.value,
                stock = stock.value,

                isEditMode = isEditMode.value,

                productUiState = productUiState.value,

                selectedProduct = selectedProduct,

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
    }
}