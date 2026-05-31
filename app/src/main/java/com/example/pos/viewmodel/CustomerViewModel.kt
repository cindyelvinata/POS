package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.model.Customer
import com.example.pos.model.CustomerLog
import com.example.pos.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CustomerUiState {
    object Idle : CustomerUiState()
    object Loading : CustomerUiState()
    object Success : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
}

class CustomerViewModel : ViewModel() {

    private val repository = CustomerRepository()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _customerLogs = MutableStateFlow<List<CustomerLog>>(emptyList())
    val customerLogs: StateFlow<List<CustomerLog>> = _customerLogs.asStateFlow()

    private val _isLoadingLogs = MutableStateFlow(false)
    val isLoadingLogs: StateFlow<Boolean> = _isLoadingLogs.asStateFlow()

    private val _uiState = MutableStateFlow<CustomerUiState>(CustomerUiState.Idle)
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    // Form inputs
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _isActive = MutableStateFlow(true)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    // Selected customer for editing
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    fun onNameChange(value: String) { _name.value = value }
    fun onPhoneChange(value: String) { _phone.value = value }
    fun onIsActiveChange(value: Boolean) { _isActive.value = value }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        if (customer != null) {
            _name.value = customer.name
            _phone.value = customer.phone
            _isActive.value = customer.isActive
        } else {
            resetForm()
        }
    }

    fun resetForm() {
        _name.value = ""
        _phone.value = ""
        _isActive.value = true
        _selectedCustomer.value = null
    }

    fun resetUiState() {
        _uiState.value = CustomerUiState.Idle
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _customers.value = repository.getCustomers()
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "Gagal memuat data pelanggan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCustomerLogs() {
        viewModelScope.launch {
            _isLoadingLogs.value = true
            try {
                _customerLogs.value = repository.getCustomerLogs()
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "Gagal memuat log aktivitas")
            } finally {
                _isLoadingLogs.value = false
            }
        }
    }

    fun saveCustomer() {
        val nameVal = _name.value.trim()
        val phoneVal = _phone.value.trim()

        if (nameVal.isEmpty()) {
            _uiState.value = CustomerUiState.Error("Nama tidak boleh kosong")
            return
        }
        if (phoneVal.isEmpty()) {
            _uiState.value = CustomerUiState.Error("Nomor telepon tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = CustomerUiState.Loading
            try {
                val currentSelected = _selectedCustomer.value
                if (currentSelected == null) {
                    // Create new customer
                    repository.createCustomer(nameVal, phoneVal, _isActive.value)
                } else {
                    // Update existing customer
                    repository.updateCustomer(
                        id = currentSelected.id,
                        name = nameVal,
                        phone = phoneVal,
                        isActive = _isActive.value,
                        oldName = currentSelected.name,
                        oldPhone = currentSelected.phone,
                        oldIsActive = currentSelected.isActive
                    )
                }
                resetForm()
                loadCustomers()
                loadCustomerLogs()
                _uiState.value = CustomerUiState.Success
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                val friendlyMessage = when {
                    errorMsg.contains("customers_phone_key") -> "Nomor telepon sudah terdaftar untuk pelanggan lain!"
                    else -> errorMsg.ifEmpty { "Gagal menyimpan data pelanggan" }
                }
                _uiState.value = CustomerUiState.Error(friendlyMessage)
            }
        }
    }
}
