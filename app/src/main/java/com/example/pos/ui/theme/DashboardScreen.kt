package com.example.pos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.pos.model.CashLog
import com.example.pos.ui.theme.kas.formatDate
import com.example.pos.ui.theme.kas.formatRupiah
import com.example.pos.viewmodel.KasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    isAdmin: Boolean,
    onLogoutClick: () -> Unit,
    onNavigateToKas: () -> Unit = {},
    kasViewModel: KasViewModel = viewModel(),
    onNavigateToProduct: () -> Unit = {},
) {
    val totalBalance by kasViewModel.totalBalance.collectAsStateWithLifecycle()
    val todaySalesTotal by kasViewModel.todaySalesTotal.collectAsStateWithLifecycle()
    val recentLogs by kasViewModel.recentLogs.collectAsStateWithLifecycle()
    val isLoading by kasViewModel.isLoadingDashboard.collectAsStateWithLifecycle()
    val totalExpenses by kasViewModel.totalExpenses.collectAsStateWithLifecycle()
    val totalProducts by kasViewModel.totalProducts.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        kasViewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val roleLabel = if (isAdmin) "Admin" else "Kasir"
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.25f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = roleLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                    }
                },
                actions = {
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardSummaryCard(
                            modifier = Modifier.weight(1f),
                            emoji = "💵",
                            label = "Total Saldo Kas",
                            value = formatRupiah(totalBalance),
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                        DashboardSummaryCard(
                            modifier = Modifier.weight(1f),
                            emoji = "🛒",
                            label = "Penjualan Hari Ini",
                            value = formatRupiah(todaySalesTotal),
                            backgroundColor = Color(0xFF26A69A)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardSummaryCard(
                            modifier = Modifier.weight(1f),
                            emoji = "📉",
                            label = "Pengeluaran",
                            value = formatRupiah(totalExpenses),
                            backgroundColor = Color(0xFFEF5350)
                        )
                        DashboardSummaryCard(
                            modifier = Modifier.weight(1f),
                            emoji = "📦",
                            label = "Total Produk",
                            value = totalProducts.toString(),
                            backgroundColor = Color(0xFFF57C00)
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onNavigateToKas,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manajemen Kas",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "›",
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Transaksi Terbaru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Lihat Semua",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToKas() }
                        )
                    }
                }

                if (recentLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada transaksi kas", color = Color.Gray)
                        }
                    }
                } else {
                    items(recentLogs) { log ->
                        DashboardLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSummaryCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    backgroundColor: Color,
    emoji: String
) {
    DashboardSummaryCard(
        modifier = modifier,
        emoji = emoji,
        label = title,
        value = value,
        backgroundColor = backgroundColor
    )
}

@Composable
fun DashboardLogItem(log: CashLog) {
    val isIn = log.type == "in"
    val typeColor = if (isIn) Color(0xFF4CAF50) else Color(0xFFF44336)
    val sign = if (isIn) "+" else "-"
    val typeLabel = if (isIn) "Masuk · " else "Keluar · "

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isIn) "↑" else "↓",
                color = typeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = typeLabel + log.source,
                color = typeColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            Text(
                text = log.notes ?: "-",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = formatDate(log.createdAt),
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Text(
            text = "$sign ${formatRupiah(log.amount)}",
            color = typeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}