package com.example.notificar

import android.app.ProgressDialog
import android.content.Intent // ALTERAÇÃO AQUI: Import necessário
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etEmail: EditText
    private lateinit var etTelefone: EditText
    private lateinit var etSenha: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluirConta: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val TAG = "EditarPerfilActivity"

    private var emailAtual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNome = findViewById(R.id.et_nome)
        etEmail = findViewById(R.id.et_email)
        etTelefone = findViewById(R.id.et_telefone)
        etSenha = findViewById(R.id.et_senha)
        btnSalvar = findViewById(R.id.btn_salvar)

        // Corrigindo o ID que você usou no XML anterior (btn_excluir_conta)
        // Se no seu XML o ID for "btn_excluir", mantenha R.id.btn_excluir
        btnExcluirConta = findViewById(R.id.btn_excluir)

        carregarDadosSeguro()
        btnSalvar.setOnClickListener { salvarAlteracoesSeguro() }
        btnExcluirConta.setOnClickListener { confirmarExclusao() }
    }

    // ---------------- CARREGAR DADOS ----------------
    // (Esta seção não foi alterada)
    private fun carregarDadosSeguro() {
        val user = auth.currentUser ?: return finish()

        emailAtual = user.email

        val pd = ProgressDialog(this)
        pd.setMessage("Carregando perfil...")
        pd.setCancelable(false)
        pd.show()

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val emailFirestore = doc.getString("email")
                etNome.setText(doc.getString("nome") ?: "")
                etEmail.setText(emailFirestore ?: emailAtual ?: "")
                etTelefone.setText(doc.getString("telefone") ?: "")
                pd.dismiss()
            }
            .addOnFailureListener { e ->
                pd.dismiss()
                Log.e(TAG, "Erro ao carregar perfil", e)
                Toast.makeText(this, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- SALVAR ALTERAÇÕES ----------------
    // (Esta seção não foi alterada)
    private fun salvarAlteracoesSeguro() {
        val user = auth.currentUser ?: return finish()

        val novoNome = etNome.text.toString().trim()
        val novoEmail = etEmail.text.toString().trim()
        val novoTelefone = etTelefone.text.toString().trim()
        val novaSenha = etSenha.text.toString().trim()

        if (novoNome.isEmpty() && novoEmail.isEmpty() &&
            novoTelefone.isEmpty() && novaSenha.isEmpty()) {
            Toast.makeText(this, "Nenhuma alteração detectada.", Toast.LENGTH_SHORT).show()
            return
        }

        val dadosAtualizados = hashMapOf<String, Any>()
        if (novoNome.isNotEmpty()) dadosAtualizados["nome"] = novoNome
        if (novoTelefone.isNotEmpty()) dadosAtualizados["telefone"] = novoTelefone

        if (novoEmail.isNotEmpty() && novoEmail != emailAtual) {
            dadosAtualizados["email"] = novoEmail
        }

        val progress = ProgressDialog(this)
        progress.setMessage("Salvando alterações...")
        progress.setCancelable(false)
        progress.show()

        db.collection("users").document(user.uid)
            .set(dadosAtualizados, SetOptions.merge())
            .addOnSuccessListener {
                handleAuthUpdates(novoEmail, novaSenha, progress)
            }
            .addOnFailureListener { e ->
                progress.dismiss()
                Log.e(TAG, "Erro ao atualizar Firestore", e)
                Toast.makeText(this, "Erro ao salvar no Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ---------------- ATUALIZAR AUTENTICAÇÃO ----------------
    // (Esta seção não foi alterada)
    private fun handleAuthUpdates(
        novoEmail: String,
        novaSenha: String,
        progress: ProgressDialog
    ) {
        val user = auth.currentUser ?: return finish()

        fun finishSuccess() {
            progress.dismiss()
            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }

        val emailMudou = novoEmail.isNotEmpty() && novoEmail != emailAtual

        if (emailMudou) {
            user.updateEmail(novoEmail)
                .addOnSuccessListener {

                    // 🔥 Garantir que atualiza no Firestore também
                    db.collection("users").document(user.uid)
                        .update("email", novoEmail)

                    emailAtual = novoEmail

                    if (novaSenha.isNotEmpty()) {
                        updatePasswordSafe(user, novaSenha, progress, ::finishSuccess)
                    } else finishSuccess()
                }
                .addOnFailureListener { e ->
                    if (e is FirebaseAuthRecentLoginRequiredException) {
                        progress.dismiss()
                        pedirReauthDialog("atualizar o e-mail") { password ->
                            reauthAndRetryEmail(
                                user, novoEmail, password,
                                novaSenha, progress, ::finishSuccess
                            )
                        }
                    } else {
                        progress.dismiss()
                        Toast.makeText(this, "Erro ao atualizar e-mail: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else if (novaSenha.isNotEmpty()) {
            updatePasswordSafe(user, novaSenha, progress, ::finishSuccess)
        } else {
            finishSuccess()
        }
    }

    // ---------------- REAUTH + TENTAR EMAIL NOVAMENTE ----------------
    // (Esta seção não foi alterada)
    private fun reauthAndRetryEmail(
        user: FirebaseUser,
        novoEmail: String,
        password: String,
        novaSenha: String,
        progress: ProgressDialog,
        onSuccessFinish: () -> Unit
    ) {
        val credential = EmailAuthProvider.getCredential(user.email ?: "", password)

        val loading = ProgressDialog(this)
        loading.setMessage("Confirmando segurança...")
        loading.setCancelable(false)
        loading.show()

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updateEmail(novoEmail)
                    .addOnSuccessListener {

                        // 🔥 Atualiza o Firestore também
                        db.collection("users").document(user.uid)
                            .update("email", novoEmail)

                        emailAtual = novoEmail

                        if (novaSenha.isNotEmpty()) {
                            updatePasswordSafe(user, novaSenha, loading, onSuccessFinish)
                        } else {
                            loading.dismiss()
                            onSuccessFinish()
                        }
                    }
                    .addOnFailureListener { e ->
                        loading.dismiss()
                        Toast.makeText(this, "Erro ao atualizar e-mail: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                loading.dismiss()
                Toast.makeText(this, "Reautenticação falhou: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ---------------- PEDIR SENHA ----------------
    // (Esta seção não foi alterada)
    private fun pedirReauthDialog(
        actionDescription: String,
        onPasswordProvided: (String) -> Unit
    ) {
        val input = EditText(this)
        input.hint = "Senha atual"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Reautenticação necessária")
            .setMessage("Para $actionDescription é preciso confirmar sua senha atual.")
            .setView(input)
            .setPositiveButton("Confirmar") { _, _ ->
                val password = input.text.toString()
                if (password.isNotBlank()) {
                    onPasswordProvided(password)
                } else {
                    Toast.makeText(this, "Senha não pode ficar vazia.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ---------------- ATUALIZA SENHA ----------------
    // (Esta seção não foi alterada)
    private fun updatePasswordSafe(
        user: FirebaseUser,
        novaSenha: String,
        progress: ProgressDialog,
        onSuccessFinish: () -> Unit
    ) {
        user.updatePassword(novaSenha)
            .addOnSuccessListener { onSuccessFinish() }
            .addOnFailureListener { e ->
                progress.dismiss()

                if (e is FirebaseAuthRecentLoginRequiredException) {
                    pedirReauthDialog("atualizar a senha") { password ->
                        val credential = EmailAuthProvider.getCredential(user.email ?: "", password)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                user.updatePassword(novaSenha)
                                    .addOnSuccessListener { onSuccessFinish() }
                                    .addOnFailureListener { ex ->
                                        Toast.makeText(this, "Erro ao atualizar senha: ${ex.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { ex ->
                                Toast.makeText(this, "Reautenticação falhou: ${ex.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Erro ao atualizar senha: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ---------------- EXCLUIR CONTA (NOVAS FUNÇÕES) ----------------

    /**
     * Mostra um diálogo de confirmação antes de excluir.
     */
    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Conta")
            .setMessage("Tem certeza que deseja excluir sua conta permanentemente? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                tentarExcluirConta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Tenta excluir os dados do Firestore e depois o usuário do Auth.
     * Lida com a necessidade de reautenticação.
     */
    private fun tentarExcluirConta() {
        val user = auth.currentUser ?: return finish()
        val uid = user.uid

        val progress = ProgressDialog(this)
        progress.setMessage("Excluindo conta...")
        progress.setCancelable(false)
        progress.show()

        // 1. Excluir dados do Firestore primeiro
        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // 2. Se os dados foram excluídos, excluir usuário do Auth
                user.delete()
                    .addOnSuccessListener {
                        progress.dismiss()
                        Toast.makeText(this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show()

                        // ALTERAÇÃO AQUI: Navega para MainActivity e limpa a pilha
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progress.dismiss()
                        if (e is FirebaseAuthRecentLoginRequiredException) {
                            // Precisa de reautenticação para excluir
                            pedirReauthDialog("excluir sua conta") { password ->
                                reauthAndFinalizeDelete(user, password)
                            }
                        } else {
                            Log.e(TAG, "Erro ao excluir usuário do Auth", e)
                            // Nota: Os dados do Firestore foram excluídos, mas o usuário do Auth não.
                            Toast.makeText(this, "Erro ao excluir conta: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                // Falha ao excluir dados do Firestore
                progress.dismiss()
                Log.e(TAG, "Erro ao excluir dados do Firestore", e)
                Toast.makeText(this, "Erro ao excluir dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Chamado após o usuário fornecer a senha para reautenticação
     * antes de finalizar a exclusão da conta.
     */
    private fun reauthAndFinalizeDelete(user: FirebaseUser, password: String) {
        val progress = ProgressDialog(this)
        progress.setMessage("Confirmando segurança...")
        progress.setCancelable(false)
        progress.show()

        // Garante que estamos usando o email atual para a credencial
        val email = user.email
        if (email == null) {
            progress.dismiss()
            Toast.makeText(this, "Não foi possível verificar o e-mail.", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Reautenticação OK, agora excluir o usuário do Auth
                user.delete()
                    .addOnSuccessListener {
                        progress.dismiss()
                        Toast.makeText(this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show()

                        // ALTERAÇÃO AQUI: Navega para MainActivity e limpa a pilha
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progress.dismiss()
                        Log.e(TAG, "Erro final ao excluir usuário do Auth", e)
                        Toast.makeText(this, "Erro final ao excluir conta: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                progress.dismiss()
                Log.e(TAG, "Reautenticação para exclusão falhou", e)
                Toast.makeText(this, "Reautenticação falhou: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}