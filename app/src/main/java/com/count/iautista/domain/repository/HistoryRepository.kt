package com.count.iautista.domain.repository

import com.count.iautista.domain.model.PhraseHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    /** Frases mais recentes, em ordem decrescente */
    fun getRecentPhrases(limit: Int): Flow<List<PhraseHistory>>

    /** Frases do dia atual (meia-noite até agora) */
    fun getPhrasesForToday(): Flow<List<PhraseHistory>>

    /** Frases dos últimos 7 dias */
    fun getPhrasesForWeek(): Flow<List<PhraseHistory>>

    suspend fun savePhrase(phrase: PhraseHistory)

    /** Remove entradas antigas conforme política de retenção */
    suspend fun cleanup()
}
