package com.example.data.utils

object PayrollCalculator {
    // List of standard positions at Nirmala Farma pharmacy
    val JABATAN_LIST = listOf(
        "Manager",
        "Apoteker",
        "Asisten Apoteker",
        "Kasir / Administrasi",
        "Kurir / Tenaga Umum"
    )

    // Automatically calculate position allowance ("Tunjangan Jabatan")
    fun getTunjanganJabatan(jabatan: String): Double {
        return when (jabatan) {
            "Manager" -> 2500000.0
            "Apoteker" -> 1500000.0
            "Asisten Apoteker" -> 800000.0
            "Kasir / Administrasi" -> 500000.0
            "Kurir / Tenaga Umum" -> 300000.0
            else -> 0.0
        }
    }

    // Default base salary based on position role
    fun getDefaultGajiPokok(jabatan: String): Double {
        return when (jabatan) {
            "Manager" -> 6500000.0
            "Apoteker" -> 4500000.0
            "Asisten Apoteker" -> 3200000.0
            "Kasir / Administrasi" -> 2500000.0
            "Kurir / Tenaga Umum" -> 2000000.0
            else -> 2000000.0
        }
    }

    // Default daily meal allowance based on position role
    fun getDefaultUangMakanHarian(jabatan: String): Double {
        return when (jabatan) {
            "Manager" -> 35000.0
            "Apoteker", "Asisten Apoteker" -> 30000.0
            else -> 25000.0
        }
    }

    // Format double value to Indonesian Rupiah (standard visual display)
    fun formatRupiah(amount: Double): String {
        return String.format("Rp %,d", amount.toLong()).replace(',', '.')
    }

    // Convert month number to Indonesian month name
    fun getIndonesianMonth(month: Int): String {
        return when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> "-"
        }
    }
}
