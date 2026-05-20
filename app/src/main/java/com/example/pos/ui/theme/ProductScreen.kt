package com.example.pos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
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

            TopBar(
                onLogoutClick = onLogoutClick
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

            ProductTabs(
                onInventoryLogClick =
                    onInventoryLogClick
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

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

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            when (productUiState) {

                is ProductUiState.Loading -> {

                    Box(

                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                is ProductUiState.Error -> {

                    Text(

                        text = productUiState.message,

                        color = Color.Red,

                        modifier =
                            Modifier.padding(16.dp)
                    )
                }

                is ProductUiState.Success -> {

                    LazyColumn(

                        modifier =
                            Modifier.padding(horizontal = 16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(16.dp)
                    ) {

                        items(productUiState.products) { product ->

                            ProductItem(

                                product = product,

                                onEditClick = {
                                    onEditClick(product)
                                },

                                onDeleteClick = {
                                    onDeleteClick(product)
                                },

                                onDetailClick = {
                                    onDetailClick(product)
                                }
                            )
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

    if (selectedProduct != null) {

        Dialog(
            onDismissRequest =
                onDismissDetail
        ) {

            Card(

                shape =
                    RoundedCornerShape(28.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor = Color.White
                    )
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(

                        text = "Detail Produk",

                        fontSize = 30.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )

                    DetailItem(
                        "Nama",
                        selectedProduct.name
                    )

                    DetailItem(
                        "Harga",
                        "Rp ${selectedProduct.price}"
                    )

                    DetailItem(
                        "Stock",
                        "${selectedProduct.stock}"
                    )

                    DetailItem(

                        "Status",

                        if (selectedProduct.isActive) {
                            "Aktif"
                        } else {
                            "Nonaktif"
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )

                    Button(

                        onClick =
                            onDismissDetail,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF005BFF)
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

        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(

            modifier =
                Modifier.padding(24.dp)
        ) {

            Text(

                text =
                    if (isEditMode) {
                        "Edit Produk"
                    } else {
                        "Tambah Produk"
                    },

                fontSize = 32.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(

                value = name,

                onValueChange = onNameChange,

                placeholder = {
                    Text("Nama Produk")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(18.dp),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(

                value = price,

                onValueChange = onPriceChange,

                placeholder = {
                    Text("Harga")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(18.dp),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            OutlinedTextField(

                value = stock,

                onValueChange = onStockChange,

                placeholder = {
                    Text("Stock")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(18.dp),

                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Row(

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Button(

                    onClick = {

                        if (isEditMode) {
                            onUpdateClick()
                        } else {
                            onAddClick()
                        }
                    },

                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(18.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF005BFF)
                        )
                ) {

                    Text(

                        text =
                            if (isEditMode) {
                                "Perbarui Produk"
                            } else {
                                "Tambah Produk"
                            },

                        fontSize = 18.sp
                    )
                }

                if (isEditMode) {

                    Button(

                        onClick =
                            onCancelEdit,

                        modifier =
                            Modifier.height(56.dp),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color.LightGray
                            )
                    ) {

                        Text(
                            text = "Batal",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    onLogoutClick: () -> Unit
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF005BFF))
            .padding(
                horizontal = 16.dp,
                vertical = 18.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            tint = Color.White
        )

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Text(

            text = "Inventory Produk",

            color = Color.White,

            fontSize = 22.sp,

            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Box(

            modifier = Modifier
                .clip(CircleShape)
                .background(
                    Color.White.copy(alpha = 0.2f)
                )
                .clickable {
                    onLogoutClick()
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {

            Text(
                text = "Admin",
                color = Color.White
            )
        }
    }
}

@Composable
fun ProductTabs(
    onInventoryLogClick: () -> Unit
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {

        Column(

            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text = "Daftar Produk",

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

        Column(

            modifier = Modifier
                .weight(1f)
                .clickable {
                    onInventoryLogClick()
                }
                .padding(vertical = 16.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Inventory Log",
                color = Color.Gray
            )
        }
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

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
        ) {

            Text(

                text = product.name,

                fontSize = 26.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Harga: Rp ${product.price}",
                fontSize = 18.sp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row {

                Text(
                    text = "Stock: ",
                    fontSize = 18.sp
                )

                Text(

                    text = "${product.stock}",

                    fontSize = 18.sp,

                    color = Color(0xFF005BFF)
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row {

                Text(
                    text = "Status: ",
                    fontSize = 18.sp
                )

                Text(

                    text =
                        if (product.isActive) {
                            "Aktif"
                        } else {
                            "Nonaktif"
                        },

                    fontSize = 18.sp,

                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                ActionButton(

                    text = "Detail",

                    color = Color(0xFF00B5EF),

                    onClick = onDetailClick
                )

                ActionButton(

                    text = "Edit",

                    color = Color(0xFFFFA500),

                    onClick = onEditClick
                )

                ActionButton(

                    text = "Hapus",

                    color = Color(0xFFFF3B5C),

                    onClick = onDeleteClick
                )
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

        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),

        shape = RoundedCornerShape(16.dp)
    ) {

        Text(text = text)
    }
}

@Composable
fun DetailItem(
    title: String,
    value: String
) {

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
    ) {

        Text(
            text = title,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(

            text = value,

            fontSize = 20.sp,

            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomBar() {

    NavigationBar {

        NavigationBarItem(

            selected = false,

            onClick = { },

            icon = {
                Icon(Icons.Default.Home, null)
            },

            label = {
                Text("Beranda")
            }
        )

        NavigationBarItem(

            selected = false,

            onClick = { },

            icon = {
                Icon(Icons.Default.ShoppingCart, null)
            },

            label = {
                Text("Transaksi")
            }
        )

        NavigationBarItem(

            selected = true,

            onClick = { },

            icon = {
                Icon(Icons.Default.Inventory2, null)
            },

            label = {
                Text("Produk")
            }
        )

        NavigationBarItem(

            selected = false,

            onClick = { },

            icon = {
                Icon(Icons.Default.BarChart, null)
            },

            label = {
                Text("Laporan")
            }
        )

        NavigationBarItem(

            selected = false,

            onClick = { },

            icon = {
                Icon(Icons.Default.Person, null)
            },

            label = {
                Text("Profile")
            }
        )
    }
}