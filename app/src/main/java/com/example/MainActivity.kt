package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.Pegawai
import com.example.data.entity.SlipGaji
import com.example.data.entity.PengeluaranLain
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.data.model.SlipGajiDenganPegawai
import com.example.data.utils.PayrollCalculator
import com.example.ui.PayrollViewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold")
                ) { innerPadding ->
                    PayrollScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PayrollScreen(
    modifier: Modifier = Modifier,
    viewModel: PayrollViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val pegawaiList by viewModel.allPegawai.collectAsState()
    val slipsList by viewModel.filteredSlips.collectAsState()

    val totalSpent by viewModel.totalPayrollSpent.collectAsState()
    val activeCount by viewModel.activeEmployeeCount.collectAsState()
    val averagePayroll by viewModel.averagePayrollPaid.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Branding Header (Natural Tones Custom Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profile Avatar Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = "Apotek Nirmala Farma",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Halo, Rekan Keuangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Notification Bell Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { 
                        Toast.makeText(context, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Modern Tab Navigation (Single view layout)
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Dasbor & Slip", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                modifier = Modifier.testTag("tab_dashboard")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { 
                    viewModel.resetSlipForm()
                    activeTab = 1 
                },
                text = { Text("Input Gaji", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Calculations") },
                modifier = Modifier.testTag("tab_calculations")
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Pegawai", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Employees") },
                modifier = Modifier.testTag("tab_employees")
            )
        }

        // Screen Body depending on Active Tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> DashboardTab(
                    viewModel = viewModel,
                    slipsList = slipsList,
                    totalSpent = totalSpent,
                    activeCount = activeCount,
                    avgPayroll = averagePayroll,
                    onNavigateToCalculation = { activeTab = 1 }
                )
                1 -> CalculationInputTab(
                    viewModel = viewModel,
                    pegawaiList = pegawaiList,
                    onSaved = {
                        activeTab = 0
                        Toast.makeText(context, "Slip Gaji berhasil disimpan!", Toast.LENGTH_LONG).show()
                    },
                    onNavigateToEmployees = { activeTab = 2 }
                )
                2 -> EmployeesTab(
                    viewModel = viewModel,
                    pegawaiList = pegawaiList
                )
            }
        }
    }
}

