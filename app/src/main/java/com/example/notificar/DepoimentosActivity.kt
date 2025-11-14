package com.example.notificar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View // NOVO: Import necessário
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog // NOVO: Import necessário
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
// NOVO: Import presumido baseado no seu código
import com.example.notificar.TelaPerfilActivity


class DepoimentosActivity : AppCompatActivity() {

    private lateinit var etDepoimento: EditText
    private lateinit var tvAnexar: TextView
    private lateinit var tvEnviar: TextView
    private lateinit var ivBack: ImageView
    private lateinit var ivProfile: ImageView
    private lateinit var containerMotoristas: LinearLayout

    private var arquivoUri: Uri? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Seletor de arquivo
    private val seletorArquivo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                arquivoUri = result.data?.data
                Toast.makeText(this, "Arquivo anexado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_depoimentos)

        // Inicializar Views
        etDepoimento = findViewById(R.id.et_depoimento)
        tvAnexar = findViewById(R.id.tv_anexar)
        tvEnviar = findViewById(R.id.tv_enviar)
        ivBack = findViewById(R.id.iv_back)
        ivProfile = findViewById(R.id.iv_profile)
        containerMotoristas = findViewById(R.id.container_outros_motoristas)

        // Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // 🔥 Carregar depoimentos em tempo real
        carregarDepoimentosRealtime()

        // Eventos
        tvAnexar.setOnClickListener { abrirSeletorDeArquivo() }
        tvEnviar.setOnClickListener { enviarDepoimentoFirestore() }
        ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        ivProfile.setOnClickListener {
            startActivity(Intent(this, TelaPerfilActivity::class.java))
        }
    }

    // -------------------------------------------------------------
    // Abrir seletor de arquivo
    // -------------------------------------------------------------
    private fun abrirSeletorDeArquivo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*", "application/pdf"))
        }
        seletorArquivo.launch(Intent.createChooser(intent, "Selecionar arquivo"))
    }

    // -------------------------------------------------------------
    // ENVIAR DEPOIMENTO PARA FIRESTORE
    // -------------------------------------------------------------
    private fun enviarDepoimentoFirestore() {
        val texto = etDepoimento.text.toString().trim()
        if (texto.isEmpty()) {
            Toast.makeText(this, "Digite seu depoimento antes de enviar.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Você precisa estar logado.", Toast.LENGTH_SHORT).show()
            return
        }

        tvEnviar.isEnabled = false

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val nomeUsuario = doc.getString("nome") ?: "Usuário"

                val depoimento = hashMapOf(
                    "nome" to nomeUsuario,
                    "userId" to user.uid, // Certifique-se de salvar o ID do usuário
                    "texto" to texto,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                db.collection("depoimentos")
                    .add(depoimento)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Depoimento enviado!", Toast.LENGTH_SHORT).show()
                        etDepoimento.text?.clear()
                        arquivoUri = null
                        tvEnviar.isEnabled = true
                    }
                    .addOnFailureListener { ex ->
                        Toast.makeText(this, "Erro ao enviar: ${ex.message}", Toast.LENGTH_LONG).show()
                        tvEnviar.isEnabled = true
                    }
            }
            .addOnFailureListener { ex ->
                Toast.makeText(this, "Erro ao obter nome: ${ex.message}", Toast.LENGTH_LONG).show()
                tvEnviar.isEnabled = true
            }
    }

    // -------------------------------------------------------------
    // 🔥 CARREGAR DEPOIMENTOS EM TEMPO REAL (SnapshotListener)
    // -------------------------------------------------------------
    private fun carregarDepoimentosRealtime() {
        // Obter o ID do usuário logado
        val currentUserId = auth.currentUser?.uid

        db.collection("depoimentos")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, "Erro ao carregar depoimentos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                containerMotoristas.removeAllViews()

                for (doc in snapshot.documents) {
                    val nome = doc.getString("nome") ?: "Usuário"
                    val texto = doc.getString("texto") ?: ""
                    val userId = doc.getString("userId") ?: "" // NOVO: Pegar o ID do autor
                    val docId = doc.id // NOVO: Pegar o ID do documento

                    // timestamp pode ser null na primeira gravação
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    val tempo = calcularTempo(timestamp)

                    // ALTERADO: Usando o data class
                    val dep = Depoimento(docId, userId, nome, tempo, texto)

                    // ALTERADO: Passando o ID do usuário atual
                    val card = criarCardDepoimento(dep, currentUserId)
                    containerMotoristas.addView(card)
                }
            }
    }

    // Criar card visual
    // ALTERADO: Função agora recebe o ID do usuário atual
    private fun criarCardDepoimento(dep: Depoimento, currentUserId: String?): CardView {
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(
            R.layout.item_depoimento_dinamico,
            containerMotoristas,
            false
        ) as CardView

        val tvNome = card.findViewById<TextView>(R.id.tv_nome_motorista)
        val tvTempo = card.findViewById<TextView>(R.id.tv_tempo)
        val tvTexto = card.findViewById<TextView>(R.id.tv_texto_depoimento)

        // NOVO: Encontrar o ícone de exclusão
        // (Assumindo que o ID no XML é 'iv_delete_depoimento')
        val ivDelete = card.findViewById<ImageView>(R.id.iv_delete_depoimento)

        tvNome.text = dep.nome
        tvTempo.text = dep.tempo
        tvTexto.text = dep.texto

        // NOVO: Lógica para mostrar/ocultar o botão de exclusão
        if (currentUserId != null && dep.userId == currentUserId) {
            ivDelete.visibility = View.VISIBLE
            ivDelete.setOnClickListener {
                confirmarExclusao(dep.docId) // Chamar a função de exclusão
            }
        } else {
            ivDelete.visibility = View.GONE
        }

        return card
    }

    // -------------------------------------------------------------
    // NOVO: FUNÇÕES DE EXCLUSÃO
    // -------------------------------------------------------------

    /**
     * Mostra um diálogo de confirmação antes de excluir.
     */
    private fun confirmarExclusao(docId: String) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Depoimento")
            .setMessage("Tem certeza que deseja excluir este depoimento?")
            .setPositiveButton("Excluir") { _, _ ->
                excluirDepoimentoFirestore(docId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Exclui o documento do Firestore.
     * O SnapshotListener atualizará a UI automaticamente.
     */
    private fun excluirDepoimentoFirestore(docId: String) {
        db.collection("depoimentos").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Depoimento excluído.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    // -------------------------------------------------------------
    // ⏱️ CALCULAR TEMPO RELATIVO (5m, 2h, 3d)
    // -------------------------------------------------------------
    private fun calcularTempo(timestamp: Long): String {
        if (timestamp == 0L) return "agora"

        val agora = System.currentTimeMillis()
        val diff = agora - timestamp

        val minutos = diff / 60000
        val horas = minutos / 60
        val dias = horas / 24

        return when {
            minutos < 1 -> "agora"
            minutos < 60 -> "${minutos}m"
            horas < 24 -> "${horas}h"
            else -> "${dias}d"
        }
    }
}


