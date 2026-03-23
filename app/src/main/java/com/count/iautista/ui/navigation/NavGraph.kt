package com.count.iautista.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.count.iautista.ui.screens.auth.CadastroScreen
import com.count.iautista.ui.screens.auth.ContaScreen
import com.count.iautista.ui.screens.auth.LoginScreen
import com.count.iautista.ui.screens.comunicar.CategoriaScreen
import com.count.iautista.ui.screens.comunicar.ComunicarScreen
import com.count.iautista.ui.screens.historico.HistoricoScreen
import com.count.iautista.ui.screens.inicio.InicioScreen
import com.count.iautista.ui.screens.onboarding.OnboardingScreen
import com.count.iautista.ui.screens.configuracoes.ConfiguracoesScreen
import com.count.iautista.ui.screens.responsavel.GerenciarItensScreen
import com.count.iautista.ui.screens.responsavel.ResponsavelScreen
import com.count.iautista.ui.screens.responsavel.pin.PinSetupScreen
import com.count.iautista.ui.screens.responsavel.pin.PinValidationScreen
import com.count.iautista.ui.screens.rotina.RotinaScreen

@Composable
fun IautistaNavGraph(
    startDestination: String = Screen.Onboarding.route,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                IautistaBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── Onboarding ────────────────────────────────────────────────────
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Auth ──────────────────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToCadastro = { navController.navigate(Screen.Cadastro.route) },
                    onSkip = { navController.popBackStack() },
                )
            }
            composable(Screen.Cadastro.route) {
                CadastroScreen(
                    onSuccess = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Cadastro.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── PIN ───────────────────────────────────────────────────────────
            composable(Screen.PinSetup.route) {
                PinSetupScreen(onComplete = { navController.popBackStack() })
            }
            composable(Screen.PinValidation.route) {
                PinValidationScreen(
                    onSuccess = {
                        navController.navigate(Screen.Responsavel.route) {
                            popUpTo(Screen.PinValidation.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Abas principais ───────────────────────────────────────────────
            composable(Screen.Inicio.route) {
                InicioScreen(navController = navController)
            }
            composable(Screen.Comunicar.route) {
                ComunicarScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(Screen.Categoria.createRoute(categoryId))
                    },
                )
            }
            composable(Screen.Categoria.route) { backStackEntry ->
                val categoryId = backStackEntry.arguments
                    ?.getString("categoryId")?.toLongOrNull() ?: 0L
                CategoriaScreen(
                    categoryId = categoryId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Rotina.route) {
                RotinaScreen()
            }
            composable(Screen.Historico.route) {
                HistoricoScreen()
            }
            composable(Screen.Responsavel.route) {
                ResponsavelScreen(
                    onNavigateToPinSetup        = { navController.navigate(Screen.PinSetup.route) },
                    onNavigateToGerenciarItens  = { navController.navigate(Screen.GerenciarItens.route) },
                    onNavigateToConfiguracoes   = { navController.navigate(Screen.Configuracoes.route) },
                    onNavigateToConta           = { navController.navigate(Screen.Conta.route) },
                    onRequirePin                = { navController.navigate(Screen.PinValidation.route) },
                )
            }

            // ── Sub-telas do Responsável ──────────────────────────────────────
            composable(Screen.Configuracoes.route) {
                ConfiguracoesScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.GerenciarItens.route) {
                GerenciarItensScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Conta.route) {
                ContaScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                )
            }
        }
    }
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

@Composable
private fun IautistaBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
) {
    Column {
        // Separador sutil no topo — substitui sombra de elevação
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,   // sem tonal overlay — surface puro
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hierarchy
                    ?.any { it.route == item.screen.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.screen.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = MaterialTheme.colorScheme.primary,
                        selectedTextColor   = MaterialTheme.colorScheme.primary,
                        indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
