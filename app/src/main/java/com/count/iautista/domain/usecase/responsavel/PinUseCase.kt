package com.count.iautista.domain.usecase.responsavel

import com.count.iautista.data.preferences.UserPreferencesDataStore
import javax.inject.Inject

/**
 * Agrupa operações de PIN do responsável.
 * O hash SHA-256 é computado dentro do DataStore; este use case trata apenas regras de negócio.
 */
class PinUseCase @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) {
    /**
     * Define o PIN. Rejeita PINs com menos de 4 dígitos.
     * @return true se salvo com sucesso, false se inválido.
     */
    suspend fun setPin(pin: String): Boolean {
        if (pin.length < 4 || !pin.all { it.isDigit() }) return false
        dataStore.setPin(pin)
        return true
    }

    /** Valida o PIN informado contra o hash armazenado. */
    suspend fun validatePin(input: String): Boolean = dataStore.validatePin(input)

    /** Remove o PIN configurado. */
    suspend fun clearPin() = dataStore.clearPin()
}
