package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.data.model.CashAccount
import com.example.pos.data.model.CashLog
import com.example.pos.data.repository.KasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/* =============================================
 * UI STATE untuk operasi Kas
 * ============================================= */
sealed class KasUiState {
    object Idle : KasUiState()
    object Loading : KasUiState()
    object Success : KasUiState()
    data class Error(val message: String) : KasUiState()
}

class KasViewModel : ViewModel() {

    private val repository = KasRepository()

    /* =============================================
     * STATE: Daftar Kas
     * ============================================= */
    private val _cashAccounts = MutableStateFlow<List<CashAccount>>(emptyList())
    val cashAccounts: StateFlow<List<CashAccount>> = _cashAccounts.asStateFlow()

    private val _isLoadingAccounts = MutableStateFlow(false)
    val isLoadingAccounts: StateFlow<Boolean> = _isLoadingAccounts.asStateFlow()

    /* =============================================
     * STATE: Log Kas (untuk detail per kas)
     * ============================================= */
    private val _cashLogs = MutableStateFlow<List<CashLog>>(emptyList())
    val cashLogs: StateFlow<List<CashLog>> = _cashLogs.asStateFlow()

    private val _isLoadingLogs = MutableStateFlow(false)
    val isLoadingLogs: StateFlow<Boolean> = _isLoadingLogs.asStateFlow()

    /* =============================================
     * STATE: Dashboard
     * ============================================= */
    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _todaySalesTotal = MutableStateFlow(0.0)
    val todaySalesTotal: StateFlow<Double> = _todaySalesTotal.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<CashLog>>(emptyList())
    val recentLogs: StateFlow<List<CashLog>> = _recentLogs.asStateFlow()

    private val _isLoadingDashboard = MutableStateFlow(false)
    val isLoadingDashboard: StateFlow<Boolean> = _isLoadingDashboard.asStateFlow()

    /* =============================================
     * STATE: Form Input
     * ============================================= */
    private val _kasName = MutableStateFlow("")
    val kasName: StateFlow<String> = _kasName.asStateFlow()

    private val _initialBalance = MutableStateFlow("")
    val initialBalance: StateFlow<String> = _initialBalance.asStateFlow()

    private val _transactionAmount = MutableStateFlow("")
    val transactionAmount: StateFlow<String> = _transactionAmount.asStateFlow()

    private val _transactionNotes = MutableStateFlow("")
    val transactionNotes: StateFlow<String> = _transactionNotes.asStateFlow()

    /* =============================================
     * STATE: UI Operation Result
     * ============================================= */
    private val _uiState = MutableStateFlow<KasUiState>(KasUiState.Idle)
    val uiState: StateFlow<KasUiState> = _uiState.asStateFlow()

    /* =============================================
     * FORM INPUT HANDLERS
     * ============================================= */
    fun onKasNameChange(value: String) { _kasName.value = value }
    fun onInitialBalanceChange(value: String) { _initialBalance.value = value }
    fun onTransactionAmountChange(value: String) { _transactionAmount.value = value }
    fun onTransactionNotesChange(value: String) { _transactionNotes.value = value }

    fun resetForm() {
        _kasName.value = ""
        _initialBalance.value = ""
        _transactionAmount.value = ""
        _transactionNotes.value = ""
    }

    fun resetUiState() {
        _uiState.value = KasUiState.Idle
    }

    /* =============================================
     * LOAD DATA
     * ============================================= */

    /**
     * Muat semua kas (aktif + non-aktif) untuk KasScreen.
     */
    fun loadCashAccounts() {
        viewModelScope.launch {
            _isLoadingAccounts.value = true
            try {
                _cashAccounts.value = repository.getAllCashAccounts()
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal memuat data kas")
            } finally {
                _isLoadingAccounts.value = false
            }
        }
    }

