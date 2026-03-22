package com.count.iautista.domain.model

/**
 * Rotina do dia agrupada por status.
 * Produzida pelo GetRoutineItemsUseCase — a UI não precisa filtrar listas.
 */
data class RoutineGroup(
    val now: List<RoutineItem> = emptyList(),
    val next: List<RoutineItem> = emptyList(),
    val later: List<RoutineItem> = emptyList(),
    val done: List<RoutineItem> = emptyList(),
) {
    val isEmpty: Boolean get() = now.isEmpty() && next.isEmpty() && later.isEmpty()
    val totalActive: Int get() = now.size + next.size + later.size
}
