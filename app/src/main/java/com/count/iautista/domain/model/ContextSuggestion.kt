package com.count.iautista.domain.model

/**
 * Origem de uma sugestão contextual.
 *
 * Permite à UI diferenciar estilo ou exibir badge informativo.
 * Expansão futura: adicionar ROUTINE_ACTIVITY, AI_INFERRED, etc.
 */
enum class SuggestionSource {
    /** Item padrão do modo ativo (Casa / Escola / Terapia). */
    MODE_DEFAULT,
    /** Frase falada nos últimos 30 minutos — repetição imediata. */
    RECENT_HISTORY,
    /** Frase frequente na faixa horária atual de hoje. */
    FREQUENT_BY_SLOT,
}

/**
 * Uma sugestão de comunicação contextual pronta para exibição.
 *
 * @param emoji    Emoji representativo — fallback quando [imageUri] é nulo.
 * @param label    Texto a ser falado.
 * @param source   Origem da sugestão — para rastreabilidade e estilo visual.
 * @param imageUri URI da imagem ARASAAC (ou foto customizada) do item correspondente no banco.
 *                 Quando presente, a UI deve exibir a imagem em vez do [emoji].
 */
data class ContextSuggestion(
    val emoji: String,
    val label: String,
    val source: SuggestionSource,
    val imageUri: String? = null,
)
