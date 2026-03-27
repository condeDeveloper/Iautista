package com.count.iautista.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AuthUser(val uid: String, val email: String)

sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
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
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.let { AuthUser(it.uid, it.email.orEmpty()) }

    val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            AuthResult.Success(AuthUser(user.uid, user.email.orEmpty()))
        } catch (e: Exception) {
            e.toAuthError()
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user!!
            AuthResult.Success(AuthUser(user.uid, user.email.orEmpty()))
        } catch (e: Exception) {
            e.toAuthError()
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(AuthUser("", email))
        } catch (e: Exception) {
            e.toAuthError()
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

private fun Exception.toAuthError(): AuthResult.Error = when (this) {
    is FirebaseAuthWeakPasswordException ->
        AuthResult.Error("Senha muito fraca. Use ao menos 6 caracteres.", AuthErrorType.WEAK_PASSWORD)
    is FirebaseAuthInvalidCredentialsException ->
        AuthResult.Error("E-mail ou senha incorretos.", AuthErrorType.WRONG_PASSWORD)
    is FirebaseAuthUserCollisionException ->
        AuthResult.Error("Este e-mail já está em uso.", AuthErrorType.EMAIL_IN_USE)
    is FirebaseAuthInvalidUserException ->
        AuthResult.Error("Conta não encontrada.", AuthErrorType.USER_NOT_FOUND)
    else -> AuthResult.Error(message ?: "Erro desconhecido. Verifique sua conexão.", AuthErrorType.UNKNOWN)
}
