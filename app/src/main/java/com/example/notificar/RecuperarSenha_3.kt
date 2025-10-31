package com.example.notificar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RecuperarSenha_2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recuperarsenha_3)

        val btnDefinirSenha = findViewById<Button>(R.id.btn_definir_senha)

        // Quando clicar, volta para a tela principal
        btnDefinirSenha.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
