package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slip_gaji")
data class SlipGaji(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pegawaiId: Int,
    val bulan: Int, // 1 to 12
    val tahun: Int, // e.g., 2026
    val hariKerja: Int,
    val totalPenjualan: Double,
    val persentaseInsentif: Double,
    val gajiPokok: Double,
    val tunjanganJabatan: Double, // dihitung otomatis berdasarkan jabatan pegawai
    val uangMakan: Double, // dihitung otomatis dari hariKerja * uangMakanHarian
    val insentifPenjualan: Double, // dihitung otomatis dari totalPenjualan * persentaseInsentif / 100
    val totalGaji: Double, // dihitung otomatis dari jumlahan seluruh komponen di atas
    val catatan: String = ""
)
