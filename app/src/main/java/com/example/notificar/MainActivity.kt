package com.example.notificar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.notificar.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val validator = CredentialsValidator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Botão de login
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Validação e tentativa de login
            validator.performLogin(auth, email, password) { isSuccess ->
                if (isSuccess) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Falha no login. Verifique as suas credenciais.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Link para tela de cadastro
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Link para tela de recuperação de senha
        binding.tvForgotPassword.setOnClickListener {
            // A MainActivity apenas navega para a tela de recuperação.
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
            // A lógica de envio do e-mail está na RecuperarSenhaActivity.kt
        }
    }
}
