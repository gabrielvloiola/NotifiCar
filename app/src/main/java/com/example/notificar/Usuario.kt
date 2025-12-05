package com.example.notificar // <--- Pacote principal

import com.google.firebase.Timestamp

// Classe principal unificada
data class Usuario(
    var id: String = "",
    var nome: String = "",
    var email: String = "",
    var fotoPerfil: String = "",

    // Objeto de gamificação (necessário para o TelaPerfilActivity)
    var gamification: GamificationData = GamificationData()
) {
    // Construtor vazio para o Firebase (Obrigatório)
    constructor() : this("", "", "", "", GamificationData())

    // Helper para facilitar pegar o nível
    fun getNivelAtual(): String {
        return GamificationUtils.calcularNivel(gamification.points)
    }
}

// Sub-classe para organizar os dados de pontos
data class GamificationData(
    var points: Int = 0,
    var level: String = "Iniciante",
    var lastIncidentDate: Timestamp? = null,
    var lastBonusDate: Timestamp? = null
)