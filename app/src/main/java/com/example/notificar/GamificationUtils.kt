package com.example.notificar // <--- MUDAMOS AQUI (Removemos o .ui)

object GamificationUtils {

    // Definição dos Níveis e Pontos Necessários
    private const val PTS_INICIANTE = 0
    private const val PTS_INTERMEDIARIO = 500
    private const val PTS_AVANCADO = 2000
    private const val PTS_ESPECIALISTA = 5000

    fun calcularNivel(pontos: Int): String {
        return when {
            pontos >= PTS_ESPECIALISTA -> "Especialista"
            pontos >= PTS_AVANCADO -> "Avançado"
            pontos >= PTS_INTERMEDIARIO -> "Intermediário"
            else -> "Iniciante"
        }
    }

    fun pontosParaProximoNivel(pontos: Int): Int {
        return when {
            pontos < PTS_INTERMEDIARIO -> PTS_INTERMEDIARIO - pontos
            pontos < PTS_AVANCADO -> PTS_AVANCADO - pontos
            pontos < PTS_ESPECIALISTA -> PTS_ESPECIALISTA - pontos
            else -> 0
        }
    }
}