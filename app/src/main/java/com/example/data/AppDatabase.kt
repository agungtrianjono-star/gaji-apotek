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

@Database(entities = [Pegawai::class, SlipGaji::class, PengeluaranLain::class], version = 2, exportSchema = false)
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
                        // Seed database directly via raw SQL execution to prevent any threading deadlocks
                        try {
                            db.execSQL("INSERT OR IGNORE INTO pegawai (id, nama, jabatan, gajiPokok, uangMakanHarian) VALUES (1, 'Apt. Sarah Safitri, S.Farm.', 'Apoteker', 4500000.0, 30000.0)")
                            db.execSQL("INSERT OR IGNORE INTO pegawai (id, nama, jabatan, gajiPokok, uangMakanHarian) VALUES (2, 'Budi Santoso', 'Asisten Apoteker', 3200000.0, 30000.0)")
                            db.execSQL("INSERT OR IGNORE INTO pegawai (id, nama, jabatan, gajiPokok, uangMakanHarian) VALUES (3, 'Dewi Lestari', 'Kasir / Administrasi', 2500000.0, 25000.0)")
                            db.execSQL("INSERT OR IGNORE INTO pegawai (id, nama, jabatan, gajiPokok, uangMakanHarian) VALUES (4, 'Roni Wijaya', 'Kurir / Tenaga Umum', 2000000.0, 25000.0)")
                            db.execSQL("INSERT OR IGNORE INTO pegawai (id, nama, jabatan, gajiPokok, uangMakanHarian) VALUES (5, 'Hendra Setiawan, S.E.', 'Manager', 6500000.0, 35000.0)")

                            db.execSQL("""
                                INSERT OR IGNORE INTO slip_gaji (id, pegawaiId, bulan, tahun, hariKerja, totalPenjualan, persentaseInsentif, gajiPokok, tunjanganJabatan, uangMakan, insentifPenjualan, totalGaji, catatan) 
                                VALUES (1, 1, 4, 2026, 26, 45000000.0, 2.0, 4500000.0, 1500000.0, 780000.0, 900000.0, 7680000.0, 'Insentif penjualan April tercapai maksimal')
                            """.trimIndent())

                            db.execSQL("""
                                INSERT OR IGNORE INTO slip_gaji (id, pegawaiId, bulan, tahun, hariKerja, totalPenjualan, persentaseInsentif, gajiPokok, tunjanganJabatan, uangMakan, insentifPenjualan, totalGaji, catatan) 
                                VALUES (2, 2, 4, 2026, 25, 30000000.0, 1.5, 3200000.0, 800000.0, 750000.0, 450000.0, 5200000.0, 'Pencatatan resep obat kronis lengkap')
                            """.trimIndent())

                            db.execSQL("""
                                INSERT OR IGNORE INTO slip_gaji (id, pegawaiId, bulan, tahun, hariKerja, totalPenjualan, persentaseInsentif, gajiPokok, tunjanganJabatan, uangMakan, insentifPenjualan, totalGaji, catatan) 
                                VALUES (3, 3, 4, 2026, 24, 0.0, 0.0, 2500000.0, 500000.0, 600000.0, 0.0, 3600000.0, 'Gaji pokok & uang makan')
                            """.trimIndent())

                            db.execSQL("""
                                INSERT OR IGNORE INTO pengeluaran_lain (id, bulan, tahun, biayaListrik, biayaInternet, biayaPdam, biayaAtk, biayaPlastik, biayaAirMinum, biayaLainLain) 
                                VALUES (1, 4, 2026, 450000.0, 300000.0, 150000.0, 100000.0, 85000.0, 50000.0, 120000.0)
                            """.trimIndent())
                        } catch (e: Exception) {
                            e.printStackTrace()
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
