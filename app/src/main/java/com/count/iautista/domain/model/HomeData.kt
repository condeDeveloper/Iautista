package com.count.iautista.domain.model

/**
 * Snapshot da tela Início — produzido pelo GetHomeDataUseCase.
 * Agrupa todas as informações necessárias para renderizar a tela de uma vez.
 */
data class HomeData(
    val profile: ChildProfile? = null,
    val greeting: String = "Olá!",
    val recentPhrases: List<PhraseHistory> = emptyList(),
    val mostUsedItems: List<CommunicationItem> = emptyList(),
    val routineNow: List<RoutineItem> = emptyList(),   // cartão "Agora" na home
    val routineNext: List<RoutineItem> = emptyList(),  // cartão "Depois" na home
)
