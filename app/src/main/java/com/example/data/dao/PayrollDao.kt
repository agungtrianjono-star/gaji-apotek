package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Pegawai
import com.example.data.entity.SlipGaji
import com.example.data.entity.PengeluaranLain
import com.example.data.model.SlipGajiDenganPegawai
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollDao {
    // Pegawai queries
    @Query("SELECT * FROM pegawai ORDER BY nama ASC")
    fun getAllPegawai(): Flow<List<Pegawai>>

    @Query("SELECT * FROM pegawai WHERE id = :id LIMIT 1")
    suspend fun getPegawaiById(id: Int): Pegawai?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPegawai(pegawai: Pegawai)

    @Update
    suspend fun updatePegawai(pegawai: Pegawai)

    @Delete
    suspend fun deletePegawai(pegawai: Pegawai)

    // Slip Gaji queries
    @Transaction
    @Query("SELECT * FROM slip_gaji ORDER BY tahun DESC, bulan DESC, id DESC")
    fun getAllSlipGaji(): Flow<List<SlipGajiDenganPegawai>>

    @Transaction
    @Query("SELECT * FROM slip_gaji WHERE bulan = :bulan AND tahun = :tahun ORDER BY id DESC")
    fun getSlipGajiByBulanTahun(bulan: Int, tahun: Int): Flow<List<SlipGajiDenganPegawai>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlipGaji(slipGaji: SlipGaji)

    @Delete
    suspend fun deleteSlipGaji(slipGaji: SlipGaji)

    // Pengeluaran Lain queries
    @Query("SELECT * FROM pengeluaran_lain WHERE bulan = :bulan AND tahun = :tahun LIMIT 1")
    fun getPengeluaranLainByBulanTahun(bulan: Int, tahun: Int): Flow<PengeluaranLain?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengeluaranLain(pengeluaranLain: PengeluaranLain)
}
