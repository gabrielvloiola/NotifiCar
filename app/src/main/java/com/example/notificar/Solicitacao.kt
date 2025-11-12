package com.example.notificar

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Solicitacao(
    val motivo: String = "",
    val observacao: String = "",
    val placa: String = "",
    val destinatarioUserId: String = "",
    val remetenteUserId: String = "",
    var status: String = "",
    @ServerTimestamp val timestamp: Date? = null,

    // Campos extra que não vêm do Firestore, mas que vamos preencher
    var modeloCarro: String? = null // Para o Requisito 2
)