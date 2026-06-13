package com.example.pos.ui.theme.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.model.CartItem
import com.example.pos.model.CashAccount
import com.example.pos.model.Customer
import com.example.pos.model.Product
import com.example.pos.model.Sale
import com.example.pos.model.SaleItem
import com.example.pos.ui.theme.kas.formatDate
import com.example.pos.ui.theme.kas.formatRupiah
import com.example.pos.viewmodel.TransactionSection
import com.example.pos.viewmodel.TransactionUiState
import com.example.pos.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    isAdmin: Boolean,
    viewModel: TransactionViewModel
) {
    val section by viewModel.section.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cashAccounts by viewModel.cashAccounts.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val selectedCashAccount by viewModel.selectedCashAccount.collectAsState()
    val paidAmount by viewModel.paidAmount.collectAsState()
    val salesHistory by viewModel.salesHistory.collectAsState()
    val selectedSale by viewModel.selectedSale.collectAsState()
    val selectedSaleItems by viewModel.selectedSaleItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is TransactionUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            is TransactionUiState.Success -> {
                snackbarHostState.showSnackbar("Checkout berhasil")
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaksi Penjualan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.24f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "Admin" else "Kasir",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(paddingValues)
        ) {
            TransactionTabs(
                selectedSection = section,
                onSelectSection = viewModel::selectSection
            )

            when (section) {
                TransactionSection.CART -> CartContent(
                    products = products.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    },
                    customers = customers,
                    selectedCustomer = selectedCustomer,
                    searchQuery = searchQuery,
                    cartItems = cartItems,
                    subtotal = viewModel.subtotal,
                    onSearchChange = viewModel::onSearchChange,
                    onSelectCustomer = viewModel::selectCustomer,
                    onAddProduct = viewModel::addProductToCart,
                    onIncreaseQuantity = viewModel::increaseQuantity,
                    onDecreaseQuantity = viewModel::decreaseQuantity,
                    onRemoveItem = viewModel::removeCartItem,
                    onCheckout = viewModel::moveToCheckout,
                    isLoading = uiState is TransactionUiState.Loading
                )

                TransactionSection.CHECKOUT -> CheckoutContent(
                    cartItems = cartItems,
                    cashAccounts = cashAccounts,
                    selectedCashAccount = selectedCashAccount,
                    selectedCustomer = selectedCustomer,
                    paidAmount = paidAmount,
                    total = viewModel.total,
                    changeAmount = viewModel.changeAmount,
                    onSelectCashAccount = viewModel::selectCashAccount,
                    onPaidAmountChange = viewModel::onPaidAmountChange,
                    onCheckout = viewModel::checkout,
                    isLoading = uiState is TransactionUiState.Loading
                )

                TransactionSection.HISTORY -> HistoryContent(
                    sales = salesHistory,
                    onOpenDetail = viewModel::openSaleDetail,
                    isLoading = uiState is TransactionUiState.Loading
                )

                TransactionSection.DETAIL -> DetailContent(
                    sale = selectedSale,
                    saleItems = selectedSaleItems,
                    onBack = { viewModel.selectSection(TransactionSection.HISTORY) }
                )
            }
        }
    }
}

