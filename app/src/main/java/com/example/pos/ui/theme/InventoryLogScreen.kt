package com.example.pos.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pos.model.InventoryLog
import com.example.pos.viewmodel.InventoryLogUiState

@Composable
fun InventoryLogScreen(
    inventoryLogUiState: InventoryLogUiState
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Inventory Log",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (inventoryLogUiState) {

            is InventoryLogUiState.Loading -> {

                CircularProgressIndicator()
            }

            is InventoryLogUiState.Error -> {

                Text(
                    text = inventoryLogUiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is InventoryLogUiState.Success -> {

                LazyColumn {

                    items(inventoryLogUiState.logs) { log ->

                        InventoryLogItem(log)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = log.productName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tipe: ${log.type}"
            )

            Text(
                text = "Jumlah: ${log.quantity}"
            )

            Text(
                text = "Stock Sebelum: ${log.stockBefore}"
            )

            Text(
                text = "Stock Sesudah: ${log.stockAfter}"
            )

            Text(
                text = "Keterangan: ${log.description}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = log.createdAt
                )
            }
        }
    }
}