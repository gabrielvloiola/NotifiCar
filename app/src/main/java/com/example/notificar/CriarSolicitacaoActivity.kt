package com.example.notificar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.notificar.databinding.ActivityCriarSolicitacaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CriarSolicitacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarSolicitacaoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var motivoSelecionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarSolicitacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Receber o motivo da tela anterior
        motivoSelecionado = intent.getStringExtra("MOTIVO_SELECIONADO")
        if (motivoSelecionado == null) {
            motivoSelecionado = "Personalizada" // Um valor padrão
        }

        // 2. Mostrar o motivo na tela
        binding.tvMotivoSelecionado.text = motivoSelecionado

        // 3. Configurar o botão Enviar
        binding.btnEnviarSolicitacao.setOnClickListener {
            enviarSolicitacao()
        }
    }

    private fun enviarSolicitacao() {
        val placaDestinatario = binding.etPlacaSolicitacao.text.toString().uppercase().trim()
        val observacao = binding.etObservacao.text.toString().trim()
        val remetenteUserId = auth.currentUser?.uid

        if (placaDestinatario.isEmpty()) {
            binding.etPlacaSolicitacao.error = "A placa é obrigatória"
            return
        }

        if (remetenteUserId == null) {
            Toast.makeText(this, "Erro: Remetente não identificado.", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Encontrar o dono da placa no nosso índice "placas"
        db.collection("placas").document(placaDestinatario)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Placa encontrada!
                    val ownerUserId = documentSnapshot.getString("ownerUserId")

                    if (ownerUserId == remetenteUserId) {
                        Toast.makeText(this, "Não pode enviar uma notificação para si mesmo.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // 5. Criar o documento na coleção "solicitacoes"
                    criarDocumentoSolicitacao(
                        remetenteUserId = remetenteUserId,
                        ownerUserId = ownerUserId!!,
                        placaDestinatario = placaDestinatario,
                        observacao = observacao
                    )
                } else {
                    // Placa não encontrada
                    Toast.makeText(this, "Placa não encontrada na nossa base de dados.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao procurar placa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun criarDocumentoSolicitacao(
        remetenteUserId: String,
        ownerUserId: String,
        placaDestinatario: String,
        observacao: String
    ) {
        val solicitacao = hashMapOf(
            "motivo" to motivoSelecionado,
            "observacao" to observacao,
            "placa" to placaDestinatario,
            "destinatarioUserId" to ownerUserId,        // ID do dono do carro (Destinatário)
            "remetenteUserId" to remetenteUserId,  // ID de quem enviou (Remetente)
            "timestamp" to FieldValue.serverTimestamp(), // Data e hora do envio
            "status" to "recebida" // Para controlo futuro (ex: 'lida')
        )

        db.collection("solicitacoes")
            .add(solicitacao) // .add() cria um documento com ID aleatório
            .addOnSuccessListener {
                Toast.makeText(this, "Notificação enviada com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a tela de envio
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}