    /**
     * Muat log transaksi untuk satu kas tertentu.
     */
    fun loadCashLogs(cashAccountId: String) {
        viewModelScope.launch {
            _isLoadingLogs.value = true
            try {
                _cashLogs.value = repository.getCashLogsByAccountId(cashAccountId)
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal memuat log kas")
            } finally {
                _isLoadingLogs.value = false
            }
        }
    }

    /**
     * Muat semua data yang dibutuhkan Dashboard.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoadingDashboard.value = true
            try {
                _totalBalance.value = repository.getTotalActiveBalance()
                _todaySalesTotal.value = repository.getTodaySalesTotal()
                _recentLogs.value = repository.getRecentCashLogs()
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal memuat data dashboard")
            } finally {
                _isLoadingDashboard.value = false
            }
        }
    }

    /* =============================================
     * CRUD OPERASI
     * ============================================= */

    /**
     * Buat kas baru dengan nama dan saldo awal.
     */
    fun createCashAccount() {
        val name = _kasName.value.trim()
        val balance = _initialBalance.value.trim().toDoubleOrNull()

        if (name.isEmpty()) {
            _uiState.value = KasUiState.Error("Nama kas tidak boleh kosong")
            return
        }
        if (balance == null || balance < 0) {
            _uiState.value = KasUiState.Error("Saldo awal tidak valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = KasUiState.Loading
            try {
                repository.createCashAccount(name, balance)
                resetForm()
                loadCashAccounts()
                _uiState.value = KasUiState.Success
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal membuat kas")
            }
        }
    }

    /**
     * Update nama kas.
     */
    fun updateCashAccountName(id: String) {
        val name = _kasName.value.trim()

        if (name.isEmpty()) {
            _uiState.value = KasUiState.Error("Nama kas tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = KasUiState.Loading
            try {
                repository.updateCashAccountName(id, name)
                resetForm()
                loadCashAccounts()
                _uiState.value = KasUiState.Success
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal mengubah nama kas")
            }
        }
    }

    /**
     * Toggle aktif/non-aktif kas.
     */
    fun toggleKasStatus(id: String, currentStatus: Boolean) {
        viewModelScope.launch {
            _uiState.value = KasUiState.Loading
            try {
                repository.toggleCashAccountStatus(id, !currentStatus)
                loadCashAccounts()
                _uiState.value = KasUiState.Success
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal mengubah status kas")
            }
        }
    }

    /**
     * Tambah saldo kas secara manual.
     */
    fun addBalance(cashAccount: CashAccount) {
        val amount = _transactionAmount.value.trim().toDoubleOrNull()
        val notes = _transactionNotes.value.trim()

        if (amount == null || amount <= 0) {
            _uiState.value = KasUiState.Error("Nominal tidak valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = KasUiState.Loading
            try {
                repository.addBalance(
                    cashAccountId = cashAccount.id,
                    currentBalance = cashAccount.currentBalance,
                    amount = amount,
                    notes = notes.ifEmpty { "Tambah saldo manual" }
                )
                resetForm()
                loadCashAccounts()
                loadCashLogs(cashAccount.id)
                _uiState.value = KasUiState.Success
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal menambah saldo")
            }
        }
    }

    /**
     * Kurangi saldo kas secara manual.
     */
    fun subtractBalance(cashAccount: CashAccount) {
        val amount = _transactionAmount.value.trim().toDoubleOrNull()
        val notes = _transactionNotes.value.trim()

        if (amount == null || amount <= 0) {
            _uiState.value = KasUiState.Error("Nominal tidak valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = KasUiState.Loading
            try {
                repository.subtractBalance(
                    cashAccountId = cashAccount.id,
                    currentBalance = cashAccount.currentBalance,
                    amount = amount,
                    notes = notes.ifEmpty { "Kurang saldo manual" }
                )
                resetForm()
                loadCashAccounts()
                loadCashLogs(cashAccount.id)
                _uiState.value = KasUiState.Success
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Gagal mengurangi saldo")
            }
        }
    }
}