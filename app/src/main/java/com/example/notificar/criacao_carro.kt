package com.example.notificar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.notificar.databinding.ActivityCriacaoCarroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdicionarCarroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriacaoCarroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isEditMode = false
    private var placaOriginal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriacaoCarroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        checkEditMode()

        binding.btnSalvarCarro.setOnClickListener {
            salvarCarro()
        }
    }

    private fun checkEditMode() {
        if (intent.getBooleanExtra("IS_EDIT_MODE", false)) {
            isEditMode = true

            placaOriginal = intent.getStringExtra("CARRO_PLACA")
            val modelo = intent.getStringExtra("CARRO_MODELO")
            val cor = intent.getStringExtra("CARRO_COR")
            val marca = intent.getStringExtra("CARRO_MARCA")

            binding.etPlacaCarro.setText(placaOriginal)
            binding.etModeloCarro.setText(modelo)
            binding.etCorCarro.setText(cor)
            binding.etMarcaCarro.setText(marca)

            binding.etPlacaCarro.isEnabled = false // Não deixa editar a placa (ID)
            binding.btnSalvarCarro.text = "Atualizar Carro"
        }
    }

    private fun salvarCarro() {
        val placa = binding.etPlacaCarro.text.toString().uppercase().trim()
        val modelo = binding.etModeloCarro.text.toString().trim()
        val cor = binding.etCorCarro.text.toString().trim()
        val marca = binding.etMarcaCarro.text.toString().trim()

        if (placa.isEmpty() || modelo.isEmpty() || cor.isEmpty() || marca.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Erro: Utilizador não está logado.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- LÓGICA ATUALIZADA ---

        // 1. Criar os dados do carro
        val carro = hashMapOf(
            "placa" to placa,
            "modelo" to modelo,
            "cor" to cor,
            "marca" to marca
        )

        // 2. Criar os dados do índice
        val indicePlaca = hashMapOf(
            "ownerUserId" to userId
        )

        // Usamos um "batch" para garantir que ambas as operações (salvar o carro E salvar o índice)
        // funcionam juntas ou falham juntas.
        val batch = db.batch()

        // Operação 1: Salvar o carro na sub-coleção do utilizador
        val refCarro = db.collection("users").document(userId)
            .collection("cars").document(placa)
        batch.set(refCarro, carro)

        // Operação 2: Salvar a placa no índice público
        val refPlaca = db.collection("placas").document(placa)
        batch.set(refPlaca, indicePlaca)

        // Executa as duas operações
        batch.commit()
            .addOnSuccessListener {
                val successMessage = if (isEditMode) "Carro atualizado!" else "Carro adicionado!"
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                val errorMessage = if (isEditMode) "Erro ao atualizar" else "Erro ao adicionar"
                Toast.makeText(this, "$errorMessage: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}