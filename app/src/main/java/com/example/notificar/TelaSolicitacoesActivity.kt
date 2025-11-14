package com.example.notificar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificar.databinding.TelaSolicitacoesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class TelaSolicitacoesActivity : AppCompatActivity() {

    private lateinit var binding: TelaSolicitacoesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var adapter: SolicitacaoAdapter
    private val listaSolicitacoes = mutableListOf<Solicitacao>()
    private val mapaMeusCarros = mutableMapOf<String, Carro>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaSolicitacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()

        carregarMeusCarros {
            carregarSolicitacoes()
        }

        // --- CÓDIGO ADICIONADO (REQUISITO 2) ---
        // Navega para o fluxo de "Notificar" (Página 12)
        binding.btnCriarNovaSolicitacao.setOnClickListener {
            startActivity(Intent(this, SelecionarMotivoActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = SolicitacaoAdapter(listaSolicitacoes)
        binding.recyclerViewSolicitacoes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSolicitacoes.adapter = adapter
    }

    private fun carregarMeusCarros(onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("cars")
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val carro = document.toObject(Carro::class.java)
                    if (carro != null) {
                        mapaMeusCarros[carro.placa] = carro
                    }
                }
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar carros", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }

    private fun carregarSolicitacoes() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("solicitacoes")
            .whereEqualTo("destinatarioUserId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listaSolicitacoes.clear()
                    for (document in snapshot.documents) {
                        val solicitacao = document.toObject(Solicitacao::class.java)
                        if (solicitacao != null) {
                            val carroCorrespondente = mapaMeusCarros[solicitacao.placa]
                            solicitacao.modeloCarro = carroCorrespondente?.modelo ?: "Placa Desconhecida"
                            listaSolicitacoes.add(solicitacao)
                        }
                    }
                    adapter.notifyDataSetChanged()

                    if (listaSolicitacoes.isEmpty()) {
                        binding.tvListaSolicitacoesVazia.visibility = View.VISIBLE
                        binding.recyclerViewSolicitacoes.visibility = View.GONE
                    } else {
                        binding.tvListaSolicitacoesVazia.visibility = View.GONE
                        binding.recyclerViewSolicitacoes.visibility = View.VISIBLE
                    }
                }
            }
    }
}