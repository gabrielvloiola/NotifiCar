package com.example.notificar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificar.databinding.TelaMeusCarrosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class TelaMeusCarros : AppCompatActivity() {

    private lateinit var binding: TelaMeusCarrosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CarroAdapter
    private val listaCarros = mutableListOf<Carro>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaMeusCarrosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        carregarCarrosDoUsuario()

        binding.btnAdicionarCarro.setOnClickListener {
            startActivity(Intent(this, AdicionarCarroActivity::class.java))
        }

        binding.tvTituloMeusCarros.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // ATUALIZAÇÃO AQUI: Passamos a função showOptionsDialog para o adapter
        adapter = CarroAdapter(listaCarros) { carro ->
            showOptionsDialog(carro)
        }
        binding.recyclerViewCarros.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCarros.adapter = adapter
    }

    // (A função carregarCarrosDoUsuario continua igual...)
    private fun carregarCarrosDoUsuario() {
        // ... (o seu código que já funciona para carregar os carros) ...
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Utilizador não logado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).collection("cars")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar carros: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    listaCarros.clear()
                    for (document in snapshot.documents) {
                        val carro = document.toObject(Carro::class.java)
                        if (carro != null) {
                            listaCarros.add(carro)
                        }
                    }
                    adapter.notifyDataSetChanged()

                    if (listaCarros.isEmpty()) {
                        binding.tvListaVazia.visibility = View.VISIBLE
                        binding.recyclerViewCarros.visibility = View.GONE
                    } else {
                        binding.tvListaVazia.visibility = View.GONE
                        binding.recyclerViewCarros.visibility = View.VISIBLE
                    }
                }
            }
    }

    // --- NOVA FUNÇÃO PARA GERIR AS OPÇÕES ---
    private fun showOptionsDialog(carro: Carro) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Utilizador não logado", Toast.LENGTH_SHORT).show()
            return
        }

        val opcoes = arrayOf("Editar Carro", "Remover Carro")

        AlertDialog.Builder(this)
            .setTitle(carro.modelo)
            .setItems(opcoes) { dialog, which ->
                when (which) {
                    0 -> { // --- LÓGICA DE EDITAR ---
                        val intent = Intent(this, AdicionarCarroActivity::class.java)
                        // Passamos os dados do carro para a próxima tela
                        intent.putExtra("IS_EDIT_MODE", true)
                        intent.putExtra("CARRO_PLACA", carro.placa)
                        intent.putExtra("CARRO_MODELO", carro.modelo)
                        intent.putExtra("CARRO_COR", carro.cor)
                        intent.putExtra("CARRO_MARCA", carro.marca)
                        startActivity(intent)
                    }
                    1 -> { // --- LÓGICA DE REMOVER ---
                        removerCarro(userId, carro.placa)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- NOVA FUNÇÃO PARA REMOVER O CARRO ---
    private fun removerCarro(userId: String, placaCarro: String) {
        db.collection("users").document(userId)
            .collection("cars").document(placaCarro)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Carro removido com sucesso!", Toast.LENGTH_SHORT).show()
                // A lista irá atualizar-se sozinha graças ao addSnapshotListener
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao remover carro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}