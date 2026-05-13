package com.example.pos.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pos.model.Product
import com.example.pos.viewmodel.ProductUiState

@Composable
fun ProductScreen(

    name: String,
    price: String,
    stock: String,

    isEditMode: Boolean,

    productUiState: ProductUiState,

    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,

    onAddClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onLogoutClick: () -> Unit

) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Daftar Produk",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = onLogoutClick
            ) {

                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {
                Text("Nama Produk")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = onPriceChange,
            label = {
                Text("Harga")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = stock,
            onValueChange = onStockChange,
            label = {
                Text("Stock")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {

                if (isEditMode) {
                    onUpdateClick()
                } else {
                    onAddClick()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(

                if (isEditMode) {
                    "Perbarui Produk"
                } else {
                    "Tambah Produk"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (productUiState) {

            is ProductUiState.Loading -> {

                CircularProgressIndicator()
            }

            is ProductUiState.Error -> {

                Text(
                    text = productUiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is ProductUiState.Success -> {

                LazyColumn {

                    items(productUiState.products) { product ->

                        ProductItem(
                            product = product,

                            onEditClick = {
                                onEditClick(product)
                            },

                            onDeleteClick = {
                                onDeleteClick(product)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Harga: Rp ${product.price}"
            )

            Text(
                text = "Stock: ${product.stock}"
            )

            Text(
                text = if (product.isActive) {
                    "Status: Aktif"
                } else {
                    "Status: Nonaktif"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = onEditClick
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = onDeleteClick
                ) {
                    Text("Hapus")
                }
            }
        }
    }
}