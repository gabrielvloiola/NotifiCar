package com.example.notificar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notificar.databinding.ActivityCriarSolicitacaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CriarSolicitacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarSolicitacaoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // NOTA: Removemos o PontosManager. As Cloud Functions farão isso automaticamente.

    private var motivoSelecionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarSolicitacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        motivoSelecionado = intent.getStringExtra("MOTIVO_SELECIONADO") ?: "Personalizada"
        binding.tvMotivoSelecionado.text = motivoSelecionado

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

        // Buscar dono da placa
        db.collection("placas").document(placaDestinatario)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val ownerUserId = documentSnapshot.getString("ownerUserId")

                    if (ownerUserId == remetenteUserId) {
                        Toast.makeText(this, "Não pode notificar a si mesmo.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    if (ownerUserId != null) {
                        criarDocumentoSolicitacao(
                            remetenteUserId = remetenteUserId,
                            ownerUserId = ownerUserId,
                            placaDestinatario = placaDestinatario,
                            observacao = observacao
                        )
                    }
                } else {
                    Toast.makeText(this, "Placa não encontrada na base de dados.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar placa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun criarDocumentoSolicitacao(
        remetenteUserId: String,
        ownerUserId: String, // Destinatário (Infrator)
        placaDestinatario: String,
        observacao: String
    ) {
        val solicitacao = hashMapOf(
            "motivo" to motivoSelecionado,
            "observacao" to observacao,
            "placa" to placaDestinatario,
            "destinatarioUserId" to ownerUserId,
            "remetenteUserId" to remetenteUserId,
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "recebida"
        )

        // Ao adicionar aqui, a Cloud Function "onCreate" dispara automaticamente
        db.collection("solicitacoes")
            .add(solicitacao)
            .addOnSuccessListener {
                Toast.makeText(this, "Notificação enviada! Pontos processados.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao enviar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}