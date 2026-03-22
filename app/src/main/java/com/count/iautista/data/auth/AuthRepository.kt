package com.count.iautista.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String, val type: AuthErrorType = AuthErrorType.UNKNOWN) : AuthResult()
}

enum class AuthErrorType {
    INVALID_EMAIL,
    WRONG_PASSWORD,
    USER_NOT_FOUND,
    EMAIL_IN_USE,
    WEAK_PASSWORD,
    NETWORK,
    UNKNOWN,
}

@Singleton
class AuthRepository @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { AuthResult.Success(it) }
                ?: AuthResult.Error("Erro ao criar conta")
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Senha fraca. Use ao menos 6 caracteres.", AuthErrorType.WEAK_PASSWORD)
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("Este e-mail já está em uso.", AuthErrorType.EMAIL_IN_USE)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("E-mail inválido.", AuthErrorType.INVALID_EMAIL)
        } catch (e: Exception) {
            AuthResult.Error(friendlyMessage(e), AuthErrorType.UNKNOWN)
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { AuthResult.Success(it) }
                ?: AuthResult.Error("Erro ao entrar")
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("Conta não encontrada.", AuthErrorType.USER_NOT_FOUND)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("E-mail ou senha incorretos.", AuthErrorType.WRONG_PASSWORD)
        } catch (e: Exception) {
            AuthResult.Error(friendlyMessage(e), AuthErrorType.UNKNOWN)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun friendlyMessage(e: Exception): String {
        val msg = e.localizedMessage ?: ""
        return when {
            msg.contains("network", ignoreCase = true) ||
            msg.contains("unable to resolve", ignoreCase = true) ->
                "Sem conexão com a internet."
            else -> "Algo deu errado. Tente novamente."
        }
    }
}
