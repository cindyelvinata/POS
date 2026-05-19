package com.example.pos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pos.data.model.CashLog
import com.example.pos.ui.theme.kas.formatDate
import com.example.pos.ui.theme.kas.formatRupiah
import com.example.pos.viewmodel.KasViewModel
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isAdmin: Boolean,
    onLogoutClick: () -> Unit,
    onNavigateToKas: () -> Unit = {},
    kasViewModel: KasViewModel = viewModel(),
    onNavigateToProduct: () -> Unit = {},
) {

    // ================================
    // STATE DASHBOARD
    // ================================
    val totalBalance by kasViewModel.totalBalance.collectAsStateWithLifecycle()
    val todaySalesTotal by kasViewModel.todaySalesTotal.collectAsStateWithLifecycle()
    val recentLogs by kasViewModel.recentLogs.collectAsStateWithLifecycle()
    val isLoading by kasViewModel.isLoadingDashboard.collectAsStateWithLifecycle()

    // ================================
    // LOAD DATA AWAL
    // ================================
    LaunchedEffect(Unit) {
        kasViewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {

                    // Tombol logout lama tetap dipertahankan
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        // ================================
        // LOADING
        // ================================
        if (isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            // ================================
            // CONTENT DASHBOARD
            // ================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ================================
                // RINGKASAN
                // ================================
                item {

                    Text(
                        text = "Ringkasan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Total saldo kas
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Saldo Kas",
                            value = formatRupiah(totalBalance),
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            emoji = "💰"
                        )

                        // Penjualan hari ini
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Penjualan Hari Ini",
                            value = formatRupiah(todaySalesTotal),
                            backgroundColor = Color(0xFF4CAF50),
                            emoji = "🛒"
                        )
                    }
                }

                // ================================
                // BUTTON KE HALAMAN KAS
                // ================================
                item {

                    OutlinedButton(
                        onClick = onNavigateToKas,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💼 Kelola Kas →")
                    }
                }

                item {

                    OutlinedButton(
                        onClick = onNavigateToProduct,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text("📦 Kelola Produk →")
                    }
                }

                // ================================
                // JUDUL TRANSAKSI
                // ================================
                item {

                    Text(
                        text = "Transaksi Kas Terbaru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // ================================
                // EMPTY STATE
                // ================================
                if (recentLogs.isEmpty()) {

                    item {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "Belum ada transaksi kas",
                                color = Color.Gray
                            )
                        }
                    }

                } else {

                    // ================================
                    // LIST LOG KAS
                    // ================================
                    items(recentLogs) { log ->

                        DashboardLogItem(log = log)
                    }
                }
            }
        }
    }
}

/* =========================================================
 * SUMMARY CARD
 * ========================================================= */
@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    backgroundColor: Color,
    emoji: String
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            Text(
                text = emoji,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/* =========================================================
 * ITEM TRANSAKSI DASHBOARD
 * ========================================================= */
@Composable
fun DashboardLogItem(
    log: CashLog
) {

    val isIn = log.type == "in"

    val typeColor =
        if (isIn) Color(0xFF4CAF50)
        else Color(0xFFF44336)

    val sign =
        if (isIn) "+"
        else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ICON TYPE
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    typeColor.copy(alpha = 0.15f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = if (isIn) "↑" else "↓",
                color = typeColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // INFO
        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = log.notes ?: log.source,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )

            Text(
                text = formatDate(log.createdAt),
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        // NOMINAL
        Text(
            text = "$sign ${formatRupiah(log.amount)}",
            color = typeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
