package com.example.notificar

import android.content.Intent // Necessário para a navegação (Intent)
import android.os.Bundle
import android.view.View // Necessário para o findViewById<View>
import androidx.appcompat.app.AppCompatActivity

// Certifique-se de que a TelaPrincipal está declarada no AndroidManifest.xml
// Exemplo: import com.example.notificar.TelaPrincipal

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usa o layout que acabamos de corrigir (activity_home.xml)
        setContentView(R.layout.activity_home)

        // 1. Encontrar o layout raiz (o elemento que cobre toda a tela)
        // ID: home_root_layout (definido no seu XML)
        val rootLayout = findViewById<View>(R.id.home_root_layout)

        // 2. Anexar o listener de clique para a tela inteira
        rootLayout.setOnClickListener {
            // Chama a função de navegação ao detectar qualquer toque
            navigateToTelaPrincipal()
        }
    }

    /**
     * Função responsável por iniciar a TelaPrincipal.
     */
    private fun navigateToTelaPrincipal() {
        // Criamos uma Intent explícita: Vamos desta Activity (this)
        // para a Activity de destino (TelaPrincipal::class.java).
        val intent = Intent(this, TelaPrincipal::class.java)

        // Inicia a nova tela
        startActivity(intent)

        // Finaliza a HomeActivity para que o usuário não retorne a tela de "Login Realizado"
        finish()
    }
}
