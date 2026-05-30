package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.PayrollDao
import com.example.data.entity.Pegawai
import com.example.data.entity.SlipGaji
import com.example.data.entity.PengeluaranLain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Pegawai::class, SlipGaji::class, PengeluaranLain::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun payrollDao(): PayrollDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nirmala_farma_payroll.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database with representative employee roles
                        CoroutineScope(Dispatchers.IO).launch {
                            // Wait for the synchronized assignment block to complete, ensuring INSTANCE is fully initialized
                            var appDb: AppDatabase? = INSTANCE
                            while (appDb == null) {
                                kotlinx.coroutines.delay(50)
                                appDb = INSTANCE
                            }
                            val dao = appDb.payrollDao()
                            // Standard employees
                            dao.insertPegawai(Pegawai(id = 1, nama = "Apt. Sarah Safitri, S.Farm.", jabatan = "Apoteker", gajiPokok = 4500000.0, uangMakanHarian = 30000.0))
                            dao.insertPegawai(Pegawai(id = 2, nama = "Budi Santoso", jabatan = "Asisten Apoteker", gajiPokok = 3200000.0, uangMakanHarian = 30000.0))
                            dao.insertPegawai(Pegawai(id = 3, nama = "Dewi Lestari", jabatan = "Kasir / Administrasi", gajiPokok = 2500000.0, uangMakanHarian = 25000.0))
                            dao.insertPegawai(Pegawai(id = 4, nama = "Roni Wijaya", jabatan = "Kurir / Tenaga Umum", gajiPokok = 2000000.0, uangMakanHarian = 25000.0))
                            dao.insertPegawai(Pegawai(id = 5, nama = "Hendra Setiawan, S.E.", jabatan = "Manager", gajiPokok = 6500000.0, uangMakanHarian = 35000.0))

                            // Seed pre-calculated salary slips for previous months (April and May 2026)
                            // 1. Apt. Sarah (April 2026)
                            dao.insertSlipGaji(SlipGaji(
                                id = 1,
                                pegawaiId = 1,
                                bulan = 4,
                                tahun = 2026,
                                hariKerja = 26,
                                totalPenjualan = 45000000.0,
                                persentaseInsentif = 2.0,
                                gajiPokok = 4500000.0,
                                tunjanganJabatan = 1500000.0,
                                uangMakan = 780000.0,
                                insentifPenjualan = 900000.0,
                                totalGaji = 7680000.0,
                                catatan = "Insentif penjualan April tercapai maksimal"
                            ))

                            // 2. Budi Santoso (April 2026)
                            dao.insertSlipGaji(SlipGaji(
                                id = 2,
                                pegawaiId = 2,
                                bulan = 4,
                                tahun = 2026,
                                hariKerja = 25,
                                totalPenjualan = 30000000.0,
                                persentaseInsentif = 1.5,
                                gajiPokok = 3200000.0,
                                tunjanganJabatan = 800000.0,
                                uangMakan = 750000.0,
                                insentifPenjualan = 450000.0,
                                totalGaji = 5200000.0,
                                catatan = "Pencatatan resep obat kronis lengkap"
                            ))

                            // 3. Dewi Lestari (April 2026)
                            dao.insertSlipGaji(SlipGaji(
                                id = 3,
                                pegawaiId = 3,
                                bulan = 4,
                                tahun = 2026,
                                hariKerja = 24,
                                totalPenjualan = 0.0,
                                persentaseInsentif = 0.0,
                                gajiPokok = 2500000.0,
                                tunjanganJabatan = 500000.0,
                                uangMakan = 600000.0,
                                insentifPenjualan = 0.0,
                                totalGaji = 3600000.0,
                                catatan = "Gaji pokok & uang makan"
                            ))

                            // Seed operational expenses for April 2026 (Month 4)
                            dao.insertPengeluaranLain(PengeluaranLain(
                                id = 1,
                                bulan = 4,
                                tahun = 2026,
                                biayaListrik = 450000.0,
                                biayaInternet = 300000.0,
                                biayaPdam = 150000.0,
                                biayaAtk = 100000.0,
                                biayaPlastik = 85000.0,
                                biayaAirMinum = 50000.0,
                                biayaLainLain = 120000.0
                            ))
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
