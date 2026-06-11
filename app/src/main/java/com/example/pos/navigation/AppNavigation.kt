package com.example.pos.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pos.model.Product
import com.example.pos.ui.theme.DashboardScreen
import com.example.pos.ui.theme.produk.InventoryLogScreen
import com.example.pos.ui.theme.login.LoginScreen
import com.example.pos.ui.theme.produk.ProductScreen
import com.example.pos.ui.theme.login.RegisterScreen
import com.example.pos.ui.theme.kas.AddKasScreen
import com.example.pos.ui.theme.kas.KasScreen
import com.example.pos.ui.theme.transaction.TransactionScreen
import com.example.pos.viewmodel.AuthCheckState
import com.example.pos.viewmodel.AuthUiState
import com.example.pos.viewmodel.AuthViewModel
import com.example.pos.viewmodel.InventoryLogViewModel
import com.example.pos.viewmodel.KasViewModel
import com.example.pos.viewmodel.ProductViewModel
import com.example.pos.viewmodel.CustomerViewModel
import com.example.pos.viewmodel.ExpenseViewModel
import com.example.pos.viewmodel.TransactionViewModel
import com.example.pos.ui.theme.CustomerScreen
import com.example.pos.ui.theme.ExpenseScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Dashboard.route,
        label = "Beranda",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Transaction.route,
        label = "Transaksi",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart
    ),
    BottomNavItem(
        route = Screen.Product.route,
        label = "Produk",
        selectedIcon = Icons.Filled.Inventory,
        unselectedIcon = Icons.Filled.Inventory
    ),
    BottomNavItem(
        route = "laporan",
        label = "Laporan",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    BottomNavItem(
        route = "pelanggan",
        label = "Pelanggan",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )
)

private val routesWithoutBottomNav = setOf(
    Screen.Login.route,
    Screen.Register.route,
    Screen.AddKas.route,
    Screen.DetailKas.route.substringBefore("/"),
    Screen.InventoryLog.route
)

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
    val kasViewModel: KasViewModel = viewModel()

    val fullName = authViewModel.fullName.collectAsStateWithLifecycle()
    val email = authViewModel.email.collectAsStateWithLifecycle()
    val password = authViewModel.password.collectAsStateWithLifecycle()
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
    val userRole = authViewModel.userRole.collectAsStateWithLifecycle()
    val isAdmin = userRole.value == "admin"

    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(uiState.value) {
        if (uiState.value is AuthUiState.Success) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            authViewModel.resetState()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null &&
            routesWithoutBottomNav.none { currentRoute.startsWith(it) } &&
            currentRoute != Screen.Login.route &&
            currentRoute != Screen.Register.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(220)) +
                        slideInHorizontally(
                            animationSpec = tween(220),
                            initialOffsetX = { it / 10 }
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(220)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { -it / 10 }
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220)) +
                        slideInHorizontally(
                            animationSpec = tween(220),
                            initialOffsetX = { -it / 10 }
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(220)) +
                        slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { it / 10 }
                        )
            }
        ) {
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

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    isAdmin = isAdmin,
                    kasViewModel = kasViewModel,
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToKas = { navController.navigate(Screen.Kas.route) },
                    onNavigateToProduct = { navController.navigate(Screen.Product.route) }
                )
            }

            composable(Screen.Kas.route) {
                KasScreen(
                    isAdmin = isAdmin,
                    kasViewModel = kasViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddKas = { navController.navigate(Screen.AddKas.route) },
                    onNavigateToDetailKas = { kasId ->
                        navController.navigate(Screen.DetailKas.createRoute(kasId))
                    }
                )
            }

            composable(Screen.Transaction.route) {
                val transactionViewModel: TransactionViewModel = viewModel()

                TransactionScreen(
                    isAdmin = isAdmin,
                    viewModel = transactionViewModel
                )
            }

            composable(Screen.AddKas.route) {
                AddKasScreen(
                    cashAccountId = null,
                    isAdmin = isAdmin,
                    kasViewModel = kasViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DetailKas.route) { backStackEntry ->
                val kasId = backStackEntry.arguments?.getString("kasId")
                AddKasScreen(
                    cashAccountId = kasId,
                    isAdmin = isAdmin,
                    kasViewModel = kasViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Product.route) {
                val productViewModel: ProductViewModel = viewModel()

                val productUiState = productViewModel.productUiState.collectAsStateWithLifecycle()
                val name = productViewModel.name.collectAsStateWithLifecycle()
                val price = productViewModel.price.collectAsStateWithLifecycle()
                val stock = productViewModel.stock.collectAsStateWithLifecycle()
                val isEditMode = productViewModel.isEditMode.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) { productViewModel.loadProducts() }

                ProductScreen(
                    name = name.value,
                    price = price.value,
                    stock = stock.value,
                    isEditMode = isEditMode.value,
                    productUiState = productUiState.value,
                    onNameChange = productViewModel::onNameChange,
                    onPriceChange = productViewModel::onPriceChange,
                    onStockChange = productViewModel::onStockChange,
                    onAddClick = { productViewModel.addProduct() },
                    onUpdateClick = { productViewModel.updateProduct() },
                    onCancelEdit = { productViewModel.cancelEdit() },
                    onEditClick = { product -> productViewModel.fillForm(product) },
                    onDeleteClick = { product -> productViewModel.deleteProduct(product.id) },
                    onDetailClick = { product -> selectedProduct = product },
                    onInventoryLogClick = {
                        navController.navigate(Screen.InventoryLog.route)
                    },
                    selectedProduct = selectedProduct,
                    onDismissDetail = { selectedProduct = null },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.InventoryLog.route) {
                val inventoryLogViewModel: InventoryLogViewModel = viewModel()
                val inventoryLogUiState = inventoryLogViewModel
                    .inventoryLogUiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(Unit) { inventoryLogViewModel.loadLogs() }

                InventoryLogScreen(
                    inventoryLogUiState = inventoryLogUiState.value,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Customer.route) {
                val customerViewModel: CustomerViewModel = viewModel()

                LaunchedEffect(Unit) {
                    customerViewModel.loadCustomers()
                    customerViewModel.loadCustomerLogs()
                }

                CustomerScreen(
                    customerViewModel = customerViewModel
                )
            }

            composable(Screen.Expense.route) {
                val expenseViewModel: ExpenseViewModel = viewModel()

                LaunchedEffect(Unit) {
                    expenseViewModel.loadExpenses()
                    expenseViewModel.loadActiveCashAccounts()
                }

                ExpenseScreen(
                    isAdmin = isAdmin,
                    expenseViewModel = expenseViewModel
                )
            }

        }
    }
}
