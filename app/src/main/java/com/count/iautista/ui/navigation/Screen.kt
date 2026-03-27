package com.count.iautista.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Onboarding
    object Onboarding : Screen("onboarding")

    // Auth
    object Login : Screen("login")
    object Cadastro : Screen("cadastro")

    // PIN
    object PinSetup : Screen("pin_setup")
    object PinValidation : Screen("pin_validation")

    // Main tabs
    object Inicio : Screen("inicio")
    object Comunicar : Screen("comunicar")
    object Rotina : Screen("rotina")
    object Historico : Screen("historico")
    object Responsavel : Screen("responsavel")

    // Comunicar sub-screens
    object Categoria : Screen("categoria/{categoryId}") {
        fun createRoute(categoryId: Long) = "categoria/$categoryId"
    }

    // Responsavel sub-screens
    object GerenciarPerfis : Screen("gerenciar_perfis")
    object GerenciarItens : Screen("gerenciar_itens")
    object GerenciarRotina : Screen("gerenciar_rotina")
    object AdicionarItem : Screen("adicionar_item?categoryId={categoryId}") {
        fun createRoute(categoryId: Long? = null) =
            if (categoryId != null) "adicionar_item?categoryId=$categoryId"
            else "adicionar_item"
    }
    object Configuracoes : Screen("configuracoes")
    object Conta : Screen("conta")
}

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
) {
    object Inicio      : BottomNavItem(Screen.Inicio,      "Início",      Icons.Filled.Home)
    object Comunicar   : BottomNavItem(Screen.Comunicar,   "Comunicar",   Icons.Filled.ChatBubble)
    object Rotina      : BottomNavItem(Screen.Rotina,      "Rotina",      Icons.Filled.CalendarToday)
    object Historico   : BottomNavItem(Screen.Historico,   "Histórico",   Icons.Filled.History)
    object Responsavel : BottomNavItem(Screen.Responsavel, "Responsável", Icons.Filled.ManageAccounts)
}

val bottomNavItems = listOf(
    BottomNavItem.Inicio,
    BottomNavItem.Comunicar,
    BottomNavItem.Rotina,
    BottomNavItem.Historico,
    BottomNavItem.Responsavel,
)
