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
 * ## Expansão futura
 * - Adicionar camada 0: frases específicas à atividade da rotina em andamento
 * - Adicionar [SuggestionSource.ROUTINE_ACTIVITY]
 * - Substituir lógica de slot por modelo leve de ML sem alterar a interface
 */
@Singleton
class GetContextualSuggestionsUseCase @Inject constructor() {

    operator fun invoke(snapshot: ContextSnapshot): List<ContextSuggestion> {
        val seen   = mutableSetOf<String>()
        val result = mutableListOf<ContextSuggestion>()

        fun tryAdd(emoji: String, label: String, source: SuggestionSource) {
            if (result.size >= MAX_SUGGESTIONS) return
            if (seen.add(label.lowercase(Locale.getDefault()))) {
                result.add(ContextSuggestion(emoji, label, source))
            }
        }

        // Camada 1: histórico recente (últimos 30 min)
        snapshot.recentLabels.take(MAX_FROM_RECENT).forEach { label ->
            tryAdd(
                emoji  = emojiFor(label, snapshot.mode),
                label  = label,
                source = SuggestionSource.RECENT_HISTORY,
            )
        }

        // Camada 2: mais usadas na faixa horária atual
        snapshot.topLabelsBySlot.take(MAX_FROM_SLOT).forEach { label ->
            tryAdd(
                emoji  = emojiFor(label, snapshot.mode),
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
     * Resolve o emoji para um label buscando em [AppMode.items].
     * Retorna "" se não encontrado — ocorre quando a frase veio do histórico
     * e não tem correspondência no vocabulário do modo atual.
     */
    private fun emojiFor(label: String, mode: AppMode): String =
        mode.items.find { (_, l) -> l.equals(label, ignoreCase = true) }?.first ?: ""

    private companion object {
        const val MAX_SUGGESTIONS = 7
        const val MAX_FROM_RECENT = 2
        const val MAX_FROM_SLOT   = 2
    }
}
