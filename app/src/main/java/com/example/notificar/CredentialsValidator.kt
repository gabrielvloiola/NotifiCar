package com.example.notificar

class CredentialsValidator {
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
    fun isEmailValid(email: String): Boolean {
        // Verificação simples: não pode estar vazio, tem de ter "@" e "."
        return email.isNotEmpty() && email.contains("@") && email.contains(".")
    }
}