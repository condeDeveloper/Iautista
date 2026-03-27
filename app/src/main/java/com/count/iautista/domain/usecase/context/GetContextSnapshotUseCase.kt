package com.count.iautista.domain.usecase.context

import com.count.iautista.data.preferences.UserPreferencesDataStore
import com.count.iautista.domain.model.ContextSnapshot
import com.count.iautista.domain.model.TimeSlot
import com.count.iautista.domain.repository.HistoryRepository
import com.count.iautista.domain.usecase.rotina.GetRoutineItemsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produz um [ContextSnapshot] reativo — emite novo valor sempre que o modo,
 * a rotina ou o histórico do dia mudam.
 *
 * Fontes combinadas:
 * - [UserPreferencesDataStore] → modo ativo (Casa / Escola / Terapia)
 * - [GetRoutineItemsUseCase]   → atividade em andamento (status NOW), ou null
 * - [HistoryRepository]        → frases do dia de hoje (uma única query cobre tudo)
 *
 * Os cálculos de "últimos 30 min" e "faixa horária atual" são feitos em memória
 * a cada emissão — sem queries adicionais ao banco.
 *
 * Nenhum módulo Hilt extra é necessário — [@Inject constructor] é suficiente.
 */
@Singleton
class GetContextSnapshotUseCase @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore,
    private val historyRepository: HistoryRepository,
    private val getRoutineItems: GetRoutineItemsUseCase,
) {
    operator fun invoke(): Flow<ContextSnapshot> = combine(
        prefsDataStore.preferences.map { it.appMode },
        getRoutineItems().map { group -> group.now.firstOrNull() },
        historyRepository.getPhrasesForToday(),
    ) { mode, nowActivity, todayPhrases ->

        val currentHour  = LocalTime.now().hour
        val slot         = TimeSlot.fromHour(currentHour)
        val thirtyMinAgo = LocalDateTime.now().minusMinutes(30)

        // Top frases dos últimos 30 min — repetição imediata
        val recentLabels = todayPhrases
            .filter { it.createdAt.isAfter(thirtyMinAgo) }
            .groupBy { it.phraseText }
            .entries
            .sortedByDescending { it.value.size }
            .take(LABEL_LIMIT)
            .map { it.key }

        // Top frases na faixa horária atual (manhã / tarde / noite)
        val topLabelsBySlot = todayPhrases
            .filter { slot.contains(it.hourOfDay) }
            .groupBy { it.phraseText }
            .entries
            .sortedByDescending { it.value.size }
            .take(LABEL_LIMIT)
            .map { it.key }

        ContextSnapshot(
            mode            = mode,
            timeSlot        = slot,
            nowActivity     = nowActivity,
            recentLabels    = recentLabels,
            topLabelsBySlot = topLabelsBySlot,
        )
    }

    private companion object {
        const val LABEL_LIMIT = 5
    }
}
