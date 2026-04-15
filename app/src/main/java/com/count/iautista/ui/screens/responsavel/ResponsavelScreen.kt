package com.count.iautista.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.count.iautista.ui.theme.ColorAccentYellow
import com.count.iautista.ui.theme.ColorPrimary
import com.count.iautista.ui.theme.ColorTertiary
import com.count.iautista.ui.theme.ColorWarning
import com.count.iautista.ui.theme.ColorWarningContainer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.ui.components.PinKeyboard
import com.count.iautista.ui.theme.*

@Composable
fun ResponsavelScreen(
    onNavigateToPinSetup: () -> Unit,
    onNavigateToGerenciarPerfis: () -> Unit = {},
    onNavigateToGerenciarItens: () -> Unit,
    onNavigateToGerenciarRotina: () -> Unit,
    onNavigateToConfiguracoes: () -> Unit,
    onNavigateToConta: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    onRequirePin: () -> Unit,
    viewModel: ResponsavelViewModel = hiltViewModel(),
    billingViewModel: com.count.iautista.ui.screens.auth.BillingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val isPremium by billingViewModel.isPremium.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (!state.pinUnlocked && state.isPinConfigured) {
        PinLockScreen(
            pinInput = pinInput,
            hasError = pinError,
            onDigit = { if (pinInput.length < 4) pinInput += it },
            onBackspace = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
            onValidate = {
                if (pinInput.length == 4) {
                    viewModel.validatePin(pinInput) { valid ->
                        if (!valid) {
                            pinError = true
                            pinInput = ""
                        }
                    }
                }
            },
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        item {
            Text(
                text = "Responsável",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
            )
        }

        // ── Card de perfil da criança ──────────────────────────────────────────
        item {
            ProfileCard(
                name = state.profile?.name?.ifBlank { null },
                onClick = onNavigateToGerenciarPerfis,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Seção: Relatórios de hoje ─────────────────────────────────────────
        item {
            SettingsSectionLabel(title = "Hoje")
            TodayReportCard(stats = state.todayStats)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Seção: Comunicação ─────────────────────────────────────────────────
        item {
            SettingsSectionLabel(title = "Comunicação")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.GridView,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Gerenciar itens",
                    subtitle = "Adicionar, editar e remover itens",
                    onClick = onNavigateToGerenciarItens,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Seção: Rotina ──────────────────────────────────────────────────────
        item {
            SettingsSectionLabel(title = "Rotina")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.CalendarToday,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Gerenciar rotina",
                    subtitle = "Adicionar e organizar atividades do dia",
                    onClick = onNavigateToGerenciarRotina,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Seção: Aparência ───────────────────────────────────────────────────
        item {
            SettingsSectionLabel(title = "Aparência")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.FormatSize,
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Tamanho dos botões",
                    subtitle = "Ajustar aparência e velocidade de fala",
                    onClick = onNavigateToConfiguracoes,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Seção: Segurança ───────────────────────────────────────────────────
        item {
            SettingsSectionLabel(title = "Segurança")
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.Lock,
                    iconContainerColor = ColorWarningContainer,
                    iconTint = ColorWarning,
                    title = if (state.isPinConfigured) "Alterar PIN" else "Configurar PIN",
                    subtitle = if (state.isPinConfigured)
                        "Troque o código de acesso"
                    else
                        "Proteger área do responsável",
                    onClick = onNavigateToPinSetup,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Seção: Conta ───────────────────────────────────────────────────────
        item {
            SettingsSectionLabel(title = "Conta")
            SettingsGroup {
                if (state.isLoggedIn) {
                    SettingsRow(
                        icon = Icons.Filled.AccountCircle,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Minha conta",
                        subtitle = "Gerenciar conta e backup",
                        onClick = onNavigateToConta,
                        showDivider = true,
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconContainerColor = MaterialTheme.colorScheme.errorContainer,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "Sair",
                        subtitle = "Desconectar da conta",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.signOut() },
                        showDivider = false,
                    )
                } else {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Login,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Entrar ou criar conta",
                        subtitle = "Para backup e recursos premium",
                        onClick = onNavigateToConta,
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Banner Premium ─────────────────────────────────────────────────────
        if (!isPremium) {
            item {
                PremiumBanner(onClick = onNavigateToPremium)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

/**
 * Card de destaque do perfil da criança.
 */
@Composable
private fun ProfileCard(
    name: String?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(ShapeCircle)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (name != null) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name ?: "Perfil da criança",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Toque para gerenciar perfis",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                )
            }

            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Rótulo de seção — mais visível e bem espaçado.
 */
@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 8.dp),
    )
}

/**
 * Agrupa itens de configuração dentro de um único card arredondado.
 * Cria coesão visual entre itens de uma mesma seção.
 */
@Composable
private fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(content = content)
    }
}

/**
 * Linha individual dentro de um SettingsGroup.
 * Ícone em container colorido + título + subtitle + chevron.
 */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Unspecified,
    showDivider: Boolean = false,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Container do ícone
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(ShapeEmojiContainer)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (titleColor == Color.Unspecified)
                        MaterialTheme.colorScheme.onSurface
                    else titleColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

/**
 * Card compacto com resumo do dia: frases faladas, modo mais usado e progresso da rotina.
 */
@Composable
private fun TodayReportCard(stats: TodayStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ReportStatCell(
                value = stats.totalPhrases.toString(),
                label = "Frases\nhoje",
                icon = Icons.Filled.ChatBubble,
                iconColor = MaterialTheme.colorScheme.primary,
            )

            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            ReportStatCell(
                value = stats.topMode?.emoji ?: "—",
                label = stats.topMode?.label ?: "Nenhum\nmodo",
                icon = Icons.Filled.Place,
                iconColor = MaterialTheme.colorScheme.secondary,
            )

            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            ReportStatCell(
                value = if (stats.routineTotal > 0)
                    "${stats.routineCompleted}/${stats.routineTotal}"
                else "—",
                label = "Rotina\nconcluída",
                icon = Icons.Filled.CheckCircle,
                iconColor = if (stats.routineTotal > 0 && stats.routineCompleted == stats.routineTotal)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReportStatCell(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Banner de destaque para o Premium — visualmente separado do resto.
 * Usa gradiente suave para se destacar sem agredir.
 */
@Composable
private fun PremiumBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(ShapeCard)
            .background(
                Brush.linearGradient(
                    colors = listOf(ColorPrimary, ColorTertiary),
                ),
            )
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Ícone de estrela em container translúcido
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ShapeEmojiContainer)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = ColorAccentYellow,
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Premium",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = Color.White,
                )
                Text(
                    text = "Itens ilimitados, backup e mais recursos",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.88f),
                )
            }

            Box(
                modifier = Modifier
                    .clip(ShapeButton)
                    .background(Color.White.copy(alpha = 0.20f))
                    .border(1.dp, Color.White.copy(alpha = 0.45f), ShapeButton)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Ver plano",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}

// ── Tela de PIN ───────────────────────────────────────────────────────────────

@Composable
private fun PinLockScreen(
    pinInput: String,
    hasError: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onValidate: () -> Unit,
) {
    LaunchedEffect(pinInput) {
        if (pinInput.length == 4) onValidate()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Ícone de cadeado em container
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(ShapeEmojiContainer)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Área do Responsável",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasError) "PIN incorreto. Tente novamente." else "Digite o PIN de 4 dígitos",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indicadores de dígito
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    val filled = i < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(ShapeCircle)
                            .background(
                                if (filled)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            PinKeyboard(
                onDigit = onDigit,
                onBackspace = onBackspace,
            )
        }
    }
}
