package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pegawai")
data class Pegawai(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val jabatan: String, // Apoteker, Asisten Apoteker, Kasir / Administrasi, Kurir / Tenaga Umum
    val gajiPokok: Double,
    val uangMakanHarian: Double
)
