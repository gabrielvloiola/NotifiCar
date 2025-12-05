package com.example.notificar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
// Mantemos o binding original do layout de admin
import com.example.notificar.databinding.ActivityAdminDepoimentosBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdministradoresDepoimentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDepoimentosBinding
    private lateinit var adapter: AdminDepoimentosAdapter // Usa o adapter com botão de lixeira
    private val listaDepoimentos = mutableListOf<Depoimento>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDepoimentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // AQUI ESTÁ A CONEXÃO: Lendo da mesma coleção que o usuário escreve
        carregarDepoimentosReais()

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdminDepoimentosAdapter(listaDepoimentos) { depoimentoParaExcluir ->
            confirmarExclusao(depoimentoParaExcluir)
        }

        binding.rvAdminDepoimentos.layoutManager = LinearLayoutManager(this)
        binding.rvAdminDepoimentos.adapter = adapter
    }

    private fun carregarDepoimentosReais() {
        // "depoimentos" é o nome exato da coleção onde o usuário salva
        db.collection("depoimentos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listaDepoimentos.clear()
                    for (doc in snapshot.documents) {
                        val depoimento = Depoimento(
                            docId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            nome = doc.getString("nome") ?: "Anônimo",
                            tempo = calcularTempo(doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L),
                            texto = doc.getString("texto") ?: ""
                        )
                        listaDepoimentos.add(depoimento)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun confirmarExclusao(depoimento: Depoimento) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Depoimento?")
            .setMessage("Deseja apagar o depoimento de ${depoimento.nome}?")
            .setPositiveButton("Apagar") { _, _ ->
                // Ao deletar aqui, some para o usuário também (conexão em tempo real)
                db.collection("depoimentos").document(depoimento.docId).delete()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun calcularTempo(timestamp: Long): String {
        if (timestamp == 0L) return "agora"
        val diff = System.currentTimeMillis() - timestamp
        val minutos = diff / 60000
        if (minutos < 60) return "${minutos}m atrás"
        return "${minutos / 60}h atrás"
    }
}