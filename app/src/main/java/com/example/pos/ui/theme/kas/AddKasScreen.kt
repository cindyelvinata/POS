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
import com.example.pos.model.CashAccount
import com.example.pos.model.CashLog
import com.example.pos.viewmodel.KasUiState
import com.example.pos.viewmodel.KasViewModel

/* =============================================
 * ADD KAS SCREEN
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
    val transactionAmount by kasViewModel.transactionAmount.collectAsStateWithLifecycle()
    val transactionNotes  by kasViewModel.transactionNotes.collectAsStateWithLifecycle()
    val initialBalance    by kasViewModel.initialBalance.collectAsStateWithLifecycle()

    val selectedAccount   = cashAccounts.find { it.id == cashAccountId }
    val isNewMode         = cashAccountId == null

    var showEditNameDialog    by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var transactionType       by remember { mutableStateOf("in") }
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
                snackbarHostState.showSnackbar("Berhasil disimpan ✓")
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
                            isNewMode               -> "Tambah Kas Baru"
                            selectedAccount != null -> selectedAccount.name
                            else                    -> "Detail Kas"
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
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when {

            /* =============================================
             * MODE BARU — Form buat kas baru (admin only)
             * ============================================= */
            isNewMode -> {
                if (!isAdmin) {
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
                        modifier               = Modifier.padding(paddingValues),
                        kasName                = kasName,
                        initialBalance         = initialBalance,
                        uiState                = uiState,
                        onKasNameChange        = kasViewModel::onKasNameChange,
                        onInitialBalanceChange = kasViewModel::onInitialBalanceChange,
                        onSubmit               = { kasViewModel.createCashAccount() },
                        onSuccess              = onNavigateBack
                    )
                }
            }

            /* =============================================
             * MODE DETAIL — Tab "Info & Aksi" + "Log Kas"
             * ============================================= */
            selectedAccount != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {

                    /* --- Tab Row --- */
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor   = MaterialTheme.colorScheme.surface,
                        contentColor     = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick  = { selectedTab = 0 },
                            text     = {
                                Text(
                                    "Info & Aksi",
                                    fontWeight = if (selectedTab == 0)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick  = { selectedTab = 1 },
                            text     = {
                                Text(
                                    "Log Kas",
                                    fontWeight = if (selectedTab == 1)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    /* --- Tab Content --- */
                    when (selectedTab) {
                        0 -> KasInfoTab(
                            account                = selectedAccount,
                            isAdmin                = isAdmin,
                            uiState                = uiState,
                            onEditNameClick        = {
                                kasViewModel.onKasNameChange(selectedAccount.name)
                                showEditNameDialog = true
                            },
                            onToggleStatusClick    = {
                                kasViewModel.toggleKasStatus(
                                    selectedAccount.id,
                                    selectedAccount.isActive
                                )
                            },
                            onAddBalanceClick      = {
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
                 * DIALOG: Edit Nama Kas — admin only
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
                                onClick = {
                                    kasViewModel.updateCashAccountName(selectedAccount.id)
                                },
                                enabled = uiState !is KasUiState.Loading
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
                    val isAddMode = transactionType == "in"
                    AlertDialog(
                        onDismissRequest = {
                            showTransactionDialog = false
                            kasViewModel.resetForm()
                        },
                        title = {
                            Text(if (isAddMode) "Tambah Saldo" else "Kurangi Saldo")
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text     = "Saldo saat ini: ${formatRupiah(selectedAccount.currentBalance)}",
                                    color    = Color.Gray,
                                    fontSize = 13.sp
                                )
                                OutlinedTextField(
                                    value           = transactionAmount,
                                    onValueChange   = kasViewModel::onTransactionAmountChange,
                                    label           = { Text("Nominal (Rp)") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    singleLine      = true,
                                    modifier        = Modifier.fillMaxWidth()
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
                                onClick = {
                                    if (isAddMode) kasViewModel.addBalance(selectedAccount)
                                    else kasViewModel.subtractBalance(selectedAccount)
                                },
                                enabled = uiState !is KasUiState.Loading,
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = if (isAddMode)
                                        Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            ) {
                                if (uiState is KasUiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color    = Color.White
                                    )
                                } else {
                                    Text(if (isAddMode) "Tambah" else "Kurangi")
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
        Text(
            "Isi data kas baru di bawah ini",
            color    = Color.Gray,
            fontSize = 14.sp
        )

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
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        /* --- Kartu Saldo Utama --- */
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text     = "Saldo Saat Ini",
                        color    = Color.White.copy(alpha = 0.80f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text       = formatRupiah(account.currentBalance),
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 30.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Badge status Aktif/Non-aktif — tampil untuk semua (bukan hanya admin)
                    // karena cashier juga perlu tahu apakah kas bisa digunakan
                    val badgeColor = if (account.isActive)
                        Color(0xFF81C784) else Color(0xFFEF9A9A)
                    val badgeText  = if (account.isActive) "Aktif" else "Non-aktif"

                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.20f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text       = badgeText,
                            color      = badgeColor,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        /* --- Transaksi Manual (admin + kas aktif) --- */
        if (isAdmin) {
            item {
                Text(
                    "Transaksi Manual",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick  = onAddBalanceClick,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled  = account.isActive
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick  = onSubtractBalanceClick,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        ),
                        enabled  = account.isActive
                    ) {
                        Text("− Kurangi", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (!account.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Aktifkan kas terlebih dahulu untuk transaksi",
                        color    = Color(0xFFF44336).copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            item { HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f)) }

            /* --- Pengaturan --- */
            item {
                Text(
                    "Pengaturan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick  = onEditNameClick,
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("✏️  Ubah Nama Kas")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick  = onToggleStatusClick,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    enabled  = uiState !is KasUiState.Loading,
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (account.isActive)
                            Color(0xFFF44336) else Color(0xFF4CAF50)
                    )
                ) {
                    if (uiState is KasUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        val icon = if (account.isActive) "🚫" else "✅"
                        val label = if (account.isActive) "Non-aktifkan Kas" else "Aktifkan Kas"
                        Text("$icon  $label")
                    }
                }
            }
        } else {
            /* --- Banner Read-only (cashier) --- */
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ℹ️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text     = "Anda login sebagai Kasir. Pengelolaan kas hanya dapat dilakukan oleh Admin.",
                            fontSize = 13.sp,
                            color    = Color(0xFF795548),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        /* --- Footer tanggal --- */
        item {
            HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Dibuat: ${formatDate(account.createdAt)}",
                color    = Color.Gray,
                fontSize = 12.sp
            )
            if (isAdmin) {
                Text(
                    "Diupdate: ${formatDate(account.updatedAt)}",
                    color    = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/* =============================================
 * KAS LOG TAB — histori transaksi
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
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📋", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Belum ada transaksi pada kas ini", color = Color.Gray)
            }
        }

        else -> LazyColumn(
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log -> CashLogItem(log) }
        }
    }
}

/* =============================================
 * CASH LOG ITEM
 * ============================================= */
@Composable
fun CashLogItem(log: CashLog) {
    val isIn      = log.type == "in"
    val typeColor = if (isIn) Color(0xFF4CAF50) else Color(0xFFF44336)
    val typeSign  = if (isIn) "+" else "-"
    val typeLabel = if (isIn) "Masuk" else "Keluar"

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left border berwarna
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .background(typeColor, RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Baris 1: label type + source
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text       = typeLabel,
                        color      = typeColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp
                    )
                    Text("·", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text     = log.source,
                        color    = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                // Baris 2: notes / keterangan
                if (!log.notes.isNullOrEmpty()) {
                    Text(
                        text     = log.notes,
                        color    = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                // Baris 3: tanggal
                Text(
                    text     = formatDate(log.createdAt),
                    color    = Color.Gray,
                    fontSize = 11.sp
                )
            }

            // Kanan: nominal + running balance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "$typeSign ${formatRupiah(log.amount)}",
                    color      = typeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
                Text(
                    text     = "Saldo: ${formatRupiah(log.runningBalance)}",
                    color    = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}