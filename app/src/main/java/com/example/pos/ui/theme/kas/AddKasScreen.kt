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
 *
 * Dua mode:
 * 1. cashAccountId == null → form buat kas baru (admin only)
 * 2. cashAccountId != null → detail kas
 *
 * isAdmin menentukan apa yang tampil:
 *
 * ADMIN   → buat kas, ubah nama, toggle status,
 *           tambah/kurang saldo, lihat log
 * CASHIER → hanya lihat saldo & log (read-only)
 * ============================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKasScreen(
    cashAccountId: String? = null,
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    kasViewModel: KasViewModel = viewModel()
) {
    val cashAccounts      by kasViewModel.cashAccounts.collectAsStateWithLifecycle()
    val cashLogs          by kasViewModel.cashLogs.collectAsStateWithLifecycle()
    val isLoadingLogs     by kasViewModel.isLoadingLogs.collectAsStateWithLifecycle()
    val uiState           by kasViewModel.uiState.collectAsStateWithLifecycle()

    val kasName           by kasViewModel.kasName.collectAsStateWithLifecycle()
    val initialBalance    by kasViewModel.initialBalance.collectAsStateWithLifecycle()
    val transactionAmount by kasViewModel.transactionAmount.collectAsStateWithLifecycle()
    val transactionNotes  by kasViewModel.transactionNotes.collectAsStateWithLifecycle()

    val selectedAccount   = cashAccounts.find { it.id == cashAccountId }
    val isNewMode         = cashAccountId == null

    var showEditNameDialog    by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionType       by remember { mutableStateOf("in") } // "in" | "out"
    var selectedTab           by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cashAccountId) {
        if (cashAccountId != null) {
            kasViewModel.loadCashAccounts()
            kasViewModel.loadCashLogs(cashAccountId)
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is KasUiState.Success -> {
                snackbarHostState.showSnackbar("Berhasil disimpan")
                showTransactionDialog = false
                showEditNameDialog    = false
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
                        text = when {
                            isNewMode          -> "Tambah Kas Baru"
                            selectedAccount != null -> selectedAccount.name
                            else               -> "Detail Kas"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when {

            /* =============================================
             * MODE BARU: hanya admin
             * ============================================= */
            isNewMode -> {
                if (!isAdmin) {
                    // Cashier tidak punya FAB, tapi ini lapisan keamanan ekstra
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Anda tidak memiliki akses untuk membuat kas baru.",
                            color = Color.Gray
                        )
                    }
                } else {
                    CreateKasForm(
                        modifier              = Modifier.padding(paddingValues),
                        kasName               = kasName,
                        initialBalance        = initialBalance,
                        uiState               = uiState,
                        onKasNameChange       = kasViewModel::onKasNameChange,
                        onInitialBalanceChange = kasViewModel::onInitialBalanceChange,
                        onSubmit              = { kasViewModel.createCashAccount() },
                        onSuccess             = onNavigateBack
                    )
                }
            }

            /* =============================================
             * MODE DETAIL
             * ============================================= */
            selectedAccount != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick  = { selectedTab = 0 },
                            text     = { Text("Info & Aksi") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick  = { selectedTab = 1 },
                            text     = { Text("Log Kas") }
                        )
                    }

                    when (selectedTab) {
                        0 -> KasInfoTab(
                            account               = selectedAccount,
                            isAdmin               = isAdmin,
                            uiState               = uiState,
                            onEditNameClick       = {
                                kasViewModel.onKasNameChange(selectedAccount.name)
                                showEditNameDialog = true
                            },
                            onToggleStatusClick   = {
                                kasViewModel.toggleKasStatus(
                                    selectedAccount.id,
                                    selectedAccount.isActive
                                )
                            },
                            onAddBalanceClick     = {
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
                            logs      = cashLogs,
                            isLoading = isLoadingLogs
                        )
                    }
                }

                /* =============================================
                 * DIALOG: Edit Nama — admin only
                 * ============================================= */
                if (showEditNameDialog && isAdmin) {
                    AlertDialog(
                        onDismissRequest = {
                            showEditNameDialog = false
                            kasViewModel.resetForm()
                        },
                        title = { Text("Ubah Nama Kas") },
                        text  = {
                            OutlinedTextField(
                                value         = kasName,
                                onValueChange = kasViewModel::onKasNameChange,
                                label         = { Text("Nama Kas") },
                                singleLine    = true,
                                modifier      = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick  = { kasViewModel.updateCashAccountName(selectedAccount.id) },
                                enabled  = uiState !is KasUiState.Loading
                            ) {
                                if (uiState is KasUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color    = Color.White
                                    )
                                } else {
                                    Text("Simpan")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showEditNameDialog = false
                                kasViewModel.resetForm()
                            }) { Text("Batal") }
                        }
                    )
                }

                /* =============================================
                 * DIALOG: Transaksi Manual — admin only
                 * ============================================= */
                if (showTransactionDialog && isAdmin) {
                    AlertDialog(
                        onDismissRequest = {
                            showTransactionDialog = false
                            kasViewModel.resetForm()
                        },
                        title = {
                            Text(if (transactionType == "in") "Tambah Saldo" else "Kurangi Saldo")
                        },
                        text  = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text  = "Saldo saat ini: ${formatRupiah(selectedAccount.currentBalance)}",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                                OutlinedTextField(
                                    value         = transactionAmount,
                                    onValueChange = kasViewModel::onTransactionAmountChange,
                                    label         = { Text("Nominal (Rp)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    singleLine    = true,
                                    modifier      = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value         = transactionNotes,
                                    onValueChange = kasViewModel::onTransactionNotesChange,
                                    label         = { Text("Keterangan (opsional)") },
                                    modifier      = Modifier.fillMaxWidth(),
                                    maxLines      = 2
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick  = {
                                    if (transactionType == "in")
                                        kasViewModel.addBalance(selectedAccount)
                                    else
                                        kasViewModel.subtractBalance(selectedAccount)
                                },
                                enabled  = uiState !is KasUiState.Loading,
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = if (transactionType == "in")
                                        Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            ) {
                                if (uiState is KasUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color    = Color.White
                                    )
                                } else {
                                    Text(if (transactionType == "in") "Tambah" else "Kurangi")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showTransactionDialog = false
                                kasViewModel.resetForm()
                            }) { Text("Batal") }
                        }
                    )
                }
            }

            /* =============================================
             * LOADING / kas belum termuat
             * ============================================= */
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }
    }
}

