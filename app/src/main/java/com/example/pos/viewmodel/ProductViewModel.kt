package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    /*
     * State untuk UI produk
     */
    private val _productUiState =
        MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val productUiState: StateFlow<ProductUiState> = _productUiState

    private val _name =
        MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _price =
        MutableStateFlow("")
    val price: StateFlow<String> = _price

    private val _stock =
        MutableStateFlow("")
    val stock: StateFlow<String> = _stock

    /*
     * Mengambil data products dari Supabase
     */
    fun loadProducts() {

        viewModelScope.launch {

            try {

                _productUiState.value = ProductUiState.Loading

                val products = repository.getProducts()

                _productUiState.value =
                    ProductUiState.Success(products)

            } catch (e: Exception) {

                _productUiState.value =
                    ProductUiState.Error(
                        e.message ?: "Gagal mengambil products"
                    )
            }
        }
    }
    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onPriceChange(value: String) {
        _price.value = value
    }

    fun onStockChange(value: String) {
        _stock.value = value
    }
    fun addProduct() {

        viewModelScope.launch {

            try {

                repository.addProduct(
                    name = _name.value,
                    price = _price.value.toDoubleOrNull() ?: 0.0,
                    stock = _stock.value.toDoubleOrNull() ?: 0.0
                )

                _name.value = ""
                _price.value = ""
                _stock.value = ""

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
}