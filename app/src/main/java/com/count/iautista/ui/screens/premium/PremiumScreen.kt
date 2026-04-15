package com.count.iautista.ui.screens.premium

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.data.billing.BillingService
import com.count.iautista.ui.screens.auth.AuthViewModel
import com.count.iautista.ui.screens.auth.BillingViewModel
import com.count.iautista.ui.utils.findActivity
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    billingViewModel: BillingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val isPremium by billingViewModel.isPremium.collectAsState()
    val ttsDailyCount by billingViewModel.ttsDailyCount.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        billingViewModel.billingError.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showLoginRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showLoginRequiredDialog = false },
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
            title = { Text("Conta necessária") },
            text = {
                Text("Para assinar o Premium, você precisa estar logado. Assim sua assinatura fica vinculada à sua conta e funciona em qualquer dispositivo.")
            },
            confirmButton = {
                Button(onClick = {
                    showLoginRequiredDialog = false
                    onNavigateToLogin()
                }) {
                    Text("Entrar ou criar conta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginRequiredDialog = false }) {
                    Text("Agora não")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (isPremium) {
                // ── Já é premium ─────────────────────────────────────────────
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Você é Premium",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Todos os recursos estão liberados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                // ── Header ────────────────────────────────────────────────────
                Text(
                    "Vozinha Premium",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Comunicação sem limites para a sua criança",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Benefícios ────────────────────────────────────────────────
                BenefitRow(
                    icon = Icons.Filled.RecordVoiceOver,
                    title = "Voz natural ilimitada",
                    subtitle = "Síntese Azure Neural sem limite diário (hoje: $ttsDailyCount/${BillingService.FREE_TTS_DAILY_LIMIT} no plano gratuito)",
                )
                BenefitRow(
                    icon = Icons.Filled.GridView,
                    title = "Itens personalizados ilimitados",
                    subtitle = "Plano gratuito: até ${BillingService.FREE_CUSTOM_ITEMS_LIMIT} itens",
                )
                BenefitRow(
                    icon = Icons.Filled.History,
                    title = "Histórico completo",
                    subtitle = "Plano gratuito: últimos ${BillingService.FREE_HISTORY_DAYS} dias",
                )
                BenefitRow(
                    icon = Icons.Filled.People,
                    title = "Múltiplos perfis",
                    subtitle = "Até 5 perfis de criança",
                )
                BenefitRow(
                    icon = Icons.Filled.CloudUpload,
                    title = "Backup automático",
                    subtitle = "Seus dados sincronizados e seguros",
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Planos ────────────────────────────────────────────────────
                Text(
                    "Escolha seu plano",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))

                PlanCard(
                    title = "Mensal",
                    price = "R$ 29,99",
                    period = "/mês",
                    highlight = false,
                    onClick = {
                        if (!authState.isLoggedIn) {
                            showLoginRequiredDialog = true
                        } else {
                            context.findActivity()?.let {
                                billingViewModel.launchBillingFlow(it, BillingService.PREMIUM_MONTHLY_ID)
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(10.dp))

                PlanCard(
                    title = "Anual",
                    price = "R$ 199,99",
                    period = "/ano  •  R$ 16,67/mês",
                    badge = "44% OFF",
                    highlight = true,
                    onClick = {
                        if (!authState.isLoggedIn) {
                            showLoginRequiredDialog = true
                        } else {
                            context.findActivity()?.let {
                                billingViewModel.launchBillingFlow(it, BillingService.PREMIUM_ANNUAL_ID)
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { billingViewModel.restorePurchases() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Restaurar compra anterior",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(22.dp)
                .padding(top = 2.dp),
        )
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    highlight: Boolean,
    badge: String? = null,
    onClick: () -> Unit,
) {
    val containerColor = if (highlight)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    val borderColor = if (highlight)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(
            width = if (highlight) 2.dp else 1.dp,
            color = borderColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    )
                    if (badge != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    period,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    price,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (highlight)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface,
                )

                Surface(
                    color = if (highlight)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        "Assinar",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (highlight)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}
