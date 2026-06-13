package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.model.CartItem
import com.example.pos.model.CashAccount
import com.example.pos.model.Customer
import com.example.pos.model.Product
import com.example.pos.model.Sale
import com.example.pos.model.SaleItem
import com.example.pos.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TransactionUiState {
    object Idle : TransactionUiState()
    object Loading : TransactionUiState()
    object Success : TransactionUiState()
    data class Error(val message: String) : TransactionUiState()
}

enum class TransactionSection {
    CART,
    CHECKOUT,
    HISTORY,
    DETAIL
}

class TransactionViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _section = MutableStateFlow(TransactionSection.CART)
    val section: StateFlow<TransactionSection> = _section.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _cashAccounts = MutableStateFlow<List<CashAccount>>(emptyList())
    val cashAccounts: StateFlow<List<CashAccount>> = _cashAccounts.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _selectedCashAccount = MutableStateFlow<CashAccount?>(null)
    val selectedCashAccount: StateFlow<CashAccount?> = _selectedCashAccount.asStateFlow()

    private val _paidAmount = MutableStateFlow("")
    val paidAmount: StateFlow<String> = _paidAmount.asStateFlow()

    private val _salesHistory = MutableStateFlow<List<Sale>>(emptyList())
    val salesHistory: StateFlow<List<Sale>> = _salesHistory.asStateFlow()

    private val _selectedSale = MutableStateFlow<Sale?>(null)
    val selectedSale: StateFlow<Sale?> = _selectedSale.asStateFlow()

    private val _selectedSaleItems = MutableStateFlow<List<SaleItem>>(emptyList())
    val selectedSaleItems: StateFlow<List<SaleItem>> = _selectedSaleItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<TransactionUiState>(TransactionUiState.Idle)
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    val subtotal: Double
        get() = _cartItems.value.sumOf { it.subtotal }

    val total: Double
        get() = subtotal

    val changeAmount: Double
        get() = (_paidAmount.value.toDoubleOrNull() ?: 0.0) - total

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            try {
                _products.value = repository.getProducts()
                _customers.value = repository.getCustomers()
                _cashAccounts.value = repository.getCashAccounts()
                _salesHistory.value = repository.getSalesHistory()
                _selectedCashAccount.value = _cashAccounts.value.firstOrNull()
                _uiState.value = TransactionUiState.Idle
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Gagal memuat data transaksi")
            }
        }
    }

    fun selectSection(section: TransactionSection) {
        _section.value = section
    }

    fun onSearchChange(value: String) {
        _searchQuery.value = value
    }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
    }

    fun selectCashAccount(cashAccount: CashAccount) {
        _selectedCashAccount.value = cashAccount
    }

    fun onPaidAmountChange(value: String) {
        _paidAmount.value = value.filter { it.isDigit() || it == '.' }
    }

    fun addProductToCart(product: Product) {
        if (product.stock <= 0.0) {
            _uiState.value = TransactionUiState.Error("Stok ${product.name} habis")
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == product.id }

        if (index >= 0) {
            val current = currentItems[index]
            val newQuantity = current.quantity + 1.0
            if (newQuantity > product.stock) {
                _uiState.value = TransactionUiState.Error("Stok ${product.name} tidak mencukupi")
                return
            }
            currentItems[index] = current.copy(quantity = newQuantity)
        } else {
            currentItems.add(CartItem(product = product, quantity = 1.0))
        }

        _cartItems.value = currentItems
        _section.value = TransactionSection.CART
    }

    fun increaseQuantity(productId: String) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.product.id == productId && item.quantity < item.product.stock) {
                item.copy(quantity = item.quantity + 1.0)
            } else {
                item
            }
        }
    }

    fun decreaseQuantity(productId: String) {
        _cartItems.value = _cartItems.value.mapNotNull { item ->
            if (item.product.id == productId) {
                val newQuantity = item.quantity - 1.0
                if (newQuantity <= 0.0) null else item.copy(quantity = newQuantity)
            } else {
                item
            }
        }
    }

    fun removeCartItem(productId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
    }

    fun moveToCheckout() {
        if (_cartItems.value.isEmpty()) {
            _uiState.value = TransactionUiState.Error("Tambahkan produk ke keranjang terlebih dahulu")
            return
        }
        if (_selectedCustomer.value == null) {
            _uiState.value = TransactionUiState.Error("Pilih pelanggan terlebih dahulu")
            return
        }
        _section.value = TransactionSection.CHECKOUT
    }

    fun checkout() {
        val customer = _selectedCustomer.value
        val cashAccount = _selectedCashAccount.value
        val paid = _paidAmount.value.toDoubleOrNull()

        if (customer == null) {
            _uiState.value = TransactionUiState.Error("Pilih pelanggan terlebih dahulu")
            return
        }
        if (cashAccount == null) {
            _uiState.value = TransactionUiState.Error("Pilih kas terlebih dahulu")
            return
        }
        if (paid == null || paid < total) {
            _uiState.value = TransactionUiState.Error("Jumlah bayar tidak valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            try {
                repository.checkout(
                    customer = customer,
                    cashAccount = cashAccount,
                    cartItems = _cartItems.value,
                    paidAmount = paid
                )
                _cartItems.value = emptyList()
                _paidAmount.value = ""
                _selectedCustomer.value = null
                _products.value = repository.getProducts()
                _cashAccounts.value = repository.getCashAccounts()
                _salesHistory.value = repository.getSalesHistory()
                _section.value = TransactionSection.HISTORY
                _uiState.value = TransactionUiState.Success
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Checkout gagal")
            }
        }
    }

    fun openSaleDetail(sale: Sale) {
        viewModelScope.launch {
            _uiState.value = TransactionUiState.Loading
            try {
                _selectedSale.value = sale
                _selectedSaleItems.value = repository.getSaleItems(sale.id)
                _section.value = TransactionSection.DETAIL
                _uiState.value = TransactionUiState.Idle
            } catch (e: Exception) {
                _uiState.value = TransactionUiState.Error(e.message ?: "Gagal memuat detail transaksi")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = TransactionUiState.Idle
    }
}
