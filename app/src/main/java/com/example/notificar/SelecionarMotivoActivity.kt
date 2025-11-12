package com.example.notificar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.notificar.databinding.ActivitySelecionarMotivoBinding

class SelecionarMotivoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelecionarMotivoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelecionarMotivoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Criamos um listener genérico
        val motivoClickListener = View.OnClickListener { view ->
            // Converte a View clicada num TextView para pegar o texto
            val motivo = (view as TextView).text.toString()
            abrirTelaDeEnvio(motivo)
        }

        // Atribuímos o listener a cada TextView
        binding.motivoVidroAberto.setOnClickListener(motivoClickListener)
        binding.motivoPortaAberta.setOnClickListener(motivoClickListener)
        binding.motivoFarolLigado.setOnClickListener(motivoClickListener)
        binding.motivoCarroBloqueando.setOnClickListener(motivoClickListener)
        binding.motivoLocalImproprio.setOnClickListener(motivoClickListener)
        binding.motivoForaDaVaga.setOnClickListener(motivoClickListener)
    }

    private fun abrirTelaDeEnvio(motivo: String) {
        val intent = Intent(this, CriarSolicitacaoActivity::class.java)
        // Passamos o motivo selecionado para a próxima tela
        intent.putExtra("MOTIVO_SELECIONADO", motivo)
        startActivity(intent)
        finish() // Fecha esta tela para que o utilizador não volte para ela
    }
}