@Composable
private fun TransactionTabs(
    selectedSection: TransactionSection,
    onSelectSection: (TransactionSection) -> Unit
) {
    val tabs = listOf(
        TransactionSection.CART to "Keranjang",
        TransactionSection.CHECKOUT to "Checkout",
        TransactionSection.HISTORY to "Riwayat",
        TransactionSection.DETAIL to "Detail"
    )
    val selectedIndex = tabs.indexOfFirst { it.first == selectedSection }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEach { (section, label) ->
            Tab(
                selected = selectedSection == section,
                onClick = { onSelectSection(section) },
                text = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
private fun CartContent(
    products: List<Product>,
    customers: List<Customer>,
    selectedCustomer: Customer?,
    searchQuery: String,
    cartItems: List<CartItem>,
    subtotal: Double,
    onSearchChange: (String) -> Unit,
    onSelectCustomer: (Customer) -> Unit,
    onAddProduct: (Product) -> Unit,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onCheckout: () -> Unit,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari produk...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }

        item {
            StartTransactionCard(
                customers = customers,
                selectedCustomer = selectedCustomer,
                onSelectCustomer = onSelectCustomer
            )
        }

        item {
            SectionTitle("Produk")
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (products.isEmpty()) {
            item {
                EmptyText("Produk tidak ditemukan")
            }
        } else {
            items(products) { product ->
                ProductPickerCard(product = product, onClick = { onAddProduct(product) })
            }
        }

        item {
            SectionTitle("Keranjang")
        }

        if (cartItems.isEmpty()) {
            item {
                EmptyText("Keranjang masih kosong")
            }
        } else {
            items(cartItems) { item ->
                CartItemCard(
                    item = item,
                    onIncrease = { onIncreaseQuantity(item.product.id) },
                    onDecrease = { onDecreaseQuantity(item.product.id) },
                    onRemove = { onRemoveItem(item.product.id) }
                )
            }

            item {
                TotalCard(
                    label = "Subtotal",
                    amount = subtotal,
                    buttonText = "Checkout",
                    onClick = onCheckout
                )
            }
        }
    }
}

@Composable
private fun StartTransactionCard(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onSelectCustomer: (Customer) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF68A6FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text("Mulai Transaksi Baru", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Pilih produk untuk memulai penjualan", color = Color.White.copy(alpha = 0.82f))
            Spacer(modifier = Modifier.height(24.dp))
            CustomerDropdown(
                customers = customers,
                selectedCustomer = selectedCustomer,
                onSelectCustomer = onSelectCustomer
            )
        }
    }
}

@Composable
private fun CustomerDropdown(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onSelectCustomer: (Customer) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                text = selectedCustomer?.name ?: "Pilih Pelanggan",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            customers.forEach { customer ->
                DropdownMenuItem(
                    text = { Text(customer.name) },
                    onClick = {
                        onSelectCustomer(customer)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductPickerCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatRupiah(product.price), color = MaterialTheme.colorScheme.primary)
                Text("Stok: ${product.stock}", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontWeight = FontWeight.SemiBold)
                Text("${item.quantity} x ${formatRupiah(item.product.price)}", color = Color.Gray, fontSize = 12.sp)
                Text(formatRupiah(item.subtotal), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDecrease) {
                Icon(Icons.Default.Remove, contentDescription = "Kurangi")
            }
            Text(item.quantity.toInt().toString(), fontWeight = FontWeight.Bold)
            IconButton(onClick = onIncrease) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFE53935))
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    cartItems: List<CartItem>,
    cashAccounts: List<CashAccount>,
    selectedCashAccount: CashAccount?,
    selectedCustomer: Customer?,
    paidAmount: String,
    total: Double,
    changeAmount: Double,
    onSelectCashAccount: (CashAccount) -> Unit,
    onPaidAmountChange: (String) -> Unit,
    onCheckout: () -> Unit,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionTitle("Checkout")
        }

        item {
            CheckoutSummaryCard(
                selectedCustomer = selectedCustomer,
                cartItems = cartItems,
                total = total
            )
        }

        item {
            CashAccountDropdown(
                cashAccounts = cashAccounts,
                selectedCashAccount = selectedCashAccount,
                onSelectCashAccount = onSelectCashAccount
            )
        }

        item {
            OutlinedTextField(
                value = paidAmount,
                onValueChange = onPaidAmountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Jumlah Bayar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            TotalCard(
                label = "Kembalian: ${formatRupiah(changeAmount.coerceAtLeast(0.0))}",
                amount = total,
                buttonText = if (isLoading) "Memproses..." else "Simpan Transaksi",
                onClick = onCheckout,
                enabled = !isLoading
            )
        }
    }
}

@Composable
private fun CheckoutSummaryCard(
    selectedCustomer: Customer?,
    cartItems: List<CartItem>,
    total: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pelanggan: ${selectedCustomer?.name ?: "-"}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            cartItems.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("${item.product.name} x ${item.quantity.toInt()}", modifier = Modifier.weight(1f))
                    Text(formatRupiah(item.subtotal), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(formatRupiah(total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CashAccountDropdown(
    cashAccounts: List<CashAccount>,
    selectedCashAccount: CashAccount?,
    onSelectCashAccount: (CashAccount) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(selectedCashAccount?.name ?: "Pilih Kas")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            cashAccounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text("${account.name} - ${formatRupiah(account.currentBalance)}") },
                    onClick = {
                        onSelectCashAccount(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    sales: List<Sale>,
    onOpenDetail: (Sale) -> Unit,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle("Riwayat Penjualan")
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (sales.isEmpty()) {
            item {
                EmptyText("Belum ada riwayat penjualan")
            }
        } else {
            items(sales) { sale ->
                SaleHistoryCard(sale = sale, onClick = { onOpenDetail(sale) })
            }
        }
    }
}

@Composable
private fun SaleHistoryCard(sale: Sale, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(sale.customer?.name ?: "Pelanggan", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(formatRupiah(sale.total), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Kas: ${sale.cashAccount?.name ?: "-"}", color = Color.Gray, fontSize = 12.sp)
            Text(formatDate(sale.transactionTime), color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DetailContent(
    sale: Sale?,
    saleItems: List<SaleItem>,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Detail Transaksi")
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onBack) {
                    Text("Riwayat")
                }
            }
        }

        if (sale == null) {
            item {
                EmptyText("Pilih transaksi dari riwayat penjualan")
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transaksi ${sale.id}", fontWeight = FontWeight.Bold)
                        Text("Pelanggan: ${sale.customer?.name ?: "-"}")
                        Text("Kas: ${sale.cashAccount?.name ?: "-"}")
                        Text("Tanggal: ${formatDate(sale.transactionTime)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        saleItems.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("${item.productName} x ${item.quantity.toInt()}", modifier = Modifier.weight(1f))
                                Text(formatRupiah(item.subtotal))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        DetailAmountRow("Total", sale.total)
                        DetailAmountRow("Bayar", sale.paidAmount)
                        DetailAmountRow(
                            "Kembalian",
                            sale.changeAmount ?: 0.0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalCard(
    label: String,
    amount: Double,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray)
            Text(formatRupiah(amount), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailAmountRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text(formatRupiah(amount), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Gray)
    }
}
