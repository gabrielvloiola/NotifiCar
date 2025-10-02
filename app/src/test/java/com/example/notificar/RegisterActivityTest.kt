package com.example.notificar

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CredentialsValidatorTest { // Renomeado para refletir o que estamos a testar

    private val validator = CredentialsValidator()

    @Test
    fun `a palavra-passe é válida quando tem 6 ou mais caracteres`() {
        val result = validator.isPasswordValid("123456")
        assertThat(result).isTrue()
    }

    @Test
    fun `a palavra-passe é inválida quando tem menos de 6 caracteres`() {
        val result = validator.isPasswordValid("12345")
        assertThat(result).isFalse()

        }
    @Test
    fun `o e-mail é válido quando tem formato correto`() {
        val result = validator.isEmailValid("nome@exemplo.com")
        assertThat(result).isTrue()
    }

    @Test
    fun `o e-mail é inválido quando não tem o @`() {
        val result = validator.isEmailValid("nomeexemplo.com")
        assertThat(result).isFalse()
    }

    @Test
    fun `o e-mail é inválido quando está vazio`() {
        val result = validator.isEmailValid("")
        assertThat(result).isFalse()
    }
}