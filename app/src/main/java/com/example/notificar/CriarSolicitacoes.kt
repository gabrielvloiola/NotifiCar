package com.example.notificar

import android.os.Bundle
import android.view.View // Import necessário
import android.widget.ImageView // Import necessário
import android.widget.LinearLayout // Import necessário
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CriarSolicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 1. Define o layout da activity
        setContentView(R.layout.activity_criar_solicitacoes)

        // 2. Encontra as Views DEPOIS do setContentView
        //    (Assumindo que seu layout tem os IDs do XML que forneci)
        val headerSugeridas = findViewById<LinearLayout>(R.id.header_sugeridas)
        val listSugeridas = findViewById<LinearLayout>(R.id.list_sugeridas)
        val iconSugeridasSeta = findViewById<ImageView>(R.id.icon_sugeridas_seta)
        val cardPersonalizada = findViewById<LinearLayout>(R.id.card_personalizada)

        // 3. Configura o listener de padding para as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Configura o OnClickListener AQUI, dentro do onCreate (COM A LÓGICA CORRIGIDA)
        headerSugeridas.setOnClickListener {
            // Lógica para expandir ou recolher
            if (listSugeridas.visibility == View.GONE) {
                // Se está ESCONDIDO (Tela 1), EXPANDA (Tela 2)
                listSugeridas.visibility = View.VISIBLE

                // CORREÇÃO APLICADA: Mude para o ícone de seta PARA BAIXO
                // (Certifique-se de que você tem um drawable chamado 'ic_arrow_down' ou use o nome correto)
                iconSugeridasSeta.setImageResource(R.drawable.proximo)

                // Opcional: Deixar o card "Personalizada" com aparência de desabilitado
                cardPersonalizada.alpha = 0.5f
            } else {
                // Se está VISÍVEL (Tela 2), ESCONDA (Tela 1)
                listSugeridas.visibility = View.GONE

                // AQUI ESTÁ CORRETO: Ícone para a DIREITA
                iconSugeridasSeta.setImageResource(R.drawable.proximo)

                // Opcional: Reabilitar a aparência do card "Personalizada"
                cardPersonalizada.alpha = 1.0f
            }
        }
    }
}
// O código duplicado que estava aqui foi removido.