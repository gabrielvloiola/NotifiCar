package com.example.notificar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificar.databinding.ActivityAdminDepoimentosBinding

class AdminDepoimentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDepoimentosBinding
    private lateinit var adapter: AdminDepoimentosAdapter

    // Lista FAKE ajustada para a sua classe Depoimento real
    private val listaFake = mutableListOf(
        Depoimento(
            docId = "doc_001",
            userId = "user_100",
            nome = "João Silva",
            tempo = "20 min atrás",
            texto = "O app é ótimo, mas travou ontem."
        ),
        Depoimento(
            docId = "doc_002",
            userId = "user_101",
            nome = "Maria Souza",
            tempo = "1 hora atrás",
            texto = "Adorei a funcionalidade de notificar!"
        ),
        Depoimento(
            docId = "doc_003",
            userId = "user_102",
            nome = "Pedro H.",
            tempo = "Ontem",
            texto = "Mensagem ofensiva que deve ser apagada..."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDepoimentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdminDepoimentosAdapter(listaFake) { depoimentoParaExcluir ->
            confirmarExclusao(depoimentoParaExcluir)
        }

        binding.rvAdminDepoimentos.layoutManager = LinearLayoutManager(this)
        binding.rvAdminDepoimentos.adapter = adapter
    }

    private fun confirmarExclusao(depoimento: Depoimento) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Depoimento?")
            .setMessage("Tem certeza que deseja apagar o comentário de ${depoimento.nome}?\nEssa ação não pode ser desfeita.")
            .setPositiveButton("Apagar") { _, _ ->
                // Aqui você usará o docId para apagar no Firebase futuramente
                // val idParaApagar = depoimento.docId

                adapter.removerItem(depoimento)
                Toast.makeText(this, "Depoimento removido!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}