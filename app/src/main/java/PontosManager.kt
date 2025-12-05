package com.example.notificar

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.concurrent.TimeUnit

class PontosManager {

    private val db = FirebaseFirestore.getInstance()

    // Configuração de Pontos
    private val PONTOS_POR_ENVIO = 10
    private val PONTOS_BOM_COMPORTAMENTO = 50
    private val DIAS_PARA_BONUS = 30 // Ganha bônus a cada 30 dias sem incidentes

    // Configuração de Níveis
    private val NIVEL_BRONZE = 100
    private val NIVEL_PRATA = 500
    private val NIVEL_OURO = 1000

    /**
     * Função 1: Adicionar pontos quando o usuário ENVIA uma notificação
     */
    fun adicionarPontosPorEnvio(userId: String) {
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val pontosAtuais = snapshot.getLong("pontos") ?: 0
            val novosPontos = pontosAtuais + PONTOS_POR_ENVIO

            // Calcula o novo nível
            val novoNivel = calcularNivel(novosPontos)

            transaction.update(userRef, "pontos", novosPontos)
            transaction.update(userRef, "nivel", novoNivel)
        }
    }

    /**
     * Função 2: Verificar e dar bônus por BOM COMPORTAMENTO
     * Esta função deve ser chamada quando o usuário abre o app (ex: na TelaPrincipal)
     */
    fun verificarBonusBomComportamento(userId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val ultimaInfracao = document.getTimestamp("ultimaNotificacaoRecebida")?.toDate()
                val ultimoBonus = document.getTimestamp("dataUltimoBonus")?.toDate()

                // Se nunca recebeu notificação, consideramos a data de criação da conta (ou uma data antiga fixa)
                val dataBaseCalculo = ultimaInfracao ?: Date(0)
                val dataReferenciaBonus = ultimoBonus ?: dataBaseCalculo

                val diasSemIncidente = calcularDiferencaDias(dataReferenciaBonus, Date())

                if (diasSemIncidente >= DIAS_PARA_BONUS) {
                    // OBA! Ganhou bônus
                    val pontosAtuais = document.getLong("pontos") ?: 0
                    val novosPontos = pontosAtuais + PONTOS_BOM_COMPORTAMENTO
                    val novoNivel = calcularNivel(novosPontos)

                    val updates = hashMapOf<String, Any>(
                        "pontos" to novosPontos,
                        "nivel" to novoNivel,
                        "dataUltimoBonus" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    userRef.update(updates)
                }
            }
        }
    }

    /**
     * Função Auxiliar: Define o nível com base nos pontos
     */
    private fun calcularNivel(pontos: Long): String {
        return when {
            pontos >= NIVEL_OURO -> "Ouro"
            pontos >= NIVEL_PRATA -> "Prata"
            pontos >= NIVEL_BRONZE -> "Bronze"
            else -> "Novato"
        }
    }

    private fun calcularDiferencaDias(data1: Date, data2: Date): Long {
        val diff = data2.time - data1.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Função 3: Registrar que o usuário RECEBEU uma notificação (zera a contagem de bom comportamento)
     */
    fun registrarInfracaoRecebida(destinatarioId: String) {
        val userRef = db.collection("users").document(destinatarioId)
        userRef.update("ultimaNotificacaoRecebida", com.google.firebase.firestore.FieldValue.serverTimestamp())
    }
}