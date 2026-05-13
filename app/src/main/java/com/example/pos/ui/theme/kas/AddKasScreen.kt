package com.example.pos.ui.theme.kas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pos.data.model.CashAccount
import com.example.pos.data.model.CashLog
import com.example.pos.viewmodel.KasUiState
import com.example.pos.viewmodel.KasViewModel

/* =============================================
 * ADD KAS SCREEN
 * Dipakai untuk 2 mode:
 * 1. Mode BARU (cashAccountId == null)  → form buat kas baru
 * 2. Mode DETAIL (cashAccountId != null) → lihat detail, edit, transaksi, log
 * ============================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKasScreen(
    cashAccountId: String? = null,
    onNavigateBack: () -> Unit,
    kasViewModel: KasViewModel = viewModel()
) {
    val cashAccounts by kasViewModel.cashAccounts.collectAsStateWithLifecycle()
    val cashLogs by kasViewModel.cashLogs.collectAsStateWithLifecycle()
    val isLoadingLogs by kasViewModel.isLoadingLogs.collectAsStateWithLifecycle()
    val uiState by kasViewModel.uiState.collectAsStateWithLifecycle()

    val kasName by kasViewModel.kasName.collectAsStateWithLifecycle()
    val initialBalance by kasViewModel.initialBalance.collectAsStateWithLifecycle()
    val transactionAmount by kasViewModel.transactionAmount.collectAsStateWithLifecycle()
    val transactionNotes by kasViewModel.transactionNotes.collectAsStateWithLifecycle()

    // Cari akun kas yang dipilih (mode DETAIL)
    val selectedAccount = cashAccounts.find { it.id == cashAccountId }

    // Mode yang sedang aktif di UI
    val isNewMode = cashAccountId == null
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf("in") } // "in" atau "out"

    // Tab index: 0 = Info, 1 = Log Kas
    var selectedTab by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load data saat pertama kali masuk
    LaunchedEffect(cashAccountId) {
        if (cashAccountId != null) {
            kasViewModel.loadCashAccounts()
            kasViewModel.loadCashLogs(cashAccountId)
        }
    }

    // Isi form edit dengan nama kas saat ini
    LaunchedEffect(selectedAccount) {
        if (selectedAccount != null && showEditNameDialog) {
            kasViewModel.onKasNameChange(selectedAccount.name)
        }
    }

    // Reaksi terhadap hasil operasi
    LaunchedEffect(uiState) {
        when (uiState) {
            is KasUiState.Success -> {
                snackbarHostState.showSnackbar("Berhasil disimpan")
                showTransactionDialog = false
                showEditNameDialog = false
                kasViewModel.resetUiState()
            }
            is KasUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as KasUiState.Error).message)
                kasViewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewMode) "Tambah Kas Baru"
                        else selectedAccount?.name ?: "Detail Kas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (isNewMode) {
            /* =============================================
             * MODE BARU: Form buat kas baru
             * ============================================= */
            CreateKasForm(
                modifier = Modifier.padding(paddingValues),
                kasName = kasName,
                initialBalance = initialBalance,
                uiState = uiState,
                onKasNameChange = kasViewModel::onKasNameChange,
                onInitialBalanceChange = kasViewModel::onInitialBalanceChange,
                onSubmit = {
                    kasViewModel.createCashAccount()
                },
                onSuccess = onNavigateBack
            )
        } else if (selectedAccount != null) {
            /* =============================================
             * MODE DETAIL: Info + Log + Aksi
             * ============================================= */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Info & Aksi") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Log Kas") }
                    )
                }

                when (selectedTab) {
                    0 -> KasInfoTab(
                        account = selectedAccount,
                        uiState = uiState,
                        onEditNameClick = {
                            kasViewModel.onKasNameChange(selectedAccount.name)
                            showEditNameDialog = true
                        },
                        onToggleStatusClick = {
                            kasViewModel.toggleKasStatus(selectedAccount.id, selectedAccount.isActive)
                        },
                        onAddBalanceClick = {
                            transactionType = "in"
                            kasViewModel.resetForm()
                            showTransactionDialog = true
                        },
                        onSubtractBalanceClick = {
                            transactionType = "out"
                            kasViewModel.resetForm()
                            showTransactionDialog = true
                        }
                    )

                    1 -> KasLogTab(
                        logs = cashLogs,
                        isLoading = isLoadingLogs
                    )
                }
            }

            /* =============================================
             * DIALOG: Edit Nama Kas
             * ============================================= */
            if (showEditNameDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showEditNameDialog = false
                        kasViewModel.resetForm()
                    },
                    title = { Text("Ubah Nama Kas") },
                    text = {
                        OutlinedTextField(
                            value = kasName,
                            onValueChange = kasViewModel::onKasNameChange,
                            label = { Text("Nama Kas") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { kasViewModel.updateCashAccountName(selectedAccount.id) },
                            enabled = uiState !is KasUiState.Loading
                        ) {
                            if (uiState is KasUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text("Simpan")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showEditNameDialog = false
                            kasViewModel.resetForm()
                        }) {
                            Text("Batal")
                        }
                    }
                )
            }

            /* =============================================
             * DIALOG: Transaksi Manual (Tambah / Kurang)
             * ============================================= */
            if (showTransactionDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showTransactionDialog = false
                        kasViewModel.resetForm()
                    },
                    title = {
                        Text(if (transactionType == "in") "Tambah Saldo" else "Kurangi Saldo")
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Saldo saat ini: ${formatRupiah(selectedAccount.currentBalance)}",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = transactionAmount,
                                onValueChange = kasViewModel::onTransactionAmountChange,
                                label = { Text("Nominal (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = transactionNotes,
                                onValueChange = kasViewModel::onTransactionNotesChange,
                                label = { Text("Keterangan (opsional)") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (transactionType == "in") {
                                    kasViewModel.addBalance(selectedAccount)
                                } else {
                                    kasViewModel.subtractBalance(selectedAccount)
                                }
                            },
                            enabled = uiState !is KasUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transactionType == "in")
                                    Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        ) {
                            if (uiState is KasUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Text(if (transactionType == "in") "Tambah" else "Kurangi")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showTransactionDialog = false
                            kasViewModel.resetForm()
                        }) {
                            Text("Batal")
                        }
                    }
                )
            }
        } else if (cashAccountId != null) {
            // Masih loading data
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

/* =============================================
 * CREATE KAS FORM
 * ============================================= */
@Composable
fun CreateKasForm(
    modifier: Modifier = Modifier,
    kasName: String,
    initialBalance: String,
    uiState: KasUiState,
    onKasNameChange: (String) -> Unit,
    onInitialBalanceChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuccess: () -> Unit
) {
    LaunchedEffect(uiState) {
        if (uiState is KasUiState.Success) {
            onSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Isi data kas baru di bawah ini",
            color = Color.Gray,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = kasName,
            onValueChange = onKasNameChange,
            label = { Text("Nama Kas *") },
            placeholder = { Text("Contoh: Kas Utama, Kas Cadangan") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = initialBalance,
            onValueChange = onInitialBalanceChange,
            label = { Text("Saldo Awal (Rp) *") },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = uiState !is KasUiState.Loading
        ) {
            if (uiState is KasUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Simpan Kas", fontSize = 16.sp)
            }
        }
    }
}

/* =============================================
 * KAS INFO TAB - Info, toggle status, transaksi
 * ============================================= */
@Composable
fun KasInfoTab(
    account: CashAccount,
    uiState: KasUiState,
    onEditNameClick: () -> Unit,
    onToggleStatusClick: () -> Unit,
    onAddBalanceClick: () -> Unit,
    onSubtractBalanceClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Kartu saldo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saldo Saat Ini", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatRupiah(account.currentBalance),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val statusColor = if (account.isActive) Color(0xFF81C784) else Color(0xFFEF9A9A)
                    Text(
                        text = if (account.isActive) "● Aktif" else "● Non-aktif",
                        color = statusColor,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            // Tombol transaksi manual
            Text("Transaksi Manual", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddBalanceClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = account.isActive
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah")
                }

                Button(
                    onClick = onSubtractBalanceClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    enabled = account.isActive
                ) {
                    Text("− Kurangi")
                }
            }

            if (!account.isActive) {
                Text(
                    text = "Aktifkan kas terlebih dahulu untuk melakukan transaksi",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        item {
            Divider()
        }

        item {
            // Pengaturan kas
            Text("Pengaturan", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onEditNameClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✏️  Ubah Nama Kas")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onToggleStatusClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is KasUiState.Loading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (account.isActive) Color(0xFFF44336) else Color(0xFF4CAF50)
                )
            ) {
                if (uiState is KasUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text(if (account.isActive) "Non-aktifkan Kas" else "Aktifkan Kas")
                }
            }
        }

        item {
            // Info detail
            Divider()
            Spacer(modifier = Modifier.height(4.dp))
            Text("Dibuat: ${formatDate(account.createdAt)}", color = Color.Gray, fontSize = 12.sp)
            Text("Diupdate: ${formatDate(account.updatedAt)}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

/* =============================================
 * KAS LOG TAB - Histori transaksi kas
 * ============================================= */
@Composable
fun KasLogTab(
    logs: List<CashLog>,
    isLoading: Boolean
) {
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        logs.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada transaksi pada kas ini", color = Color.Gray)
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    CashLogItem(log = log)
                }
            }
        }
    }
}

/* =============================================
 * CASH LOG ITEM - Satu baris histori transaksi
 * ============================================= */
@Composable
fun CashLogItem(log: CashLog) {
    val isIn = log.type == "in"
    val typeColor = if (isIn) Color(0xFF4CAF50) else Color(0xFFF44336)
    val typeSign = if (isIn) "+" else "-"
    val typeLabel = if (isIn) "Masuk" else "Keluar"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indikator warna
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(typeColor, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(typeLabel, color = typeColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("•", color = Color.Gray, fontSize = 12.sp)
                    Text(log.source, color = Color.Gray, fontSize = 12.sp)
                }
                if (!log.notes.isNullOrEmpty()) {
                    Text(log.notes, color = Color.DarkGray, fontSize = 13.sp)
                }
                Text(
                    text = formatDate(log.createdAt),
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$typeSign ${formatRupiah(log.amount)}",
                    color = typeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Saldo: ${formatRupiah(log.runningBalance)}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}