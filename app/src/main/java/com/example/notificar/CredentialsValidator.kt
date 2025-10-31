package com.example.notificar

import com.google.firebase.auth.FirebaseAuth

class CredentialsValidator {

    /**
     * Verifica se a senha possui pelo menos 6 caracteres.
     */
    fun isPasswordValid(password: String): Boolean {
        return password.trim().length >= 6
    }

    /**
     * Verifica se o e-mail possui formato básico válido.
     */
    fun isEmailValid(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() &&
                trimmed.contains("@") &&
                trimmed.contains(".") &&
                trimmed.indexOf("@") < trimmed.lastIndexOf(".")
    }

    /**
     * Realiza o login no Firebase com callbacks seguros.
     * Retorna `false` se e-mail ou senha estiverem vazios.
     */
    fun performLogin(auth: FirebaseAuth, email: String, pass: String, callback: (Boolean) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        // Valida campos vazios
        if (trimmedEmail.isEmpty() || trimmedPass.isEmpty()) {
            callback(false)
            return
        }

        // Executa autenticação no Firebase
        auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnCompleteListener { task ->
                try {
                    callback(task.isSuccessful)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(false)
                }
            }
    }
}
