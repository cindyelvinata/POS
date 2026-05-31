package com.example.pos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pos.model.Customer
import com.example.pos.model.CustomerLog
import com.example.pos.viewmodel.CustomerUiState
import com.example.pos.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    customerViewModel: CustomerViewModel
) {
    val customers by customerViewModel.customers.collectAsStateWithLifecycle()
    val isLoading by customerViewModel.isLoading.collectAsStateWithLifecycle()
    val customerLogs by customerViewModel.customerLogs.collectAsStateWithLifecycle()
    val isLoadingLogs by customerViewModel.isLoadingLogs.collectAsStateWithLifecycle()
    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()

    val name by customerViewModel.name.collectAsStateWithLifecycle()
    val phone by customerViewModel.phone.collectAsStateWithLifecycle()
    val isActive by customerViewModel.isActive.collectAsStateWithLifecycle()
    val selectedCustomer by customerViewModel.selectedCustomer.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CustomerUiState.Success -> {
                snackbarHostState.showSnackbar("Data berhasil disimpan ✓")
                customerViewModel.resetUiState()
            }
            is CustomerUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as CustomerUiState.Error).message)
                customerViewModel.resetUiState()
            }
            else -> {}
        }
    }

    // Filter customers berdasarkan pencarian
    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Manajemen Pelanggan", fontWeight = FontWeight.Bold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Daftar Pelanggan",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Log Aktivitas",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Form Card
                        item {
                            CustomerForm(
                                name = name,
                                phone = phone,
                                isActive = isActive,
                                isEditMode = selectedCustomer != null,
                                uiState = uiState,
                                onNameChange = customerViewModel::onNameChange,
                                onPhoneChange = customerViewModel::onPhoneChange,
                                onIsActiveChange = customerViewModel::onIsActiveChange,
                                onSubmit = { customerViewModel.saveCustomer() },
                                onCancel = { customerViewModel.selectCustomer(null) }
                            )
                        }

                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari nama atau nomor telp...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // List Header
                        item {
                            Text(
                                text = "Daftar Pelanggan (${filteredCustomers.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        // Customers List
                        if (isLoading) {
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
                        } else if (filteredCustomers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tidak ada pelanggan ditemukan", color = Color.Gray)
                                }
                            }
                        } else {
                            items(filteredCustomers) { customer ->
                                CustomerItem(
                                    customer = customer,
                                    onEditClick = { customerViewModel.selectCustomer(customer) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }

                1 -> {
                    if (isLoadingLogs) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (customerLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada log aktivitas", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(customerLogs) { log ->
                                CustomerLogItem(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerForm(
    name: String,
    phone: String,
    isActive: Boolean,
    isEditMode: Boolean,
    uiState: CustomerUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (isEditMode) "Ubah Data Pelanggan" else "Daftar Pelanggan Baru",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nama Pelanggan *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("No. Telepon *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = onIsActiveChange
                )
                Text("Pelanggan Aktif", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = uiState !is CustomerUiState.Loading
                ) {
                    if (uiState is CustomerUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(if (isEditMode) "Perbarui" else "Simpan")
                    }
                }

                if (isEditMode) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerItem(
    customer: Customer,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val badgeColor = if (customer.isActive) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                    val badgeBg = if (customer.isActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                    val badgeLabel = if (customer.isActive) "Aktif" else "Nonaktif"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📞 " + customer.phone,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onEditClick) {
                Text("✏️", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CustomerLogItem(log: CustomerLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = log.description,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDateTime(log.createdAt),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

fun formatDateTime(isoString: String): String {
    return try {
        val date = isoString.substring(0, 10)
        val time = isoString.substring(11, 16)
        val parts = date.split("-")
        "${parts[2]}/${parts[1]}/${parts[0]} $time"
    } catch (e: Exception) {
        isoString
    }
}
