package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pengeluaran_lain")
data class PengeluaranLain(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bulan: Int,  // 1-12
    val tahun: Int,  // e.g. 2026
    val biayaListrik: Double = 0.0,
    val biayaInternet: Double = 0.0,
    val biayaPdam: Double = 0.0,
    val biayaAtk: Double = 0.0,
    val biayaPlastik: Double = 0.0,
    val biayaAirMinum: Double = 0.0,
    val biayaLainLain: Double = 0.0
) {
    val totalPengeluaran: Double
        get() = biayaListrik + biayaInternet + biayaPdam + biayaAtk + biayaPlastik + biayaAirMinum + biayaLainLain
}
