package com.example.notificar

data class Depoimento(
    val docId: String,  // Necessário para saber qual documento excluir
    val userId: String, // Necessário para saber quem postou
    val nome: String,
    val tempo: String,
    val texto: String
)