package com.example.notificar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.notificar.databinding.TelaPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaPrincipal : AppCompatActivity() {

    private lateinit var binding: TelaPrincipalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()
        ouvirNovasSolicitacoes() // Inicia o listener do crachá
    }

    private fun setupListeners() {
        binding.cardNotificar.setOnClickListener {
            startActivity(Intent(this, SelecionarMotivoActivity::class.java))
        }
        binding.cardSolicitacoes.setOnClickListener {
            startActivity(Intent(this, TelaSolicitacoesActivity::class.java))
        }
        binding.cardMeusCarros.setOnClickListener {
            startActivity(Intent(this, TelaMeusCarros::class.java))
        }
        binding.imgPerfil.setOnClickListener {
            startActivity(Intent(this, TelaPerfilActivity::class.java))
        }
        binding.cardDepoimentos.setOnClickListener {
            startActivity(Intent(this, DepoimentosActivity::class.java))
        }
    }

    // --- FUNÇÃO DO CRACHÁ ---
    private fun ouvirNovasSolicitacoes() {
        val userId = auth.currentUser?.uid
        if (userId == null) return // Sai se o utilizador não estiver logado

        db.collection("solicitacoes")
            .whereEqualTo("destinatarioUserId", userId)
            .whereEqualTo("status", "recebida") // Apenas as não lidas
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    binding.tvContadorSolicitacoes.visibility = View.GONE
                    return@addSnapshotListener
                }

                val contagem = snapshot.size() // Pega o número de solicitações

                if (contagem > 0) {
                    binding.tvContadorSolicitacoes.text = contagem.toString()
                    binding.tvContadorSolicitacoes.visibility = View.VISIBLE
                } else {
                    binding.tvContadorSolicitacoes.visibility = View.GONE
                }
            }
    }
}