package com.example.notificar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificar.databinding.ActivityDepoimentosBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class DepoimentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepoimentosBinding
    private lateinit var adapter: DepoimentosAdapter
    private val listaDepoimentos = mutableListOf<Depoimento>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepoimentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // Configura a lista
        configurarBotoes()  // Configura os cliques
        carregarDepoimentos() // Busca do Firebase
    }

    private fun configurarBotoes() {
        // -------------------------------------------------
        // 1. BOTÃO VOLTAR (Seta no topo)
        // -------------------------------------------------
        binding.ivBack.setOnClickListener {
            finish() // Fecha a tela e volta para a anterior
        }

        // -------------------------------------------------
        // 2. BOTÃO ENVIAR (Texto "Enviar")
        // -------------------------------------------------
        binding.tvEnviar.setOnClickListener {
            // Pega o texto do campo et_depoimento
            val texto = binding.etDepoimento.text.toString().trim()

            if (texto.isNotEmpty()) {
                // Se tiver texto, envia para o Firebase
                salvarNoFirebase(texto)
            } else {
                // Se estiver vazio, avisa o usuário
                Toast.makeText(this, "Escreva seu depoimento primeiro!", Toast.LENGTH_SHORT).show()
            }
        }

        // -------------------------------------------------
        // 3. BOTÃO ANEXAR (Texto "Anexar foto/vídeo")
        // -------------------------------------------------
        binding.tvAnexar.setOnClickListener {
            // Por enquanto, mostramos apenas uma mensagem visual
            Toast.makeText(this, "Funcionalidade de anexo em breve!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = DepoimentosAdapter(listaDepoimentos)

        // Conecta ao RecyclerView (rvDepoimentos no XML)
        binding.rvDepoimentos.layoutManager = LinearLayoutManager(this)
        binding.rvDepoimentos.adapter = adapter
    }

    private fun carregarDepoimentos() {
        db.collection("depoimentos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listaDepoimentos.clear()
                    for (doc in snapshot.documents) {
                        val depoimento = Depoimento(
                            docId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            nome = doc.getString("nome") ?: "Motorista",
                            tempo = calcularTempo(doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L),
                            texto = doc.getString("texto") ?: ""
                        )
                        listaDepoimentos.add(depoimento)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun salvarNoFirebase(texto: String) {
        val novoDepoimento = hashMapOf(
            "nome" to "Usuário", // Futuramente, isso virá do login
            "texto" to texto,
            "timestamp" to FieldValue.serverTimestamp(),
            "userId" to UUID.randomUUID().toString()
        )

        db.collection("depoimentos")
            .add(novoDepoimento)
            .addOnSuccessListener {
                Toast.makeText(this, "Depoimento enviado!", Toast.LENGTH_SHORT).show()

                // 1. Limpa o campo de texto para não enviar duplicado
                binding.etDepoimento.text.clear()

                // 2. Esconde o teclado para ver a lista melhor
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.etDepoimento.windowToken, 0)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao enviar. Verifique sua conexão.", Toast.LENGTH_SHORT).show()
            }
    }

    // FALTAVA ESTA FUNÇÃO:
    private fun calcularTempo(timestamp: Long): String {
        if (timestamp == 0L) return "agora"
        val diff = System.currentTimeMillis() - timestamp
        val minutos = diff / 60000
        val horas = minutos / 60
        val dias = horas / 24

        return when {
            minutos < 1 -> "agora"
            minutos < 60 -> "${minutos}m"
            horas < 24 -> "${horas}h"
            else -> "${dias}d"
        }
    }
}