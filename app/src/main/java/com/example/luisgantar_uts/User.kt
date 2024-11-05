package com.example.luisgantar_uts

data class User (
    val nama: String = "",
    val nim: String = "",
    val email: String = "",
    val password: String = ""
) {
    constructor() : this("", "", "", "")
}