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

class TelaPerfilActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var tvVoltarPrincipal: TextView
    private lateinit var btnVoltar: ImageView
    private lateinit var btnEmail: Button
    private lateinit var btnWhatsapp: Button
    private lateinit var tvNomeUsuario: TextView
    private lateinit var tvEditarPerfil: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null

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
        carregarPerfilUsuario()  // AGORA ATUALIZA EM TEMPO REAL
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
    }

    // -----------------------------------------------------
    //         🔥 AGORA ATUALIZA EM TEMPO REAL 🔥
    // -----------------------------------------------------
    private fun carregarPerfilUsuario() {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        // ESCUTA MUDANÇAS AUTOMATICAMENTE
        db.collection("users").document(user.uid)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    return@addSnapshotListener
                }

                val nome = doc.getString("nome") ?: "Usuário"
                val fotoUrl = doc.getString("fotoPerfil")

                tvNomeUsuario.text = nome

                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.perfil)
                        .into(profileImage)
                }
            }
    }

    // ----------------------------
    //      Funções de Perfil
    // ----------------------------

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        seletorImagem.launch(intent)
    }

    private fun salvarFotoNoFirebase() {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = imageUri ?: return
        val ref = storage.reference.child("perfil/${user.uid}/foto.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    val dados = mapOf("fotoPerfil" to url.toString())

                    db.collection("users")
                        .document(user.uid)
                        .set(dados, com.google.firebase.firestore.SetOptions.merge())

                    Toast.makeText(this, "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao enviar a foto.", Toast.LENGTH_SHORT).show()
            }
    }

    // ----------------------------
    //      Funções de Suporte
    // ----------------------------

    private fun enviarEmailSuporte() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, "Suporte Notificar")
            putExtra(Intent.EXTRA_TEXT, "Olá, preciso de ajuda com o aplicativo Notificar.")
        }
        startActivity(Intent.createChooser(emailIntent, "Escolha um app de e-mail"))
    }

    private fun abrirWhatsAppSuporte() {
        try {
            val numero = "+5561994170310"
            val uri = Uri.parse("https://wa.me/${numero.replace("+", "")}")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível abrir o WhatsApp.", Toast.LENGTH_SHORT).show()
        }
    }
}


