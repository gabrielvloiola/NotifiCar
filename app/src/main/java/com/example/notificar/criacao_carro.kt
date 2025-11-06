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

    private var isEditMode = false // Para saber se estamos a editar ou a criar
    private var placaOriginal: String? = null // Para saber qual documento atualizar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriacaoCarroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Verifica se a tela foi aberta em "Modo Edição"
        checkEditMode()

        binding.btnSalvarCarro.setOnClickListener {
            salvarCarro()
        }
    }

    // --- NOVA FUNÇÃO PARA VERIFICAR O MODO DE EDIÇÃO ---
    private fun checkEditMode() {
        if (intent.getBooleanExtra("IS_EDIT_MODE", false)) {
            isEditMode = true

            // Pega os dados passados pelo Intent
            placaOriginal = intent.getStringExtra("CARRO_PLACA")
            val modelo = intent.getStringExtra("CARRO_MODELO")
            val cor = intent.getStringExtra("CARRO_COR")
            val marca = intent.getStringExtra("CARRO_MARCA")

            // Atualiza a UI
            binding.etPlacaCarro.setText(placaOriginal)
            binding.etModeloCarro.setText(modelo)
            binding.etCorCarro.setText(cor)
            binding.etMarcaCarro.setText(marca)

            // CRÍTICO: Não deixa o utilizador editar a placa (que é o ID)
            binding.etPlacaCarro.isEnabled = false
            binding.btnSalvarCarro.text = "Atualizar Carro" // Muda o texto do botão
        }
    }

    private fun salvarCarro() {
        // A lógica de obter os dados é a mesma
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

        val carro = hashMapOf(
            "placa" to placa,
            "modelo" to modelo,
            "cor" to cor,
            "marca" to marca
        )

        // Se estamos em modo edição, usamos a placa original.
        // Se estamos em modo de criação, usamos a nova placa.
        // Como a placa está desativada no modo edição, o 'placa' será o mesmo que 'placaOriginal'.
        // O comando .set() irá ATUALIZAR (sobrescrever) o documento se o ID já existir.
        db.collection("users").document(userId)
            .collection("cars").document(placa) // A placa é o ID
            .set(carro)
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