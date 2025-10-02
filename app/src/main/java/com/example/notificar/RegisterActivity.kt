package com.example.notificar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.notificar.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Crie uma instância do validador aqui
        val validator = CredentialsValidator()

        binding.btnCreate.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            val phone = binding.etPhone.text.toString()
            val plate = binding.etPlaca.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || plate.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                // 2. Use o validador para verificar a senha
            } else if (!validator.isPasswordValid(password)) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val userProfile = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "phone" to phone,
                                    "plate" to plate
                                )
                                db.collection("users").document(userId)
                                    .set(userProfile)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()
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
        binding.btnCreate.setOnClickListener {
            // ... (pegar os valores dos campos) ...
            val email = binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()

            if ( email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else if (!validator.isPasswordValid(password)) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()

                // ADICIONE ESTA VERIFICAÇÃO
            } else if (!validator.isEmailValid(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()

            } else {
                // ... (código do Firebase para criar o utilizador) ...
            }
        }
    }

    // A função isPasswordValid() foi removida daqui. A sua lógica agora está na classe CredentialsValidator.
}