package com.example.notificar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

    private val TAG = "RecuperarSenhaActivity"

    private lateinit var editEmail: EditText
    private lateinit var layoutEnviarLink: LinearLayout
    private lateinit var textCancelar: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha) // layout da tela 1

        auth = FirebaseAuth.getInstance()

        editEmail = findViewById(R.id.edit_email)
        layoutEnviarLink = findViewById(R.id.layout_enviar_link)
        textCancelar = findViewById(R.id.text_cancelar)

        layoutEnviarLink.setOnClickListener {
            val email = editEmail.text.toString().trim()
            Log.d(TAG, "Botão ENVIAR clicado. Email: $email")
            if (email.isEmpty()) {
                editEmail.error = "Digite seu e-mail"
                Toast.makeText(this, "Por favor, digite seu e-mail.", Toast.LENGTH_SHORT).show()
            } else {
                enviarLinkRecuperacao(email)
            }
        }

        textCancelar.setOnClickListener {
            finish()
        }
    }

    private fun enviarLinkRecuperacao(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Link de recuperação enviado para $email. Verifique sua caixa de entrada.",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.d(TAG, "Envio bem-sucedido. Abrindo RecuperarSenha_2")
                    // Navega para a segunda tela
                    val intent = Intent(this, RecuperarSenha_2::class.java)
                    startActivity(intent)
                    // Se quiser permitir voltar à tela anterior, comente a linha abaixo.
                    finish()
                } else {
                    Log.d(TAG, "Falha ao enviar link: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        "Erro: Não foi possível enviar o link. Verifique o e-mail digitado.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}

