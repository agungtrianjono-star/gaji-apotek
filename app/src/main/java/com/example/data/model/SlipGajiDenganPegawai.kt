package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.entity.Pegawai
import com.example.data.entity.SlipGaji

data class SlipGajiDenganPegawai(
    @Embedded val slipGaji: SlipGaji,
    @Relation(
        parentColumn = "pegawaiId",
        entityColumn = "id"
    )
    val pegawai: Pegawai?
)
