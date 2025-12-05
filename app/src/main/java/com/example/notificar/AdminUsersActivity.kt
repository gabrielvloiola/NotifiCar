package com.example.notificar

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificar.databinding.ActivityAdminUsersBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsersBinding
    private lateinit var adapter: AdminUsersAdapter
    private val listaUsuarios = mutableListOf<Usuario>()
    private val db = FirebaseFirestore.getInstance()

    // ⚠️ CONFIRA SE NO SEU BANCO É 'users' OU 'usuarios'
    private val NOME_COLECAO = "users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        carregarUsuariosDoFirebase()

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        // Passamos a função de confirmar exclusão para o Adapter
        adapter = AdminUsersAdapter(listaUsuarios) { usuario ->
            confirmarExclusao(usuario)
        }
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    private fun carregarUsuariosDoFirebase() {
        db.collection(NOME_COLECAO)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nenhum usuário encontrado em '$NOME_COLECAO'.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                listaUsuarios.clear()
                for (document in result) {
                    try {
                        val usuario = document.toObject(Usuario::class.java)
                        usuario.id = document.id
                        if (usuario.nome.isEmpty()) usuario.nome = "Sem Nome"

                        // Opcional: Não mostrar o próprio Admin na lista pra não se apagar sem querer
                        // if (usuario.email != "seuemail@admin.com") {
                        listaUsuarios.add(usuario)
                        // }

                    } catch (e: Exception) {
                        Log.e("AdminDebug", "Erro: ${e.message}")
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro de Permissão: Verifique as Regras do Firebase.", Toast.LENGTH_LONG).show()
                Log.e("AdminError", e.toString())
            }
    }

    private fun confirmarExclusao(usuario: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Conta?")
            .setMessage("ATENÇÃO: Você tem certeza que deseja APAGAR PERMANENTEMENTE o usuário ${usuario.nome}?\nIsso não pode ser desfeito.")
            .setPositiveButton("EXCLUIR") { _, _ ->
                deletarUsuarioDoBanco(usuario)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deletarUsuarioDoBanco(usuario: Usuario) {
        db.collection(NOME_COLECAO).document(usuario.id)
            .delete() // <--- Comando que apaga do banco
            .addOnSuccessListener {
                // Remove da lista visual na hora
                adapter.removerUsuario(usuario)
                Toast.makeText(this, "Usuário apagado com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao apagar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}