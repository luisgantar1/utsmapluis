package com.example.luisgantar_uts

import java.util.Date

data class Absen(
    val type: String,
    val timestamp: Date,
    val photoUri: String
) {
}