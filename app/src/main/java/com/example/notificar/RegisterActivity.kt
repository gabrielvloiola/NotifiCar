package com.example.notificar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.notificar.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // Variável para o validador
    private lateinit var validator: CredentialsValidator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        // Inicializa o validador (assumindo que você tem essa classe)
        validator = CredentialsValidator()

        // Ouve o clique do botão "Criar →"
        binding.btnCreate.setOnClickListener {

            // 1. Coleta os dados dos campos
            val name = binding.etName.text.toString()
            val email = binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            val phone = binding.etPhone.text.toString()
            val plate = binding.etPlaca.text.toString()

            // 2. Validação dos Campos
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || plate.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else if (!validator.isEmailValid(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
            } else if (!validator.isPasswordValid(password)) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
            } else {

                // 3. Tenta criar o usuário no Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // 4. Salva dados adicionais no Firestore
                                val userProfile = hashMapOf(
                                    "nome" to name,    // <-- MUDOU DE "name"
                                    "email" to email,
                                    "telefone" to phone, // <-- MUDOU DE "phone"
                                    "placa" to plate   // <-- MUDOU DE "plate"
                                )
                                db.collection("users").document(userId)
                                    .set(userProfile)
                                    .addOnSuccessListener {

                                        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()

                                        val intent = Intent(this, TelaConfirmacaoActivity::class.java)
                                        startActivity(intent)

                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Erro ao guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            val errorMessage = task.exception?.message
                            Toast.makeText(this, "Falha no cadastro: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}

