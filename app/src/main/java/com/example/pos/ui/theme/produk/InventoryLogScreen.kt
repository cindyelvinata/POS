package com.example.pos.ui.theme.produk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.model.InventoryLog
import com.example.pos.viewmodel.InventoryLogUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryLogScreen(
    inventoryLogUiState: InventoryLogUiState,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Produk",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (inventoryLogUiState) {
                is InventoryLogUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is InventoryLogUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = inventoryLogUiState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is InventoryLogUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(inventoryLogUiState.logs) { log ->
                            InventoryLogItem(log)
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryLogItem(
    log: InventoryLog
) {

    val type = log.type.lowercase()

    val isKeluar =
        type == "terjual" ||
                (
                        type == "edit" &&
                                log.stockAfter < log.stockBefore
                        )

    val badgeText =
        if (isKeluar) {
            "KELUAR"
        } else {
            "MASUK"
        }

    val badgeBg =
        if (isKeluar) {
            Color(0xFFFFE5E5)
        } else {
            Color(0xFFE5FFF0)
        }

    val badgeColor =
        if (isKeluar) {
            Color.Red
        } else {
            Color(0xFF00A651)
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = log.productName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = badgeBg,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 6.dp
                        )
                ) {

                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            InventoryInfoRow(
                title = "Jumlah",
                value = "${log.quantity}"
            )

            InventoryInfoRow(
                title = "Stock Sebelum",
                value = "${log.stockBefore}"
            )

            InventoryInfoRow(
                title = "Stock Sesudah",
                value = "${log.stockAfter}"
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = log.description,
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = log.createdAt,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun InventoryInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 16.sp
        )

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