// ==========================================
// TAB 1: DASHBOARD & SLIPS RECORD LIST
// ==========================================
@Composable
fun DashboardTab(
    viewModel: PayrollViewModel,
    slipsList: List<SlipGajiDenganPegawai>,
    totalSpent: Double,
    activeCount: Int,
    avgPayroll: Double,
    onNavigateToCalculation: () -> Unit
) {
    var selectedSlipForDetail by remember { mutableStateOf<SlipGajiDenganPegawai?>(null) }
    var showExpensesDialog by remember { mutableStateOf(false) }
    val monthlyExpenses by viewModel.filteredExpenses.collectAsState()
    val totalExpenses = totalSpent + (monthlyExpenses?.totalPengeluaran ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Month Selection (Natural Tones styled)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (viewModel.selectedMonth > 1) {
                        viewModel.selectedMonth -= 1
                    } else {
                        viewModel.selectedMonth = 12
                        viewModel.selectedYear -= 1
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Bulan Sebelumnya",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "${PayrollCalculator.getIndonesianMonth(viewModel.selectedMonth)} ${viewModel.selectedYear}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    if (viewModel.selectedMonth < 12) {
                        viewModel.selectedMonth += 1
                    } else {
                        viewModel.selectedMonth = 1
                        viewModel.selectedYear += 1
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Bulan Selanjutnya",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Main Salary Card with Dynamic Expenses Realization (Natural Tones)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary // SageTint (0xFFDCE7E1)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL REALISASI PENGELUARAN APOTEK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = PayrollCalculator.formatRupiah(totalExpenses),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Total Gaji Staf",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Text(
                                PayrollCalculator.formatRupiah(totalSpent),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Total Operasional",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Text(
                                PayrollCalculator.formatRupiah(monthlyExpenses?.totalPengeluaran ?: 0.0),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Already Paid Pill
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(NaturalSuccess, CircleShape)
                        )
                        Text(
                            text = "SUDAH DITRANSFER • PERIODE AKTIF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NaturalSuccess,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        // Secondary Sub-stats Row (Natural Tones)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Employee Count Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Staf Terbayar",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$activeCount Orang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }

            // Average Payroll Card
            Card(
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rata-rata Gaji",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = PayrollCalculator.formatRupiah(avgPayroll),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Card Pengeluaran Operasional Apotek (Pilihan pengeluaran lain-lain)
        val expenses = monthlyExpenses
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Operasional",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Pengeluaran Operasional",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.loadExpenses(expenses)
                            showExpensesDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Pengeluaran",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseItemRow(label = "Biaya Listrik", amount = expenses?.biayaListrik ?: 0.0)
                    ExpenseItemRow(label = "Biaya Internet", amount = expenses?.biayaInternet ?: 0.0)
                    ExpenseItemRow(label = "Biaya PDAM", amount = expenses?.biayaPdam ?: 0.0)
                    ExpenseItemRow(label = "ATK (Alat Tulis Kantor)", amount = expenses?.biayaAtk ?: 0.0)
                    ExpenseItemRow(label = "Plastik Pembungkus", amount = expenses?.biayaPlastik ?: 0.0)
                    ExpenseItemRow(label = "Air Minum Pegawai & Toko", amount = expenses?.biayaAirMinum ?: 0.0)
                    ExpenseItemRow(label = "Lain-lain / Tak Terduga", amount = expenses?.biayaLainLain ?: 0.0)
                    
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Operasional",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                        Text(
                            text = PayrollCalculator.formatRupiah(expenses?.totalPengeluaran ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Section header for sheets
        Text(
            text = "Daftar Slip Gaji Periode Ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (slipsList.isEmpty()) {
            // Empty State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty Data",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Slip Gaji",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seluruh perhitungan bulanan pegawai untuk bulan ${PayrollCalculator.getIndonesianMonth(viewModel.selectedMonth)} belum dihitung.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToCalculation,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Hitung Gaji")
                    }
                }
            }
        } else {
            // Recycler loop instead of nested LazyColumn to prevent Compose scrolling exceptions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slipsList.forEach { slipWithPegawai ->
                    SlipSalaryCard(
                        slipWithPegawai = slipWithPegawai,
                        onClick = { selectedSlipForDetail = slipWithPegawai },
                        onDelete = { viewModel.deleteSlipGaji(slipWithPegawai.slipGaji) }
                    )
                }
            }
        }
    }

    // Pay Slip detailed visual invoice Modal pop-up
    selectedSlipForDetail?.let { slip ->
        PaySlipDetailDialog(
            slipWithPegawai = slip,
            onDismiss = { selectedSlipForDetail = null }
        )
    }

    if (showExpensesDialog) {
        ManageExpensesDialog(
            viewModel = viewModel,
            onDismiss = { showExpensesDialog = false }
        )
    }
}

