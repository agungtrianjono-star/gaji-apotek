package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.Pegawai
import com.example.data.entity.SlipGaji
import com.example.data.entity.PengeluaranLain
import com.example.data.model.SlipGajiDenganPegawai
import com.example.data.repository.PayrollRepository
import com.example.data.utils.PayrollCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class PayrollViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PayrollRepository

    // Global Date Selectors
    var selectedMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-12
    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))

    // Streams
    val allPegawai: StateFlow<List<Pegawai>>
    val filteredSlips: StateFlow<List<SlipGajiDenganPegawai>>
    val filteredExpenses: StateFlow<PengeluaranLain?>

    // Combined payroll statistics for selected Month and Year
    val totalPayrollSpent: StateFlow<Double>
    val activeEmployeeCount: StateFlow<Int>
    val averagePayrollPaid: StateFlow<Double>

    // Form Stats for adding new Employee (Pegawai)
    var namaInput by mutableStateOf("")
    var jabatanInput by mutableStateOf("Apoteker")
    var gajiPokokInput by mutableStateOf("")
    var uangMakanInput by mutableStateOf("")

    // Form Stats for calculating monthly salary slip (Input Slip Gaji)
    var selectedPegawaiForSlip by mutableStateOf<Pegawai?>(null)
    var slipWorkDaysInput by mutableStateOf("")
    var slipSalesInput by mutableStateOf("")
    var slipIncentiveRateInput by mutableStateOf(1.5) // standard 1.5%
    var slipCatatanInput by mutableStateOf("")

    // Form inputs for operational expenses (Pengeluaran Lain-lain)
    var inputListrik by mutableStateOf("0")
    var inputInternet by mutableStateOf("0")
    var inputPdam by mutableStateOf("0")
    var inputAtk by mutableStateOf("0")
    var inputPlastik by mutableStateOf("0")
    var inputAirMinum by mutableStateOf("0")
    var inputLainLain by mutableStateOf("0")

    // Live calculation totals (Dynamic Display)
    val liveGajiPokok: Double
        get() = selectedPegawaiForSlip?.gajiPokok ?: 0.0

    val liveTunjanganJabatan: Double
        get() = selectedPegawaiForSlip?.let { PayrollCalculator.getTunjanganJabatan(it.jabatan) } ?: 0.0

    val liveUangMakan: Double
        get() = (slipWorkDaysInput.toIntOrNull() ?: 0) * (selectedPegawaiForSlip?.uangMakanHarian ?: 0.0)

    val liveInsentifPenjualan: Double
        get() = ((slipSalesInput.toDoubleOrNull() ?: 0.0) * slipIncentiveRateInput) / 100.0

    val liveTotalGaji: Double
        get() = liveGajiPokok + liveTunjanganJabatan + liveUangMakan + liveInsentifPenjualan

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PayrollRepository(database.payrollDao())

        // Fetch employee list
        allPegawai = repository.allPegawai
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Dynamic Filtering of salary slips depending on Selected Month and Year
        val filterFlow = snapshotFlow { Pair(selectedMonth, selectedYear) }
        
        @OptIn(ExperimentalCoroutinesApi::class)
        filteredSlips = filterFlow
            .flatMapLatest { pair ->
                repository.getSlipGajiByBulanTahun(pair.first, pair.second)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        @OptIn(ExperimentalCoroutinesApi::class)
        filteredExpenses = filterFlow
            .flatMapLatest { pair ->
                repository.getPengeluaranLainByBulanTahun(pair.first, pair.second)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Map live stats
        totalPayrollSpent = filteredSlips.map { list ->
            list.sumOf { it.slipGaji.totalGaji }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        activeEmployeeCount = filteredSlips.map { list ->
            list.map { it.slipGaji.pegawaiId }.distinct().size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        averagePayrollPaid = filteredSlips.map { list ->
            if (list.isEmpty()) 0.0 else list.sumOf { it.slipGaji.totalGaji } / list.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    // Update form values based on position change when creating employee
    fun onJabatanChanged(newJabatan: String) {
        jabatanInput = newJabatan
        gajiPokokInput = PayrollCalculator.getDefaultGajiPokok(newJabatan).toLong().toString()
        uangMakanInput = PayrollCalculator.getDefaultUangMakanHarian(newJabatan).toLong().toString()
    }

    // Reset Employee form state
    fun resetEmployeeForm() {
        namaInput = ""
        jabatanInput = "Apoteker"
        gajiPokokInput = PayrollCalculator.getDefaultGajiPokok("Apoteker").toLong().toString()
        uangMakanInput = PayrollCalculator.getDefaultUangMakanHarian("Apoteker").toLong().toString()
    }

    // Reset Monthly Salary Calculation Form State
    fun resetSlipForm() {
        selectedPegawaiForSlip = allPegawai.value.firstOrNull()
        slipWorkDaysInput = "25" // standard workdays in pharmacy
        slipSalesInput = "0"
        slipIncentiveRateInput = 1.5
        slipCatatanInput = ""
    }

    // Save customized Pegawai to Room
    fun saveEmployee(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (namaInput.isBlank()) {
            onError("Nama pegawai tidak boleh kosong")
            return
        }
        val baseSalary = gajiPokokInput.toDoubleOrNull() ?: 0.0
        val dailyMeal = uangMakanInput.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            try {
                val newPegawai = Pegawai(
                    nama = namaInput.trim(),
                    jabatan = jabatanInput,
                    gajiPokok = baseSalary,
                    uangMakanHarian = dailyMeal
                )
                repository.insertPegawai(newPegawai)
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal menyimpan pegawai: ${e.localizedMessage}")
            }
        }
    }

    // Edit/Update Pegawai
    fun updateEmployee(pegawai: Pegawai) {
        viewModelScope.launch {
            repository.updatePegawai(pegawai)
        }
    }

    // Delete Pegawai
    fun deleteEmployee(pegawai: Pegawai) {
        viewModelScope.launch {
            repository.deletePegawai(pegawai)
        }
    }

    // Save pre-calculated Slip Gaji to Room
    fun saveSlipGaji(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val pegawai = selectedPegawaiForSlip
        if (pegawai == null) {
            onError("Silakan pilih pegawai terlebih dahulu")
            return
        }

        val workDays = slipWorkDaysInput.toIntOrNull() ?: 0
        if (workDays < 0 || workDays > 31) {
            onError("Jumlah hari kerja tidak valid (0 - 31)")
            return
        }

        val salesVolume = slipSalesInput.toDoubleOrNull() ?: 0.0
        if (salesVolume < 0.0) {
            onError("Total penjualan tidak boleh negatif")
            return
        }

        viewModelScope.launch {
            try {
                val newSlip = SlipGaji(
                    pegawaiId = pegawai.id,
                    bulan = selectedMonth,
                    tahun = selectedYear,
                    hariKerja = workDays,
                    totalPenjualan = salesVolume,
                    persentaseInsentif = slipIncentiveRateInput,
                    gajiPokok = liveGajiPokok,
                    tunjanganJabatan = liveTunjanganJabatan,
                    uangMakan = liveUangMakan,
                    insentifPenjualan = liveInsentifPenjualan,
                    totalGaji = liveTotalGaji,
                    catatan = slipCatatanInput.trim()
                )
                repository.insertSlipGaji(newSlip)
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal menyimpan slip gaji: ${e.localizedMessage}")
            }
        }
    }

    // Delete static database salary slip
    fun deleteSlipGaji(slip: SlipGaji) {
        viewModelScope.launch {
            repository.deleteSlipGaji(slip)
        }
    }

    // Function to load the expenses into inputs
    fun loadExpenses(expenses: PengeluaranLain?) {
        inputListrik = expenses?.biayaListrik?.toLong()?.toString() ?: "0"
        inputInternet = expenses?.biayaInternet?.toLong()?.toString() ?: "0"
        inputPdam = expenses?.biayaPdam?.toLong()?.toString() ?: "0"
        inputAtk = expenses?.biayaAtk?.toLong()?.toString() ?: "0"
        inputPlastik = expenses?.biayaPlastik?.toLong()?.toString() ?: "0"
        inputAirMinum = expenses?.biayaAirMinum?.toLong()?.toString() ?: "0"
        inputLainLain = expenses?.biayaLainLain?.toLong()?.toString() ?: "0"
    }

    // Save operational expenses to database
    fun saveOperationalExpenses(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val listrik = inputListrik.toDoubleOrNull() ?: 0.0
        val internet = inputInternet.toDoubleOrNull() ?: 0.0
        val pdam = inputPdam.toDoubleOrNull() ?: 0.0
        val atk = inputAtk.toDoubleOrNull() ?: 0.0
        val plastik = inputPlastik.toDoubleOrNull() ?: 0.0
        val airMinum = inputAirMinum.toDoubleOrNull() ?: 0.0
        val lainLain = inputLainLain.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            try {
                val existing = filteredExpenses.value
                val newRecord = PengeluaranLain(
                    id = existing?.id ?: 0,
                    bulan = selectedMonth,
                    tahun = selectedYear,
                    biayaListrik = listrik,
                    biayaInternet = internet,
                    biayaPdam = pdam,
                    biayaAtk = atk,
                    biayaPlastik = plastik,
                    biayaAirMinum = airMinum,
                    biayaLainLain = lainLain
                )
                repository.insertPengeluaranLain(newRecord)
                onSuccess()
            } catch (e: Exception) {
                onError("Gagal menyimpan pengeluaran: ${e.localizedMessage}")
            }
        }
    }
}
