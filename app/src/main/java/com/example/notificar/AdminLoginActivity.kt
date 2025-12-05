package com.example.notificar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        // Vinculando os componentes da tela
        val etUser = findViewById<EditText>(R.id.etAdminUser) // Agora aqui vai o E-MAIL
        val etPass = findViewById<EditText>(R.id.etAdminPass)
        val btnEnter = findViewById<Button>(R.id.btnAdminEnter)
        val ivBack = findViewById<ImageView>(R.id.ivBackAdmin)

        // Botão voltar
        ivBack.setOnClickListener {
            finish()
        }

        // Botão entrar
        btnEnter.setOnClickListener {
            val email = etUser.text.toString().trim()
            val senha = etPass.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                fazerLoginAdmin(email, senha)
            } else {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fazerLoginAdmin(email: String, senha: String) {
        // Conecta no Firebase de verdade
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                // Login deu certo! Agora verificamos se é o chefe.
                if (email == "bibig.barbosa@gmail.com") {
                    Toast.makeText(this, "Bem-vindo, Chefe!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, AdminDashboardActivity::class.java)
                    // Limpa a pilha para não voltar ao login
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Se tentar entrar com outro e-mail nessa tela
                    Toast.makeText(this, "Acesso restrito apenas a administradores.", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut() // Desloga o intruso
                }
            }
            .addOnFailureListener { e ->
                // Aqui aparece o erro de "Credenciais Inválidas" se a senha estiver errada
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}