package com.example.notificar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.TimeUnit

class TelaPerfilActivity : AppCompatActivity() {

    // Componentes de UI
    private lateinit var profileImage: ImageView
    private lateinit var tvVoltarPrincipal: TextView
    private lateinit var btnVoltar: ImageView
    private lateinit var btnEmail: Button
    private lateinit var btnWhatsapp: Button
    private lateinit var btnSair: Button

    // Textos Dinâmicos
    private lateinit var tvNomeUsuario: TextView
    private lateinit var tvEditarPerfil: TextView
    private lateinit var tvPontuacao: TextView
    private lateinit var tvStatusIncidentes: TextView
    private lateinit var tvTipoConta: TextView
    private lateinit var imgIconeRanking: ImageView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null

    // Seletor de Imagem da Galeria
    private val seletorImagem =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                imageUri?.let {
                    profileImage.setImageURI(it)
                    salvarFotoNoFirebase()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_perfil)

        iniciarFirebase()
        iniciarViews()
        configurarClicks()
        carregarPerfilUsuario() // Listener em tempo real
    }

    private fun iniciarFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    private fun iniciarViews() {
        profileImage = findViewById(R.id.profile_image)
        tvVoltarPrincipal = findViewById(R.id.tvVoltarPrincipal)
        btnVoltar = findViewById(R.id.btnVoltar)
        btnEmail = findViewById(R.id.btnEmail)
        btnWhatsapp = findViewById(R.id.btnWhatsapp)
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario)
        tvEditarPerfil = findViewById(R.id.tvEditarPerfil)
        btnSair = findViewById(R.id.btnSair)

        // IDs da Gamificação
        tvPontuacao = findViewById(R.id.tvPontuacao)
        tvStatusIncidentes = findViewById(R.id.tvStatusIncidentes)
        tvTipoConta = findViewById(R.id.tvTipoConta)
        imgIconeRanking = findViewById(R.id.imgIconeRanking)
    }

    private fun configurarClicks() {
        btnVoltar.setOnClickListener { finish() }
        tvVoltarPrincipal.setOnClickListener { finish() }

        profileImage.setOnClickListener { abrirGaleria() }

        tvEditarPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        btnEmail.setOnClickListener { enviarEmailSuporte() }
        btnWhatsapp.setOnClickListener { abrirWhatsAppSuporte() }

        btnSair.setOnClickListener { realizarLogout() }
    }

    private fun realizarLogout() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // -----------------------------------------------------
    //         🔥 LÓGICA DE DADOS DO USUÁRIO 🔥
    // -----------------------------------------------------
    private fun carregarPerfilUsuario() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener

                // 1. Carregar Dados Pessoais
                val nome = doc.getString("nome") ?: "Usuário"
                val fotoUrl = doc.getString("fotoPerfil")

                tvNomeUsuario.text = nome

                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.perfil) // Certifique-se de ter essa imagem em drawable
                        .into(profileImage)
                }

                // 2. Carregar Pontos (Lendo do objeto aninhado 'gamification')
                // Firestore permite usar "ponto.campo" para acessar mapas aninhados
                val pontos = doc.getLong("gamification.points") ?: 0
                atualizarRanking(pontos)

                // 3. Carregar Data do Último Incidente
                val timestamp = doc.getTimestamp("gamification.lastIncidentDate")
                calcularTempoSemIncidentes(timestamp)
            }
    }

    private fun atualizarRanking(pontos: Long) {
        val pontosInt = pontos.toInt()

        // Exibe os pontos
        tvPontuacao.text = "$pontos pontos"

        // Usa a Utils que criamos para pegar o nome correto do nível
        val nomeNivel = GamificationUtils.calcularNivel(pontosInt)
        tvTipoConta.text = nomeNivel

        // Lógica visual opcional (Troca de ícones/cores conforme o nível)
        when (nomeNivel) {
            "Especialista" -> {
                // imgIconeRanking.setImageResource(R.drawable.ic_diamante)
                // tvTipoConta.setTextColor(getColor(R.color.cor_diamante))
            }
            "Avançado" -> {
                // imgIconeRanking.setImageResource(R.drawable.ic_ouro)
            }
            "Intermediário" -> {
                // imgIconeRanking.setImageResource(R.drawable.ic_prata)
            }
            else -> {
                // imgIconeRanking.setImageResource(R.drawable.ic_bronze)
            }
        }
    }

    private fun calcularTempoSemIncidentes(timestamp: com.google.firebase.Timestamp?) {
        if (timestamp == null) {
            tvStatusIncidentes.text = "Histórico limpo (sem incidentes registrados)."
            return
        }

        val dataUltimo = timestamp.toDate()
        val dataAtual = java.util.Date()

        val diferencaMillis = dataAtual.time - dataUltimo.time
        val dias = TimeUnit.MILLISECONDS.toDays(diferencaMillis)

        val texto = when {
            dias < 1 -> "Incidente reportado hoje."
            dias < 30 -> "$dias dias sem incidentes."
            else -> {
                val meses = dias / 30
                "$meses meses sem incidentes."
            }
        }
        tvStatusIncidentes.text = texto
    }

    // ----------------------------
    //      Funções Auxiliares
    // ----------------------------

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        seletorImagem.launch(intent)
    }

    private fun salvarFotoNoFirebase() {
        val user = auth.currentUser ?: return
        val uri = imageUri ?: return
        val ref = storage.reference.child("perfil/${user.uid}/foto.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    val dados = mapOf("fotoPerfil" to url.toString())
                    db.collection("users").document(user.uid)
                        .set(dados, com.google.firebase.firestore.SetOptions.merge())

                    Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao enviar imagem.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarEmailSuporte() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "Suporte Notificar")
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Email"))
        } catch (e: Exception) {
            Toast.makeText(this, "Nenhum app de email encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirWhatsAppSuporte() {
        try {
            val numero = "5561994170310"
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$numero")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp não instalado.", Toast.LENGTH_SHORT).show()
        }
    }
}