package com.example.pos.ui.theme.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pos.model.Expense
import com.example.pos.ui.theme.kas.formatDate
import com.example.pos.ui.theme.kas.formatRupiah
import com.example.pos.viewmodel.ExpenseUiState
import com.example.pos.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    isAdmin: Boolean,
    expenseViewModel: ExpenseViewModel
) {
    val expenses by expenseViewModel.expenses.collectAsStateWithLifecycle()
    val isLoadingExpenses by expenseViewModel.isLoadingExpenses.collectAsStateWithLifecycle()
    val cashAccounts by expenseViewModel.cashAccounts.collectAsStateWithLifecycle()
    val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()

    val description by expenseViewModel.description.collectAsStateWithLifecycle()
    val amount by expenseViewModel.amount.collectAsStateWithLifecycle()
    val selectedCashAccount by expenseViewModel.selectedCashAccount.collectAsStateWithLifecycle()
    val date by expenseViewModel.date.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }

    var editDescriptionText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ExpenseUiState.Success -> {
                snackbarHostState.showSnackbar("Berhasil disimpan ✓")
                showAddDialog = false
                showDetailDialog = false
                expenseViewModel.resetUiState()
            }
            is ExpenseUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ExpenseUiState.Error).message)
                expenseViewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Laporan Pengeluaran", fontWeight = FontWeight.Bold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        expenseViewModel.resetForm()
                        expenseViewModel.loadActiveCashAccounts()
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Catat Pengeluaran", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (!isAdmin) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ℹ️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Anda login sebagai Kasir. Pencatatan dan pembatalan pengeluaran kas hanya dapat dilakukan oleh Admin.",
                            fontSize = 12.sp,
                            color = Color(0xFF795548),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (isLoadingExpenses) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada pencatatan pengeluaran", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onClick = {
                                selectedExpense = expense
                                editDescriptionText = expense.description
                                showDetailDialog = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // =============================================
        // DIALOG: Tambah Pengeluaran Baru
        // =============================================
        if (showAddDialog && isAdmin) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Catat Pengeluaran Baru", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = expenseViewModel::onDescriptionChange,
                            label = { Text("Deskripsi / Keterangan *") },
                            placeholder = { Text("Contoh: Membeli sapu, Zakat, Bayar air") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = expenseViewModel::onAmountChange,
                            label = { Text("Nominal (Rp) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = date,
                            onValueChange = expenseViewModel::onDateChange,
                            label = { Text("Tanggal (YYYY-MM-DD) *") },
                            placeholder = { Text("Format: YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Custom Dropdown untuk memilih Akun Kas
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedCashAccount?.name ?: "Pilih Akun Kas",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sumber Pembayaran (Kas) *") },
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                cashAccounts.forEach { account ->
                                    DropdownMenuItem(
                                        text = { Text("${account.name} (Saldo: ${formatRupiah(account.currentBalance)})") },
                                        onClick = {
                                            expenseViewModel.onSelectedCashAccountChange(account)
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { expenseViewModel.addExpense() },
                        enabled = uiState !is ExpenseUiState.Loading
                    ) {
                        if (uiState is ExpenseUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Simpan")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
                }
            )
        }

        // =============================================
        // DIALOG: Detail & Opsi Edit / Batalkan Pengeluaran
        // =============================================
        if (showDetailDialog && selectedExpense != null) {
            val expense = selectedExpense!!
            AlertDialog(
                onDismissRequest = { showDetailDialog = false },
                title = { Text("Detail Pengeluaran", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DetailRow("Tanggal", formatDate(expense.createdAt))
                        DetailRow("Jumlah", formatRupiah(expense.amount))
                        DetailRow("Kas Pembayar", expense.cashAccount?.name ?: "-")

                        val statusLabel = if (expense.isCancelled) "Dibatalkan" else "Aktif"
                        val statusColor = if (expense.isCancelled) Color.Red else Color(0xFF2E7D32)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status", color = Color.Gray, fontSize = 13.sp)
                            Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (isAdmin && !expense.isCancelled) {
                            Text("Ubah Deskripsi", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = editDescriptionText,
                                onValueChange = { editDescriptionText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Deskripsi pengeluaran") }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    expenseViewModel.updateExpenseDescription(expense.id, editDescriptionText)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState !is ExpenseUiState.Loading
                            ) {
                                Text("Simpan Deskripsi Baru")
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { expenseViewModel.cancelExpense(expense) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                enabled = uiState !is ExpenseUiState.Loading
                            ) {
                                Text("❌ Batalkan Pengeluaran (Refund)")
                            }
                        } else if (expense.isCancelled) {
                            Text(
                                "Pengeluaran ini telah dibatalkan. Saldo kas telah dikembalikan.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            DetailRow("Keterangan", expense.description)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDetailDialog = false }) { Text("Tutup") }
                }
            )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit
) {
    val isCancelled = expense.isCancelled
    val alpha = if (isCancelled) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = expense.description,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                            textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "💳 " + (expense.cashAccount?.name ?: "-"),
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = alpha)
                        )
                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = formatDate(expense.createdAt),
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = alpha)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "- " + formatRupiah(expense.amount),
                        color = if (isCancelled) Color.Gray else Color(0xFFEF5350),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (isCancelled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Batal",
                                color = Color.Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(text = value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