/* =============================================
 * CREATE KAS FORM — admin only
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
        if (uiState is KasUiState.Success) onSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Isi data kas baru di bawah ini", color = Color.Gray, fontSize = 14.sp)

        OutlinedTextField(
            value         = kasName,
            onValueChange = onKasNameChange,
            label         = { Text("Nama Kas *") },
            placeholder   = { Text("Contoh: Kas Utama, Kas Cadangan") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value           = initialBalance,
            onValueChange   = onInitialBalanceChange,
            label           = { Text("Saldo Awal (Rp) *") },
            placeholder     = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine      = true,
            modifier        = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick  = onSubmit,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled  = uiState !is KasUiState.Loading
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
 * KAS INFO TAB
 *
 * ADMIN   → kartu saldo + tombol transaksi + pengaturan + info tanggal
 * CASHIER → kartu saldo + banner info read-only + tanggal dibuat
 * ============================================= */
@Composable
fun KasInfoTab(
    account: CashAccount,
    isAdmin: Boolean,
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

        /* --- Kartu Saldo (semua role) --- */
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Saldo Saat Ini",
                        color    = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text       = formatRupiah(account.currentBalance),
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 28.sp
                    )
                    // Status aktif hanya tampil untuk admin
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val statusColor = if (account.isActive)
                            Color(0xFF81C784) else Color(0xFFEF9A9A)
                        Text(
                            text     = if (account.isActive) "● Aktif" else "● Non-aktif",
                            color    = statusColor,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (isAdmin) {
            /* --- Transaksi Manual (admin only) --- */
            item {
                Text("Transaksi Manual", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick  = onAddBalanceClick,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled  = account.isActive
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah")
                    }
                    Button(
                        onClick  = onSubtractBalanceClick,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        enabled  = account.isActive
                    ) {
                        Text("− Kurangi")
                    }
                }
                if (!account.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Aktifkan kas terlebih dahulu untuk melakukan transaksi",
                        color    = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            item { HorizontalDivider() }

            /* --- Pengaturan (admin only) --- */
            item {
                Text("Pengaturan", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick  = onEditNameClick,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("✏️  Ubah Nama Kas") }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick  = onToggleStatusClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = uiState !is KasUiState.Loading,
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (account.isActive)
                            Color(0xFFF44336) else Color(0xFF4CAF50)
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
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                Text("Dibuat: ${formatDate(account.createdAt)}", color = Color.Gray, fontSize = 12.sp)
                Text("Diupdate: ${formatDate(account.updatedAt)}", color = Color.Gray, fontSize = 12.sp)
            }

        } else {
            /* --- Banner read-only (cashier only) --- */
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ℹ️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text     = "Anda login sebagai Kasir. Pengelolaan kas hanya dapat dilakukan oleh Admin.",
                            fontSize = 13.sp,
                            color    = Color(0xFF795548)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dibuat: ${formatDate(account.createdAt)}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

/* =============================================
 * KAS LOG TAB — sama untuk semua role
 * ============================================= */
@Composable
fun KasLogTab(logs: List<CashLog>, isLoading: Boolean) {
    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        logs.isEmpty() -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("Belum ada transaksi pada kas ini", color = Color.Gray) }

        else -> LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) { items(logs) { log -> CashLogItem(log) } }
    }
}

/* =============================================
 * CASH LOG ITEM
 * ============================================= */
@Composable
fun CashLogItem(log: CashLog) {
    val isIn       = log.type == "in"
    val typeColor  = if (isIn) Color(0xFF4CAF50) else Color(0xFFF44336)
    val typeSign   = if (isIn) "+" else "-"
    val typeLabel  = if (isIn) "Masuk" else "Keluar"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(8.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier  = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp).height(40.dp)
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
                Text(formatDate(log.createdAt), color = Color.Gray, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "$typeSign ${formatRupiah(log.amount)}",
                    color      = typeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
                Text(
                    text     = "Saldo: ${formatRupiah(log.runningBalance)}",
                    color    = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}