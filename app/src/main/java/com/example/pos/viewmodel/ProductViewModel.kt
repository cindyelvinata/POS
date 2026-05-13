package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.model.Product
import com.example.pos.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    /*
     * Menyimpan ID produk yang sedang diedit
     */
    private var selectedProductId: String? = null

    /*
     * State mode edit
     */
    private val _isEditMode =
        MutableStateFlow(false)

    val isEditMode: StateFlow<Boolean> =
        _isEditMode

    /*
     * State UI produk
     */
    private val _productUiState =
        MutableStateFlow<ProductUiState>(ProductUiState.Loading)

    val productUiState: StateFlow<ProductUiState> =
        _productUiState

    /*
     * State form input
     */
    private val _name =
        MutableStateFlow("")

    val name: StateFlow<String> =
        _name

    private val _price =
        MutableStateFlow("")

    val price: StateFlow<String> =
        _price

    private val _stock =
        MutableStateFlow("")

    val stock: StateFlow<String> =
        _stock

    /*
     * Load semua products
     */
    fun loadProducts() {

        viewModelScope.launch {

            try {

                _productUiState.value =
                    ProductUiState.Loading

                val products =
                    repository.getProducts()

                _productUiState.value =
                    ProductUiState.Success(products)

            } catch (e: Exception) {

                e.printStackTrace()

                _productUiState.value =
                    ProductUiState.Error(
                        e.message ?: "Gagal mengambil products"
                    )
            }
        }
    }

    /*
     * Update input
     */
    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onPriceChange(value: String) {
        _price.value = value
    }

    fun onStockChange(value: String) {
        _stock.value = value
    }

    /*
     * Isi form saat edit
     */
    fun fillForm(product: Product) {

        selectedProductId = product.id

        _isEditMode.value = true

        _name.value = product.name
        _price.value = product.price.toString()
        _stock.value = product.stock.toString()
    }

    /*
     * Tambah produk
     */
    fun addProduct() {

        viewModelScope.launch {

            try {

                repository.addProduct(
                    name = _name.value,
                    price = _price.value.toDoubleOrNull() ?: 0.0,
                    stock = _stock.value.toDoubleOrNull() ?: 0.0
                )

                clearForm()

                _isEditMode.value = false

                loadProducts()

            } catch (e: Exception) {

                e.printStackTrace()

                _productUiState.value =
                    ProductUiState.Error(
                        e.message ?: "Gagal tambah produk"
                    )
            }
        }
    }

    /*
     * Update produk
     */
    fun updateProduct() {

        val productId =
            selectedProductId ?: return

        viewModelScope.launch {

            try {

                repository.updateProduct(
                    id = productId,
                    name = _name.value,
                    price = _price.value.toDoubleOrNull() ?: 0.0,
                    stock = _stock.value.toDoubleOrNull() ?: 0.0,
                    isActive = true
                )

                clearForm()

                selectedProductId = null

                _isEditMode.value = false

                loadProducts()

            } catch (e: Exception) {

                e.printStackTrace()

                _productUiState.value =
                    ProductUiState.Error(
                        e.message ?: "Gagal update produk"
                    )
            }
        }
    }

    /*
     * Soft delete produk
     */
    fun deleteProduct(id: String) {

        viewModelScope.launch {

            try {

                repository.deleteProduct(id)

                loadProducts()

            } catch (e: Exception) {

                e.printStackTrace()

                _productUiState.value =
                    ProductUiState.Error(
                        e.message ?: "Gagal hapus produk"
                    )
            }
        }
    }

    /*
     * Bersihkan form
     */
    private fun clearForm() {

        _name.value = ""
        _price.value = ""
        _stock.value = ""
    }
}