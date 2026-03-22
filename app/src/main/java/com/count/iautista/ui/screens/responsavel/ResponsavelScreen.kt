package com.count.iautista.ui.screens.responsavel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.ui.components.PinKeyboard

@Composable
fun ResponsavelScreen(
    onNavigateToPinSetup: () -> Unit,
    onNavigateToGerenciarItens: () -> Unit,
    onNavigateToConfiguracoes: () -> Unit,
    onNavigateToConta: () -> Unit,
    onRequirePin: () -> Unit,
    viewModel: ResponsavelViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var pinUnlocked by remember { mutableStateOf(false) }

    if (!pinUnlocked && state.isPinConfigured) {
        PinLockScreen(
            pinInput = pinInput,
            hasError = pinError,
            onDigit = { if (pinInput.length < 4) pinInput += it },
            onBackspace = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
            onValidate = {
                if (pinInput.length == 4) {
                    viewModel.validatePin(pinInput) { valid ->
                        if (valid) {
                            pinUnlocked = true
                            pinError = false
                        } else {
                            pinError = true
                            pinInput = ""
                        }
                    }
                }
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Text(
                text = "Responsável",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            )
        }

        // Perfil da criança
        item {
            SectionTitle("Perfil da criança")
            SettingsItem(
                icon = Icons.Filled.Person,
                title = state.profile?.name?.ifBlank { "Configurar perfil" } ?: "Configurar perfil",
                subtitle = "Nome e foto",
                onClick = { /* navegar para edição de perfil */ },
            )
        }

        // Comunicação
        item {
            SectionTitle("Comunicação")
            SettingsItem(
                icon = Icons.Filled.GridView,
                title = "Gerenciar itens",
                subtitle = "Adicionar, editar e remover itens",
                onClick = onNavigateToGerenciarItens,
            )
        }

        // Visual
        item {
            SectionTitle("Aparência")
            SettingsItem(
                icon = Icons.Filled.FormatSize,
                title = "Tamanho dos botões e voz",
                subtitle = "Ajustar aparência e velocidade de fala",
                onClick = onNavigateToConfiguracoes,
            )
        }

        // Segurança
        item {
            SectionTitle("Segurança")
            SettingsItem(
                icon = Icons.Filled.Lock,
                title = if (state.isPinConfigured) "Alterar PIN" else "Configurar PIN",
                subtitle = "Proteger área do responsável",
                onClick = onNavigateToPinSetup,
            )
        }

        // Conta
        item {
            SectionTitle("Conta")
            if (state.isLoggedIn) {
                SettingsItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "Minha conta",
                    subtitle = "Gerenciar conta e backup",
                    onClick = onNavigateToConta,
                )
                SettingsItem(
                    icon = Icons.Filled.Logout,
                    title = "Sair",
                    subtitle = "Desconectar da conta",
                    onClick = { viewModel.signOut() },
                )
            } else {
                SettingsItem(
                    icon = Icons.Filled.Login,
                    title = "Entrar ou criar conta",
                    subtitle = "Para backup e recursos premium",
                    onClick = onNavigateToConta,
                )
            }
        }

        // Premium
        item {
            SectionTitle("Premium")
            SettingsItem(
                icon = Icons.Filled.Star,
                title = "Conhecer o Premium",
                subtitle = "Itens ilimitados, backup e mais",
                onClick = { },
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}

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
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Digite o PIN",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            if (hasError) {
                Text("PIN incorreto", color = MaterialTheme.colorScheme.errorContainer)
            }
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .then(
                                if (i < pinInput.length)
                                    Modifier
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            modifier = Modifier.size(20.dp),
                            shape = MaterialTheme.shapes.small,
                            color = if (i < pinInput.length)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        ) {}
                    }
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
