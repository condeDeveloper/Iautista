package com.count.iautista.domain.model

/**
 * Faixa do dia — segmenta sugestões contextuais por horário.
 *
 * Manhã  : 05h–11h
 * Tarde  : 12h–17h
 * Noite  : 18h–04h (inclui madrugada)
 */
enum class TimeSlot(val label: String) {
    MANHA("Manhã"),
    TARDE("Tarde"),
    NOITE("Noite");

    /** Retorna true se [hour] (0–23) pertence a esta faixa. */
    fun contains(hour: Int): Boolean = when (this) {
        MANHA -> hour in 5..11
        TARDE -> hour in 12..17
        NOITE -> hour !in 5..17
    }

    companion object {
        fun fromHour(hour: Int): TimeSlot = when {
            hour in 5..11  -> MANHA
            hour in 12..17 -> TARDE
            else           -> NOITE
        }
    }
}

/**
 * Foto imutável do contexto atual do app.
 *
 * É computado pelo [GetContextSnapshotUseCase] a cada mudança relevante
 * (modo, rotina ou histórico) e servido como entrada para
 * [GetContextualSuggestionsUseCase].
 *
 * @param mode           Modo de ambiente ativo (Casa / Escola / Terapia).
 * @param timeSlot       Faixa do dia no momento da emissão.
 * @param nowActivity    Atividade em andamento na rotina, ou null se não houver.
 * @param recentLabels   Textos das frases faladas nos últimos 30 min, mais frequentes primeiro.
 * @param topLabelsBySlot Textos mais usados na faixa horária atual hoje, mais frequentes primeiro.
 */
data class ContextSnapshot(
    val mode: AppMode,
    val timeSlot: TimeSlot,
    val nowActivity: RoutineItem?,
    val recentLabels: List<String>,
    val topLabelsBySlot: List<String>,
)
