package com.example.notificar

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TelaConfirmacaoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Verifique o nome real do seu layout XML
        setContentView(R.layout.activity_tela_confirmacao)

        // TextView clicável "Voltar ao Login →"
        val tvVoltarLogin = findViewById<TextView>(R.id.tvVoltarLogin)

        tvVoltarLogin.setOnClickListener {

            // 1. Crie a Intent para a MainActivity (sua tela de Login)
            val intent = Intent(this, MainActivity::class.java)

            // 2. Use flags para limpar o histórico (back stack).
            // Isso garante que você volte para o Login e não para o Cadastro,
            // e que, ao apertar Voltar, o app feche.
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            // 3. Inicia a tela de Login
            startActivity(intent)

            // 4. Fecha esta tela de confirmação, removendo-a da pilha.
            finish()
        }
    }
}