@Composable
fun SlipSalaryCard(
    slipWithPegawai: SlipGajiDenganPegawai,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val pegawai = slipWithPegawai.pegawai
    val slip = slipWithPegawai.slipGaji

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("slip_salary_card_${slip.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pegawai?.nama ?: "Pegawai Terhapus",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Jabatan badge chip
                    Surface(
                        color = getJabatanColor(pegawai?.jabatan ?: "Umum").copy(alpha = 0.15f),
                        contentColor = getJabatanColor(pegawai?.jabatan ?: "Umum"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = pegawai?.jabatan ?: "Umum",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus slip",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total Gaji Diterima",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PayrollCalculator.formatRupiah(slip.totalGaji),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Lihat Slip", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// TAB 2: DETAILED COMPONENT SALARY INPUT
// ==========================================
@Composable
fun CalculationInputTab(
    viewModel: PayrollViewModel,
    pegawaiList: List<Pegawai>,
    onSaved: () -> Unit,
    onNavigateToEmployees: () -> Unit
) {
    val context = LocalContext.current
    var isPegawaiDropdownExpanded by remember { mutableStateOf(false) }

    // Synchronize selected employee on list change to prevent stale or deleted employee references
    LaunchedEffect(pegawaiList) {
        val currentSelected = viewModel.selectedPegawaiForSlip
        if (pegawaiList.isNotEmpty()) {
            if (currentSelected == null || !pegawaiList.any { it.id == currentSelected.id }) {
                viewModel.selectedPegawaiForSlip = pegawaiList.first()
            }
        } else {
            viewModel.selectedPegawaiForSlip = null
        }
    }

    if (pegawaiList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "No employees",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Pegawai Belum Terdaftar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Harap daftarkan sedikitnya satu pegawai Apotek terlebih dahulu agar dapat memasukkan/menghitung slip gaji.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToEmployees,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Daftarkan")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daftar Pegawai Baru")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Hitung Komponen Gaji Bulanan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Masukkan parameter operasional pegawai untuk periode ${PayrollCalculator.getIndonesianMonth(viewModel.selectedMonth)} ${viewModel.selectedYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 1. Employee Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Pilih Pegawai Apotek",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { isPegawaiDropdownExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = viewModel.selectedPegawaiForSlip?.nama ?: "Pilih Pegawai",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = viewModel.selectedPegawaiForSlip?.jabatan ?: "Posisi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.Menu, contentDescription = "Dropdown")
                            }

                            DropdownMenu(
                                expanded = isPegawaiDropdownExpanded,
                                onDismissRequest = { isPegawaiDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                pegawaiList.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(p.nama, fontWeight = FontWeight.Bold)
                                                Text(p.jabatan, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectedPegawaiForSlip = p
                                            isPegawaiDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Input Fields for variables
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Variabel Jam & Penjualan Bulan Ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // Absensi / Total Hari Kerja
                        OutlinedTextField(
                            value = viewModel.slipWorkDaysInput,
                            onValueChange = { viewModel.slipWorkDaysInput = it },
                            label = { Text("Jumlah Hari Kerja Masuk") },
                            placeholder = { Text("Maksimal 31 hari") },
                            suffix = { Text("Hari") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Real Penjualan Retail Apotek
                        OutlinedTextField(
                            value = viewModel.slipSalesInput,
                            onValueChange = { viewModel.slipSalesInput = it },
                            label = { Text("Total Realisasi Penjualan Apotek") },
                            placeholder = { Text("Contoh: 15000000") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Dynamic Rate Picker
                        Column {
                            Text(
                                text = "Persentase Insentif Penjualan (%): ${viewModel.slipIncentiveRateInput}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(1.0, 1.5, 2.0, 2.5, 3.0).forEach { rate ->
                                    val isSelected = viewModel.slipIncentiveRateInput == rate
                                    Button(
                                        onClick = { viewModel.slipIncentiveRateInput = rate },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("${rate}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Catatan Tambahan
                        OutlinedTextField(
                            value = viewModel.slipCatatanInput,
                            onValueChange = { viewModel.slipCatatanInput = it },
                            label = { Text("Keterangan Tambahan / Catatan") },
                            placeholder = { Text("misal: Bonus penjualan tercapai") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // 3. Real-time Calculation Ledger Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RINCIAN PERHITUNGAN (LIVE)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "OTOMATIS",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Ledger breakdown
                        LedgerRow(label = "1. Gaji Pokok", amount = viewModel.liveGajiPokok)
                        
                        // Calculated Tunjangan Jabatan based on Role
                        LedgerRow(
                            label = "2. Tunjangan Jabatan (${viewModel.selectedPegawaiForSlip?.jabatan ?: "-"})",
                            amount = viewModel.liveTunjanganJabatan,
                            isAutomatedSubtext = "Otomatis sistem"
                        )

                        // Calculated Meal allowance from workdays
                        val countDays = viewModel.slipWorkDaysInput.toIntOrNull() ?: 0
                        val rateMeal = viewModel.selectedPegawaiForSlip?.uangMakanHarian ?: 0.0
                        LedgerRow(
                            label = "3. Uang Makan",
                            amount = viewModel.liveUangMakan,
                            isAutomatedSubtext = "$countDays hari x ${PayrollCalculator.formatRupiah(rateMeal)}"
                        )

                        // Calculated Sales incentive
                        val totalSales = viewModel.slipSalesInput.toDoubleOrNull() ?: 0.0
                        LedgerRow(
                            label = "4. Insentif Penjualan",
                            amount = viewModel.liveInsentifPenjualan,
                            isAutomatedSubtext = "${viewModel.slipIncentiveRateInput}% x ${PayrollCalculator.formatRupiah(totalSales)}"
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        // Total grand total highlighted
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL ESTIMASI GAJI",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = PayrollCalculator.formatRupiah(viewModel.liveTotalGaji),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Save trigger button
            item {
                Button(
                    onClick = {
                        viewModel.saveSlipGaji(
                            onSuccess = onSaved,
                            onError = { errMsg ->
                                Toast.makeText(context, "Kesalahan: $errMsg", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_slip_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Done")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan & Rekam Slip Gaji",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LedgerRow(
    label: String,
    amount: Double,
    isAutomatedSubtext: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isAutomatedSubtext != null) {
                Text(
                    text = isAutomatedSubtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = PayrollCalculator.formatRupiah(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ==========================================
// TAB 3: EMPLOYEES ROSTER LIST
// ==========================================
@Composable
fun EmployeesTab(
    viewModel: PayrollViewModel,
    pegawaiList: List<Pegawai>
) {
    var isAddDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daftar Staff Apotek",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kelola ${pegawaiList.size} pegawai aktif Nirmala Farma",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        viewModel.resetEmployeeForm()
                        isAddDialogVisible = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (pegawaiList.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Staff Empty",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Pegawai",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Harap daftarkan rincian nama dan jabatan pegawai Apotek terlebih dahulu.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pegawaiList) { p ->
                        EmployeeItemCard(
                            pegawai = p,
                            onDelete = { viewModel.deleteEmployee(p) }
                        )
                    }
                }
            }
        }
    }

    // Modal dialog to Add new Employee
    if (isAddDialogVisible) {
        Dialog(onDismissRequest = { isAddDialogVisible = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("add_employee_dialog"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Daftarkan Pegawai Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Nama Pegawai
                    OutlinedTextField(
                        value = viewModel.namaInput,
                        onValueChange = { viewModel.namaInput = it },
                        label = { Text("Nama Lengkap Pegawai") },
                        placeholder = { Text("Contoh: Apt. Sarah Safitri, S.Farm.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Jabatan / Position selector
                    Text(
                        text = "Jabatan Apotek (Pengaruh Tunjangan)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Dropdown simulation with nice styled radio / buttons rows
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PayrollCalculator.JABATAN_LIST.forEach { j ->
                            val isSelected = viewModel.jabatanInput == j
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onJabatanChanged(j) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(j, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "Tunjangan Otomatis: ${PayrollCalculator.formatRupiah(PayrollCalculator.getTunjanganJabatan(j))}/bln",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    // Gaji Pokok (initialized based on position)
                    OutlinedTextField(
                        value = viewModel.gajiPokokInput,
                        onValueChange = { viewModel.gajiPokokInput = it },
                        label = { Text("Gaji Pokok Dasar (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Uang Makan Harian (initialized based on position)
                    OutlinedTextField(
                        value = viewModel.uangMakanInput,
                        onValueChange = { viewModel.uangMakanInput = it },
                        label = { Text("Uang Makan Harian (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isAddDialogVisible = false }) {
                            Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.saveEmployee(
                                    onSuccess = {
                                        isAddDialogVisible = false
                                        Toast.makeText(context, "Pegawai berhasil didaftarkan!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { errMsg ->
                                        Toast.makeText(context, "Gagal: $errMsg", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeItemCard(
    pegawai: Pegawai,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_card_${pegawai.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant styled initials circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(getJabatanColor(pegawai.jabatan).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pegawai.nama.take(2).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = getJabatanColor(pegawai.jabatan),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pegawai.nama,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Jabatan badge
                    Surface(
                        color = getJabatanColor(pegawai.jabatan).copy(alpha = 0.12f),
                        contentColor = getJabatanColor(pegawai.jabatan),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = pegawai.jabatan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Tunjangan sub indicator
                    Text(
                        text = "Tunj: ${PayrollCalculator.formatRupiah(PayrollCalculator.getTunjanganJabatan(pegawai.jabatan))}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Gaji Pokok: ${PayrollCalculator.formatRupiah(pegawai.gajiPokok)} • Uang Makan: ${PayrollCalculator.formatRupiah(pegawai.uangMakanHarian)}/hari",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus pegawai",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// Helper to color-code position badges beautifully
fun getJabatanColor(jabatan: String): Color {
    return when (jabatan) {
        "Manager" -> Color(0xFF1B4D3E)          // Deep Emerald Sage
        "Apoteker" -> Color(0xFF2E7D32)        // Botanical/Success Green
        "Asisten Apoteker" -> Color(0xFF4A6759) // Primary Sage Green
        "Kasir / Administrasi" -> Color(0xFF8C7355) // Warm Khaki
        "Kurir / Tenaga Umum" -> Color(0xFF64748B) // Slate Grey
        else -> Color(0xFF2C3E36) // Deep Forest Sage
    }
}

// ==========================================
// MODAL: SLIP GAJI INVOICE DIALOG
// ==========================================
@Composable
fun PaySlipDetailDialog(
    slipWithPegawai: SlipGajiDenganPegawai,
    onDismiss: () -> Unit
) {
    val pegawai = slipWithPegawai.pegawai
    val slip = slipWithPegawai.slipGaji
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("payslip_detail_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Slip Header mimicking physical doc
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "APOTEK NIRMALA FARMA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Jl. Nirmala Raya No. 45, Denpasar Barat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SLIP GAJI PEGAWAI",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Periode: ${PayrollCalculator.getIndonesianMonth(slip.bulan)} ${slip.tahun}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Divider(thickness = 1.5.dp, color = MaterialTheme.colorScheme.outline)

                // Employee credentials
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Nama Staff  : ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.width(90.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pegawai?.nama ?: "Pegawai Terhapus",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Jabatan     : ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.width(90.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pegawai?.jabatan ?: "Umum",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Detail components ledger
                Text(
                    text = "RINCIAN PENERIMAAN GAJI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InvoiceLedgerLine(label = "A. Gaji Pokok Dasar", amount = slip.gajiPokok)
                    
                    // Auto jabatan allowance
                    InvoiceLedgerLine(
                        label = "B. Tunjangan Jabatan (Otomatis)",
                        amount = slip.tunjanganJabatan,
                        subText = "Ditentukan oleh jabatan: ${pegawai?.jabatan}"
                    )

                    // Meal calculations
                    InvoiceLedgerLine(
                        label = "C. Uang Makan Bulanan",
                        amount = slip.uangMakan,
                        subText = "${slip.hariKerja} hari kerja x ${PayrollCalculator.formatRupiah(pegawai?.uangMakanHarian ?: 0.0)}"
                    )

                    // Sales incentive calculation
                    InvoiceLedgerLine(
                        label = "D. Insentif Penjualan Toko",
                        amount = slip.insentifPenjualan,
                        subText = "${slip.persentaseInsentif}% dari volume penjualan Rp %,d".format(slip.totalPenjualan.toLong()).replace(',', '.')
                    )
                }

                Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

                // Total payout net block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL DITERIMA (NETTO)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = PayrollCalculator.formatRupiah(slip.totalGaji),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (slip.catatan.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Catatan: ${slip.catatan}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "*Dibuat & disahkan secara elektronik oleh Sistem Keuangan Apotek Nirmala Farma",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Slip Gaji disiapkan untuk dibagikan...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceLedgerLine(
    label: String,
    amount: Double,
    subText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subText != null) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
        Text(
            text = PayrollCalculator.formatRupiah(amount),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.7f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ExpenseItemRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }
        Text(
            text = PayrollCalculator.formatRupiah(amount),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Slate900
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageExpensesDialog(
    viewModel: PayrollViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kelola Pengeluaran Operasional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Apotek Nirmala Farma Period: ${PayrollCalculator.getIndonesianMonth(viewModel.selectedMonth)} ${viewModel.selectedYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                
                Divider()

                OutlinedTextField(
                    value = viewModel.inputListrik,
                    onValueChange = { viewModel.inputListrik = it.filter { char -> char.isDigit() } },
                    label = { Text("Biaya Listrik") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.inputInternet,
                    onValueChange = { viewModel.inputInternet = it.filter { char -> char.isDigit() } },
                    label = { Text("Biaya Internet") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.inputPdam,
                    onValueChange = { viewModel.inputPdam = it.filter { char -> char.isDigit() } },
                    label = { Text("Biaya PDAM") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.inputAtk,
                    onValueChange = { viewModel.inputAtk = it.filter { char -> char.isDigit() } },
                    label = { Text("ATK (Alat Tulis Kantor)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.inputPlastik,
                    onValueChange = { viewModel.inputPlastik = it.filter { char -> char.isDigit() } },
                    label = { Text("Plastik Pembungkus") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.inputAirMinum,
                    onValueChange = { viewModel.inputAirMinum = it.filter { char -> char.isDigit() } },
                    label = { Text("Air Minum Pegawai & Toko") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.inputLainLain,
                    onValueChange = { viewModel.inputLainLain = it.filter { char -> char.isDigit() } },
                    label = { Text("Lain-Lain") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            viewModel.saveOperationalExpenses(
                                onSuccess = {
                                    Toast.makeText(context, "Operasional diperbarui!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
