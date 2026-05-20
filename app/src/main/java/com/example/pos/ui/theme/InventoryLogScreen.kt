package com.example.pos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun InventoryLogScreen(

    inventoryLogUiState: InventoryLogUiState,

    onBackToProduct: () -> Unit
) {

    Scaffold(

        topBar = {

            TopBar(
                onLogoutClick = {

                }
            )
        },

        bottomBar = {

            BottomBar()
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {

            InventoryTabs(
                onBackToProduct = onBackToProduct
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            when (inventoryLogUiState) {

                is InventoryLogUiState.Loading -> {

                    Box(

                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                is InventoryLogUiState.Error -> {

                    Text(

                        text = inventoryLogUiState.message,

                        color = Color.Red,

                        modifier =
                            Modifier.padding(16.dp)
                    )
                }

                is InventoryLogUiState.Success -> {

                    LazyColumn(

                        modifier =
                            Modifier.padding(horizontal = 16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        items(inventoryLogUiState.logs) { log ->

                            InventoryLogItem(log)
                        }

                        item {

                            Spacer(
                                modifier =
                                    Modifier.height(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryTabs(
    onBackToProduct: () -> Unit
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        Column(

            modifier = Modifier
                .weight(1f)
                .clickable {
                    onBackToProduct()
                }
                .padding(vertical = 16.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text = "Daftar Produk",

                color = Color.Gray
            )
        }

        Column(

            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text = "Inventory Log",

                color = Color(0xFF005BFF),

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Box(

                modifier = Modifier
                    .width(100.dp)
                    .height(3.dp)
                    .background(Color(0xFF005BFF))
            )
        }
    }
}

@Composable
fun InventoryLogItem(
    log: InventoryLog
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(

            modifier =
                Modifier.padding(20.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text = log.productName,

                    fontSize = 24.sp,

                    fontWeight = FontWeight.Bold
                )

                Box(

                    modifier = Modifier
                        .background(
                            color =
                                if (
                                    log.type == "OUT" ||
                                    log.type.lowercase() == "terjual"
                                ) {
                                    Color(0xFFFFE5E5)
                                } else {
                                    Color(0xFFE5FFF0)
                                },

                            shape =
                                RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 6.dp
                        )
                ) {

                    Text(

                        text =
                            if (
                                log.type == "OUT" ||
                                log.type.lowercase() == "terjual"
                            ) {
                                "TERJUAL"
                            } else {
                                "MASUK"
                            },

                        color =
                            if (
                                log.type == "OUT" ||
                                log.type.lowercase() == "terjual"
                            ) {
                                Color.Red
                            } else {
                                Color(0xFF00A651)
                            },

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

        horizontalArrangement =
            Arrangement.SpaceBetween
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