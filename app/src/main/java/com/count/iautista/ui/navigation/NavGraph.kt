package com.count.iautista.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.count.iautista.ui.theme.ShapeChip
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.count.iautista.ui.sound.LocalSoundManager
import com.count.iautista.ui.sound.SoundManager
import com.count.iautista.ui.screens.auth.CadastroScreen
import com.count.iautista.ui.screens.auth.ContaScreen
import com.count.iautista.ui.screens.auth.LoginScreen
import com.count.iautista.ui.screens.comunicar.CategoriaScreen
import com.count.iautista.ui.screens.comunicar.ComunicarScreen
import com.count.iautista.ui.screens.historico.HistoricoScreen
import com.count.iautista.ui.screens.inicio.InicioScreen
import com.count.iautista.ui.screens.onboarding.OnboardingScreen
import com.count.iautista.ui.screens.onboarding.OnboardingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.ui.screens.configuracoes.ConfiguracoesScreen
import com.count.iautista.ui.screens.responsavel.AdicionarItemScreen
import com.count.iautista.ui.screens.responsavel.GerenciarItensScreen
import com.count.iautista.ui.screens.responsavel.GerenciarPerfisScreen
import com.count.iautista.ui.screens.responsavel.GerenciarRotinaScreen
import com.count.iautista.ui.screens.responsavel.ResponsavelScreen
import com.count.iautista.ui.screens.responsavel.pin.PinSetupScreen
import com.count.iautista.ui.screens.responsavel.pin.PinValidationScreen
import com.count.iautista.ui.screens.rotina.RotinaScreen

@Composable
fun VozinhaNavGraph(
    startDestination: String = Screen.Onboarding.route,
) {
    val soundManager = remember { SoundManager() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // shouldShowBottomBar muda instantaneamente com o destino.
    // showBottomBar atrasa a exibição para não sobrepor o exit transition da tela anterior.
    val shouldShowBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
    }
    var showBottomBar by remember { mutableStateOf(shouldShowBottomBar) }
    LaunchedEffect(shouldShowBottomBar) {
        if (shouldShowBottomBar) {
            delay(200L) // aguarda exit transition terminar (exitTransition = 180ms)
            showBottomBar = true
        } else {
            showBottomBar = false // some imediatamente ao navegar para sub-tela
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalSoundManager provides soundManager) {
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(180)),
            ) {
                VozinhaBottomBar(
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
            enterTransition = { fadeIn(tween(220)) },
            exitTransition  = { fadeOut(tween(180)) },
        ) {
            // ── Onboarding ────────────────────────────────────────────────────
            composable(Screen.Onboarding.route) {
                val onboardingVm: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    onComplete = {
                        onboardingVm.completeOnboarding()
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
                RotinaScreen(
                    onNavigateToGerenciarRotina = {
                        navController.navigate(Screen.GerenciarRotina.route)
                    },
                )
            }
            composable(Screen.Historico.route) {
                HistoricoScreen()
            }
            composable(Screen.Responsavel.route) {
                ResponsavelScreen(
                    onNavigateToPinSetup        = { navController.navigate(Screen.PinSetup.route) },
                    onNavigateToGerenciarPerfis = { navController.navigate(Screen.GerenciarPerfis.route) },
                    onNavigateToGerenciarItens  = { navController.navigate(Screen.GerenciarItens.route) },
                    onNavigateToGerenciarRotina = { navController.navigate(Screen.GerenciarRotina.route) },
                    onNavigateToConfiguracoes   = { navController.navigate(Screen.Configuracoes.route) },
                    onNavigateToConta           = { navController.navigate(Screen.Conta.route) },
                    onRequirePin                = { navController.navigate(Screen.PinValidation.route) },
                )
            }

            // ── Sub-telas do Responsável ──────────────────────────────────────
            composable(Screen.GerenciarPerfis.route) {
                GerenciarPerfisScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.GerenciarRotina.route) {
                GerenciarRotinaScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Configuracoes.route) {
                ConfiguracoesScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.GerenciarItens.route) {
                GerenciarItensScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToAddItem = { categoryId ->
                        navController.navigate(Screen.AdicionarItem.createRoute(categoryId))
                    },
                )
            }
            composable(Screen.AdicionarItem.route) { backStackEntry ->
                val categoryId = backStackEntry.arguments
                    ?.getString("categoryId")?.toLongOrNull()
                AdicionarItemScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPremium = { navController.navigate(Screen.Conta.route) },
                )
            }
            composable(Screen.Conta.route) {
                ContaScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                )
            }
        }
    }
    } // CompositionLocalProvider
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

@Composable
private fun VozinhaBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
) {
    Column {
        // Separador sutil no topo — sem sombra, sem elevação
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                    .height(68.dp),
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == item.screen.route } == true
                    BottomBarItem(
                        item = item,
                        selected = selected,
                        onClick = { onNavigate(item.screen.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Pill compacto 48×32dp — abraça o ícone, não a área toda
        // Menos genérico que o pill padrão do Material3 (64dp de largura)
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 32.dp)
                .clip(ShapeChip)
                .background(
                    if (selected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
