package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.model.CashAccount
import com.example.pos.data.repository.KasRepository
import com.example.pos.model.Expense
import com.example.pos.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed class ExpenseUiState {
    object Idle : ExpenseUiState()
    object Loading : ExpenseUiState()
    object Success : ExpenseUiState()
    data class Error(val message: String) : ExpenseUiState()
}

class ExpenseViewModel : ViewModel() {

    private val repository = ExpenseRepository()
    private val kasRepository = KasRepository()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _isLoadingExpenses = MutableStateFlow(false)
    val isLoadingExpenses: StateFlow<Boolean> = _isLoadingExpenses.asStateFlow()

    private val _cashAccounts = MutableStateFlow<List<CashAccount>>(emptyList())
    val cashAccounts: StateFlow<List<CashAccount>> = _cashAccounts.asStateFlow()

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Idle)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // Form inputs
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _selectedCashAccount = MutableStateFlow<CashAccount?>(null)
    val selectedCashAccount: StateFlow<CashAccount?> = _selectedCashAccount.asStateFlow()

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date.asStateFlow()

    fun onDescriptionChange(value: String) { _description.value = value }
    fun onAmountChange(value: String) { _amount.value = value }
    fun onSelectedCashAccountChange(value: CashAccount?) { _selectedCashAccount.value = value }
    fun onDateChange(value: String) { _date.value = value }

    fun resetForm() {
        _description.value = ""
        _amount.value = ""
        _selectedCashAccount.value = null
        // Default date to today's date (YYYY-MM-DD)
        val todayIso = Clock.System.now().toString()
        _date.value = todayIso.substring(0, 10)
    }

    fun resetUiState() {
        _uiState.value = ExpenseUiState.Idle
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _isLoadingExpenses.value = true
            try {
                _expenses.value = repository.getExpenses()
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Gagal memuat laporan pengeluaran")
            } finally {
                _isLoadingExpenses.value = false
            }
        }
    }

    fun loadActiveCashAccounts() {
        viewModelScope.launch {
            try {
                val accounts = kasRepository.getActiveCashAccounts()
                _cashAccounts.value = accounts
                if (_selectedCashAccount.value == null && accounts.isNotEmpty()) {
                    _selectedCashAccount.value = accounts.first()
                }
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Gagal memuat daftar kas")
            }
        }
    }

    fun addExpense() {
        val desc = _description.value.trim()
        val amt = _amount.value.trim().toDoubleOrNull()
        val account = _selectedCashAccount.value
        val dateVal = _date.value.trim()

        if (desc.isEmpty()) {
            _uiState.value = ExpenseUiState.Error("Deskripsi tidak boleh kosong")
            return
        }
        if (amt == null || amt <= 0) {
            _uiState.value = ExpenseUiState.Error("Nominal pengeluaran tidak valid")
            return
        }
        if (account == null) {
            _uiState.value = ExpenseUiState.Error("Pilih kas yang digunakan untuk membayar")
            return
        }
        if (dateVal.isEmpty()) {
            _uiState.value = ExpenseUiState.Error("Pilih tanggal pengeluaran")
            return
        }

        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                // Konversi tanggal ke format ISO-8601 full string
                val dateString = "${dateVal}T00:00:00Z"

                repository.addExpense(
                    description = desc,
                    amount = amt,
                    cashAccountId = account.id,
                    currentBalance = account.currentBalance,
                    dateString = dateString
                )

                resetForm()
                loadExpenses()
                _uiState.value = ExpenseUiState.Success
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Gagal mencatatkan pengeluaran")
            }
        }
    }

    fun updateExpenseDescription(expenseId: String, newDescription: String) {
        if (newDescription.trim().isEmpty()) {
            _uiState.value = ExpenseUiState.Error("Deskripsi tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                repository.updateExpenseDescription(expenseId, newDescription.trim())
                loadExpenses()
                _uiState.value = ExpenseUiState.Success
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Gagal merubah deskripsi")
            }
        }
    }

    fun cancelExpense(expense: Expense) {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                // Dapatkan data kas terbaru untuk saldo terkini
                val accounts = kasRepository.getAllCashAccounts()
                val currentAccount = accounts.find { it.id == expense.cashAccountsId }
                    ?: throw IllegalArgumentException("Kas yang digunakan tidak ditemukan")

                repository.cancelExpense(
                    expenseId = expense.id,
                    amount = expense.amount,
                    cashAccountId = expense.cashAccountsId,
                    currentBalance = currentAccount.currentBalance,
                    expenseDescription = expense.description
                )

                loadExpenses()
                _uiState.value = ExpenseUiState.Success
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Gagal membatalkan pengeluaran")
            }
        }
    }
}
