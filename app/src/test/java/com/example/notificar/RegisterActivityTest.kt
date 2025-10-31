package com.example.notificar

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class CredentialsValidatorTest {

    private val validator = CredentialsValidator()

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockTask: Task<AuthResult>

    // -------------------------------
    // TESTES DE VALIDAÇÃO DE CAMPOS
    // -------------------------------

    @Test
    fun `isPasswordValid - é válida quando tem 6 ou mais caracteres`() {
        val result = validator.isPasswordValid("123456")
        assertThat(result).isTrue()
    }

    @Test
    fun `isPasswordValid - é inválida quando tem menos de 6 caracteres`() {
        val result = validator.isPasswordValid("12345")
        assertThat(result).isFalse()
    }

    @Test
    fun `isEmailValid - é válido quando tem formato correto`() {
        val result = validator.isEmailValid("nome@exemplo.com")
        assertThat(result).isTrue()
    }

    @Test
    fun `isEmailValid - é inválido quando não contém arroba`() {
        val result = validator.isEmailValid("nomeexemplo.com")
        assertThat(result).isFalse()
    }

    // -------------------------------
    // TESTES DE LOGIN (COM MOCK DO FIREBASE)
    // -------------------------------

    @Test
    fun `performLogin - falha quando o e-mail está vazio`() {
        var resultadoLogin: Boolean? = null
        validator.performLogin(mockAuth, "", "123456") { success ->
            resultadoLogin = success
        }
        assertThat(resultadoLogin).isFalse()
    }

    @Test
    fun `performLogin - falha quando a senha está vazia`() {
        var resultadoLogin: Boolean? = null
        validator.performLogin(mockAuth, "teste@teste.com", "") { success ->
            resultadoLogin = success
        }
        assertThat(resultadoLogin).isFalse()
    }

    @Test
    fun `performLogin - sucesso quando as credenciais do Firebase estão corretas`() {
        // Simula sucesso no login
        whenever(mockTask.isSuccessful).thenReturn(true)
        whenever(mockAuth.signInWithEmailAndPassword(any(), any())).thenReturn(mockTask)

        // Simula callback do Firebase
        doAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockTask)
            null
        }.`when`(mockTask).addOnCompleteListener(any())

        var resultadoLogin: Boolean? = null
        validator.performLogin(mockAuth, "teste@teste.com", "123456") { success ->
            resultadoLogin = success
        }

        assertThat(resultadoLogin).isTrue()
    }

    @Test
    fun `performLogin - falha quando as credenciais do Firebase estão incorretas`() {
        // Simula falha no login
        whenever(mockTask.isSuccessful).thenReturn(false)
        whenever(mockAuth.signInWithEmailAndPassword(any(), any())).thenReturn(mockTask)

        doAnswer { invocation ->
            val listener = invocation.getArgument<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockTask)
            null
        }.`when`(mockTask).addOnCompleteListener(any())

        var resultadoLogin: Boolean? = null
        validator.performLogin(mockAuth, "emailerrado@teste.com", "senhaerrada") { success ->
            resultadoLogin = success
        }

        assertThat(resultadoLogin).isFalse()
    }
}
