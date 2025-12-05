package com.example.notificar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AdminNotificationActivity : AppCompatActivity() {

    // Componentes da tela
    private lateinit var etTitulo: EditText
    private lateinit var etMensagem: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnRemover: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnVoltar: ImageView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_notification)

        // Vincular IDs (devem bater com o XML)
        etTitulo = findViewById(R.id.etNotifTitle)
        etMensagem = findViewById(R.id.etNotifMessage)
        btnEnviar = findViewById(R.id.btnSendNotif)
        btnRemover = findViewById(R.id.btnRemoveNotif)
        progressBar = findViewById(R.id.progressBar)
        btnVoltar = findViewById(R.id.ivBack)

        // Configurar Botões
        btnVoltar.setOnClickListener { finish() }

        btnEnviar.setOnClickListener {
            publicarAviso()
        }

        btnRemover.setOnClickListener {
            removerAviso()
        }

        // Carregar mensagem atual para o admin ver
        carregarStatusAtual()
    }

    private fun publicarAviso() {
        val titulo = etTitulo.text.toString().trim()
        val mensagem = etMensagem.text.toString().trim()

        if (mensagem.isEmpty()) {
            etMensagem.error = "A mensagem é obrigatória"
            return
        }

        setLoading(true)

        // Salva no caminho que a TelaPrincipal está escutando
        val dados = hashMapOf(
            "titulo" to titulo, // Opcional, mas salvamos
            "mensagem" to mensagem,
            "ativo" to true,
            "data_atualizacao" to FieldValue.serverTimestamp()
        )

        db.collection("configuracoes").document("aviso_global")
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "Aviso fixado para todos os usuários!", Toast.LENGTH_LONG).show()
                finish() // Volta para o dashboard
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removerAviso() {
        setLoading(true)

        // Define 'ativo' como false para sumir da tela dos usuários
        db.collection("configuracoes").document("aviso_global")
            .update("ativo", false)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "Aviso removido.", Toast.LENGTH_SHORT).show()
                etMensagem.text.clear()
                etTitulo.text.clear()
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(this, "Erro ao remover.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarStatusAtual() {
        db.collection("configuracoes").document("aviso_global").get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getBoolean("ativo") == true) {
                    etTitulo.setText(document.getString("titulo"))
                    etMensagem.setText(document.getString("mensagem"))
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnEnviar.isEnabled = false
            btnRemover.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            btnEnviar.isEnabled = true
            btnRemover.isEnabled = true
        }
    }
}