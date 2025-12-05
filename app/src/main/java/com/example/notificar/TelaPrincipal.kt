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
import com.bumptech.glide.Glide
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

        // --- INICIALIZAÇÃO DE FUNÇÕES ---
        ouvirNovasSolicitacoes()   // Contador vermelho (Badge)
        ouvirDadosUsuario()        // Nome, Foto e Gamificação (Nível/Pontos)
        pedirPermissaoNotificacao() // Android 13+
        salvarTokenFCM()           // Token para Push Notification
        ouvirAvisoDoAdmin()        // <--- NOVO: Escuta mensagens fixadas pelo Admin
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
        binding.cardIncidentes.setOnClickListener {
            startActivity(Intent(this, IncidentesActivity::class.java))
        }
    }

    // --- 1. DADOS DO USUÁRIO E GAMIFICAÇÃO ---
    private fun ouvirDadosUsuario() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) return@addSnapshotListener

                // Atualizar Nome e Foto
                val nome = document.getString("nome") ?: "Usuário"
                val fotoUrl = document.getString("fotoPerfil")

                binding.tvOlaUsuario.text = "Olá, $nome"

                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(fotoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.perfil)
                        .into(binding.imgPerfil)
                }

                // Atualizar Gamificação
                val pontos = document.getLong("gamification.points")?.toInt() ?: 0
                atualizarInterfaceGamificacao(pontos)
            }
    }

    private fun atualizarInterfaceGamificacao(pontos: Int) {
        val nivelNome = GamificationUtils.calcularNivel(pontos)
        val pontosFaltantes = GamificationUtils.pontosParaProximoNivel(pontos)

        binding.tvNivelAtual.text = "Nível: $nivelNome"
        binding.tvPontosCentro.text = pontos.toString()

        val maximoProximoNivel = when (nivelNome) {
            "Iniciante" -> 500
            "Intermediário" -> 2000
            "Avançado" -> 5000
            else -> pontos + 1000
        }

        binding.progressLevel.max = maximoProximoNivel
        binding.progressLevel.progress = pontos

        if (nivelNome == "Especialista") {
            binding.tvProximoNivelInfo.text = "Nível Máximo!"
        } else {
            binding.tvProximoNivelInfo.text = "Faltam $pontosFaltantes para subir"
        }
    }

    // --- 2. CONTADOR DE SOLICITAÇÕES ---
    private fun ouvirNovasSolicitacoes() {
        val userId = auth.currentUser?.uid ?: return

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

    // --- 3. AVISO GLOBAL DO ADMIN (NOVO) ---
    private fun ouvirAvisoDoAdmin() {
        // Escuta a coleção 'configuracoes', documento 'aviso_global'
        db.collection("configuracoes").document("aviso_global")
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) {
                    binding.cardAvisoAdmin.visibility = View.GONE
                    return@addSnapshotListener
                }

                val ativo = document.getBoolean("ativo") ?: false
                val mensagem = document.getString("mensagem")

                // Se estiver ativo e tiver texto, mostra o cartão amarelo
                if (ativo && !mensagem.isNullOrEmpty()) {
                    binding.tvTextoAvisoAdmin.text = mensagem
                    binding.cardAvisoAdmin.visibility = View.VISIBLE
                } else {
                    binding.cardAvisoAdmin.visibility = View.GONE
                }
            }
    }

    // --- 4. PERMISSÕES ANDROID 13+ ---
    private fun pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    // --- 5. TOKEN FCM & INSCRIÇÃO EM TÓPICO ---
    private fun salvarTokenFCM() {
        val userId = auth.currentUser?.uid ?: return

        // Passo A: Inscreve no tópico geral para receber avisos do Admin
        FirebaseMessaging.getInstance().subscribeToTopic("todos_usuarios")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.e("FCM", "Falha ao inscrever no tópico geral")
                }
            }

        // Passo B: Salva o token individual
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val dados = hashMapOf("fcmToken" to token)
            db.collection("users").document(userId)
                .set(dados, SetOptions.merge())
        }
    }
}