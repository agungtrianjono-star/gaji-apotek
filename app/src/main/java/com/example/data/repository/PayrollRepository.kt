package com.example.data.repository

import com.example.data.dao.PayrollDao
import com.example.data.entity.Pegawai
import com.example.data.entity.SlipGaji
import com.example.data.entity.PengeluaranLain
import com.example.data.model.SlipGajiDenganPegawai
import kotlinx.coroutines.flow.Flow

class PayrollRepository(private val payrollDao: PayrollDao) {

    // Streams
    val allPegawai: Flow<List<Pegawai>> = payrollDao.getAllPegawai()
    val allSlipGaji: Flow<List<SlipGajiDenganPegawai>> = payrollDao.getAllSlipGaji()

    fun getSlipGajiByBulanTahun(bulan: Int, tahun: Int): Flow<List<SlipGajiDenganPegawai>> =
        payrollDao.getSlipGajiByBulanTahun(bulan, tahun)

    fun getPengeluaranLainByBulanTahun(bulan: Int, tahun: Int): Flow<PengeluaranLain?> =
        payrollDao.getPengeluaranLainByBulanTahun(bulan, tahun)

    // Pegawai CRUD suspended operations
    suspend fun getPegawaiById(id: Int): Pegawai? = payrollDao.getPegawaiById(id)

    suspend fun insertPegawai(pegawai: Pegawai) = payrollDao.insertPegawai(pegawai)

    suspend fun updatePegawai(pegawai: Pegawai) = payrollDao.updatePegawai(pegawai)

    suspend fun deletePegawai(pegawai: Pegawai) = payrollDao.deletePegawai(pegawai)

    // Slip Gaji CRUD suspended operations
    suspend fun insertSlipGaji(slipGaji: SlipGaji) = payrollDao.insertSlipGaji(slipGaji)

    suspend fun deleteSlipGaji(slipGaji: SlipGaji) = payrollDao.deleteSlipGaji(slipGaji)

    // Pengeluaran Lain suspended operations
    suspend fun insertPengeluaranLain(pengeluaranLain: PengeluaranLain) = payrollDao.insertPengeluaranLain(pengeluaranLain)
}
