package com.example.notificar

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecuperarSenha_3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // IMPORTANTE: usar o layout da TELA 2 (arquivo XML diferente da tela 1)
        setContentView(R.layout.recuperarsenha_2)
    }
}
