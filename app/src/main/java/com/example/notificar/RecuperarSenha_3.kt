package com.example.notificar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenha_3 : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var btnEnviar: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recuperarsenha_3) // usa seu layout

        // Inicializa o Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Conecta os componentes da interface
        etEmail = findViewById(R.id.etEmail) // campo de email
        btnEnviar = findViewById(R.id.btn_definir_senha) // botão de envio

        // Clique do botão para enviar o link de recuperação
        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, digite seu e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Envia o e-mail de redefinição de senha pelo Firebase
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Link de recuperação enviado para $email. Verifique sua caixa de entrada.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish() // Fecha a tela após o envio
                    } else {
                        Toast.makeText(
                            this,
                            "Erro: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}

