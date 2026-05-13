package com.example.pos.ui.theme.kas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pos.data.model.CashAccount
import com.example.pos.viewmodel.KasUiState
import com.example.pos.viewmodel.KasViewModel
import java.text.NumberFormat
import java.util.Locale

/* =============================================
 * HELPER FUNCTIONS
 * ============================================= */
fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount)
}

fun formatDate(isoString: String): String {
    return try {
        val date = isoString.substring(0, 10)
        val parts = date.split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (e: Exception) {
        isoString
    }
}

/* =============================================
 * KAS SCREEN
 *
 * isAdmin = true  (ADMIN)
 *   → FAB tambah kas muncul
 *   → Semua kas tampil (aktif + non-aktif)
 *   → Badge status tampil di kartu
 *
 * isAdmin = false (CASHIER)
 *   → Tidak ada FAB
 *   → Hanya kas aktif yang tampil
 *   → Tidak ada badge status
 * ============================================= */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToAddKas: () -> Unit,
    onNavigateToDetailKas: (String) -> Unit,
    kasViewModel: KasViewModel = viewModel()
) {
    val cashAccounts by kasViewModel.cashAccounts.collectAsStateWithLifecycle()
    val isLoading    by kasViewModel.isLoadingAccounts.collectAsStateWithLifecycle()
    val uiState      by kasViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        kasViewModel.loadCashAccounts()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is KasUiState.Error) {
            snackbarHostState.showSnackbar((uiState as KasUiState.Error).message)
            kasViewModel.resetUiState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Kas", fontWeight = FontWeight.Bold) },
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
        /*
         * FAB hanya muncul untuk ADMIN.
         */
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onNavigateToAddKas,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Kas", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                cashAccounts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Belum ada data kas", color = Color.Gray)
                        if (isAdmin) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap + untuk menambah kas baru",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        /*
                         * Admin → lihat semua kas (aktif + non-aktif)
                         * Cashier → hanya lihat kas aktif
                         */
                        val displayed = if (isAdmin) cashAccounts
                        else cashAccounts.filter { it.isActive }

                        items(displayed) { account ->
                            KasCard(
                                account  = account,
                                isAdmin  = isAdmin,
                                onClick  = { onNavigateToDetailKas(account.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* =============================================
 * KAS CARD
 *
 * isAdmin → tampilkan badge Aktif/Non-aktif
 * Cashier → tidak perlu tahu status, hanya lihat saldo
 * ============================================= */
@Composable
fun KasCard(
    account: CashAccount,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    val cardAlpha    = if (account.isActive) 1f else 0.55f
    val statusColor  = if (account.isActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
    val statusText   = if (account.isActive) "Aktif" else "Non-aktif"
    val iconBg       = if (account.isActive)
        MaterialTheme.colorScheme.primaryContainer else Color(0xFFE0E0E0)
    val iconColor    = if (account.isActive)
        MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier  = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar inisial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = account.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp,
                    color      = iconColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = account.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 16.sp,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = cardAlpha)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = formatRupiah(account.currentBalance),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = if (account.isActive)
                        MaterialTheme.colorScheme.primary else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = "Dibuat: ${formatDate(account.createdAt)}",
                    fontSize = 11.sp,
                    color    = Color.Gray
                )
            }

            // Badge status hanya untuk ADMIN
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = statusText,
                        color      = statusColor,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}