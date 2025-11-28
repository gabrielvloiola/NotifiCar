package com.example.notificar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.notificar.databinding.TelaPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class TelaPrincipal : AppCompatActivity() {

    private lateinit var binding: TelaPrincipalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupListeners()

        // Estas linhas estavam vermelhas porque as funções abaixo não existiam
        ouvirNovasSolicitacoes()
        pedirPermissaoNotificacao()
        salvarTokenFCM()
    }

    private fun setupListeners() {
        binding.cardNotificar.setOnClickListener {
            startActivity(Intent(this, SelecionarMotivoActivity::class.java))
        }
        binding.cardSolicitacoes.setOnClickListener {
            startActivity(Intent(this, TelaSolicitacoesActivity::class.java))
        }
        binding.cardMeusCarros.setOnClickListener {
            startActivity(Intent(this, TelaMeusCarros::class.java))
        }
        binding.imgPerfil.setOnClickListener {
            startActivity(Intent(this, TelaPerfilActivity::class.java))
        }
        binding.cardDepoimentos.setOnClickListener {
            startActivity(Intent(this, DepoimentosActivity::class.java))
        }
        binding.cardEstacionamento.setOnClickListener {
            startActivity(Intent(this, EstacionamentoActivity::class.java))
        }
        // Adicione o listener para Incidentes se já tiver a Activity criada
        // binding.cardIncidentes.setOnClickListener {
        //     startActivity(Intent(this, IncidentesActivity::class.java))
        // }
    }

    // --- FUNÇÃO 1: OUVIR NOVAS SOLICITAÇÕES (CRACHÁ) ---
    private fun ouvirNovasSolicitacoes() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        db.collection("solicitacoes")
            .whereEqualTo("destinatarioUserId", userId)
            .whereEqualTo("status", "recebida")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    binding.tvContadorSolicitacoes.visibility = View.GONE
                    return@addSnapshotListener
                }

                val contagem = snapshot.size()

                if (contagem > 0) {
                    binding.tvContadorSolicitacoes.text = contagem.toString()
                    binding.tvContadorSolicitacoes.visibility = View.VISIBLE
                } else {
                    binding.tvContadorSolicitacoes.visibility = View.GONE
                }
            }
    }

    // --- FUNÇÃO 2: PEDIR PERMISSÃO DE NOTIFICAÇÃO (ANDROID 13+) ---
    private fun pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    // --- FUNÇÃO 3: SALVAR O TOKEN DO FIREBASE MESSAGING ---
    private fun salvarTokenFCM() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Log para debug (opcional)
            android.util.Log.d("FCM_TOKEN", "TOKEN DE NOTIFICAÇÃO: $token")

            val dados = hashMapOf("fcmToken" to token)

            // Salva no Firestore com merge para não apagar outros dados
            db.collection("users").document(userId)
                .set(dados, SetOptions.merge())
        }
    }
}