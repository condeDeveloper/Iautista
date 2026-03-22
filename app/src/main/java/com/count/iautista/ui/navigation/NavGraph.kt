package com.count.iautista.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == item.screen.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
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
