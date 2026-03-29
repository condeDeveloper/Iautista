package com.count.iautista.domain.usecase.context

import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.ContextSnapshot
import com.count.iautista.domain.model.ContextSuggestion
import com.count.iautista.domain.model.SuggestionSource
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Motor de sugestões contextuais — lógica puramente determinística, sem IA.
 *
 * Recebe um [ContextSnapshot] e devolve até [MAX_SUGGESTIONS] sugestões ordenadas
 * por relevância contextual. É uma função pura: sem estado, sem I/O, testável
 * diretamente com qualquer snapshot arbitrário.
 *
 * ## Algoritmo (3 camadas em prioridade decrescente)
 *
 * 1. **Histórico recente** (últimos 30 min) — até [MAX_FROM_RECENT] frases.
 *    A criança provavelmente quer repetir o que disse pouco antes.
 *
 * 2. **Faixa horária** (manhã / tarde / noite de hoje) — até [MAX_FROM_SLOT] frases.
 *    Padrões de uso neste horário revelam necessidades recorrentes no contexto do dia.
 *
 * 3. **Padrão do modo** (Casa / Escola / Terapia) — preenche os slots restantes.
 *    Fallback garantido: mesmo sem histórico, o app sempre tem sugestões relevantes.
 *
 * Todas as camadas passam pela mesma fila de deduplicação (case-insensitive),
 * então uma frase nunca aparece duas vezes, independente de sua origem.
 *
 * @param emojiLookup Mapa label→emoji construído pelo ViewModel a partir dos itens já
 *   carregados do banco. Permite resolver o emoji para itens do histórico que não estão
 *   em [AppMode.items] (ex: "Triste" falado via "Como estou").
 */
@Singleton
class GetContextualSuggestionsUseCase @Inject constructor() {

    operator fun invoke(
        snapshot: ContextSnapshot,
        emojiLookup: Map<String, String> = emptyMap(),
        imageUriLookup: Map<String, String> = emptyMap(),
    ): List<ContextSuggestion> {
        val seen   = mutableSetOf<String>()
        val result = mutableListOf<ContextSuggestion>()

        fun tryAdd(emoji: String, label: String, source: SuggestionSource) {
            if (result.size >= MAX_SUGGESTIONS) return
            if (seen.add(label.lowercase(Locale.ROOT))) {
                result.add(ContextSuggestion(
                    emoji    = emoji,
                    label    = label,
                    source   = source,
                    imageUri = imageUriLookup[label.lowercase(Locale.ROOT)],
                ))
            }
        }

        // Camada 1: histórico recente (últimos 30 min)
        snapshot.recentLabels.take(MAX_FROM_RECENT).forEach { label ->
            tryAdd(
                emoji  = emojiFor(label, snapshot.mode, emojiLookup),
                label  = label,
                source = SuggestionSource.RECENT_HISTORY,
            )
        }

        // Camada 2: mais usadas na faixa horária atual
        snapshot.topLabelsBySlot.take(MAX_FROM_SLOT).forEach { label ->
            tryAdd(
                emoji  = emojiFor(label, snapshot.mode, emojiLookup),
                label  = label,
                source = SuggestionSource.FREQUENT_BY_SLOT,
            )
        }

        // Camada 3: padrão do modo — preenche slots restantes
        snapshot.mode.items.forEach { (emoji, label) ->
            tryAdd(emoji, label, SuggestionSource.MODE_DEFAULT)
        }

        return result
    }

    /**
     * Resolve o emoji para um label com prioridade:
     * 1. [emojiLookup] — itens reais do banco (mais preciso)
     * 2. [AppMode.items] do modo atual
     * 3. Todos os modos — cobre itens de outros modos no histórico
     * 4. "" — caso não encontrado (QuickCard mostra container vazio)
     */
    private fun emojiFor(label: String, mode: AppMode, emojiLookup: Map<String, String>): String {
        val key = label.lowercase(Locale.ROOT)
        emojiLookup[key]?.takeIf { it.isNotBlank() }?.let { return it }
        mode.items.find { (_, l) -> l.equals(label, ignoreCase = true) }?.first
            ?.takeIf { it.isNotBlank() }?.let { return it }
        AppMode.entries.flatMap { it.items }
            .find { (_, l) -> l.equals(label, ignoreCase = true) }?.first
            ?.takeIf { it.isNotBlank() }?.let { return it }
        return ""
    }

    private companion object {
        const val MAX_SUGGESTIONS = 7
        const val MAX_FROM_RECENT = 2
        const val MAX_FROM_SLOT   = 2
    }
}
