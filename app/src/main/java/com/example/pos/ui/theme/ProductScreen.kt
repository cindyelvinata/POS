package com.example.pos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pos.model.Product
import com.example.pos.viewmodel.ProductUiState

@OptIn(ExperimentalMaterial3Api::class)
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
    onCancelEdit: () -> Unit,
    onEditClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onDetailClick: (Product) -> Unit,
    onInventoryLogClick: () -> Unit,
    onLogoutClick: () -> Unit,
    selectedProduct: Product?,
    onDismissDetail: () -> Unit
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
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
            ProductSectionTabs(
                selectedTabIndex = 0,
                onProductClick = {},
                onInventoryLogClick = onInventoryLogClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ProductForm(
                        name = name,
                        price = price,
                        stock = stock,
                        isEditMode = isEditMode,
                        onNameChange = onNameChange,
                        onPriceChange = onPriceChange,
                        onStockChange = onStockChange,
                        onAddClick = onAddClick,
                        onUpdateClick = onUpdateClick,
                        onCancelEdit = onCancelEdit
                    )
                }

                when (productUiState) {
                    is ProductUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is ProductUiState.Error -> {
                        item {
                            Text(
                                text = productUiState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    is ProductUiState.Success -> {
                        if (productUiState.products.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Belum ada produk", color = Color.Gray)
                                    }
                                }
                            }
                        } else {
                            items(productUiState.products) { product ->
                                ProductItem(
                                    product = product,
                                    onEditClick = { onEditClick(product) },
                                    onDeleteClick = { onDeleteClick(product) },
                                    onDetailClick = { onDetailClick(product) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (selectedProduct != null) {
        Dialog(onDismissRequest = onDismissDetail) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Detail Produk",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DetailItem("Nama", selectedProduct.name)
                    DetailItem("Harga", "Rp ${selectedProduct.price}")
                    DetailItem("Stok", "${selectedProduct.stock}")
                    DetailItem(
                        "Status",
                        if (selectedProduct.isActive) "Aktif" else "Nonaktif"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismissDetail,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductForm(
    name: String,
    price: String,
    stock: String,
    isEditMode: Boolean,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (isEditMode) "Edit Produk" else "Tambah Produk",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Nama Produk") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = onPriceChange,
                placeholder = { Text("Harga (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = stock,
                onValueChange = onStockChange,
                placeholder = { Text("Stok") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { if (isEditMode) onUpdateClick() else onAddClick() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isEditMode) "Perbarui" else "Tambah",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isEditMode) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductSectionTabs(
    selectedTabIndex: Int,
    onProductClick: () -> Unit,
    onInventoryLogClick: () -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = onProductClick,
            text = {
                Text(
                    "Daftar Produk",
                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        )

        Tab(
            selected = selectedTabIndex == 1,
            onClick = onInventoryLogClick,
            text = {
                Text(
                    "Inventory Log",
                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

@Composable
fun ProductItem(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                val isActive = product.isActive
                val badgeBg = if (isActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                val badgeColor = if (isActive) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                val badgeLabel = if (isActive) "Aktif" else "Nonaktif"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Rp ${product.price}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Stok: ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${product.stock}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("Detail", Color(0xFF00B5EF), onDetailClick)
                ActionButton("Edit", Color(0xFFF57C00), onEditClick)
                ActionButton("Hapus", Color(0xFFE53935), onDeleteClick)
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}

@Composable
fun DetailItem(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
    ) {
        Text(text = title, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}