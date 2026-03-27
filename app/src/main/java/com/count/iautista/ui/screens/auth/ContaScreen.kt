package com.count.iautista.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.ui.screens.responsavel.ResponsavelViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContaScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    responsavelViewModel: ResponsavelViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
) {
    val responsavelState by responsavelViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val isPremium by billingViewModel.isPremium.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Minha conta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (responsavelState.isLoggedIn) {
                // Usuário logado
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Conta conectada",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(32.dp))

                AccountInfoItem(
                    icon = Icons.Filled.Star,
                    title = "Plano atual",
                    value = if (isPremium) "Premium" else "Gratuito",
                )
                HorizontalDivider()

                // Backup manual
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        isSyncing = true
                        responsavelViewModel.pushBackup { success ->
                            isSyncing = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Backup realizado com sucesso!"
                                    else "Erro ao fazer backup. Verifique sua conexão."
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSyncing,
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isSyncing) "Fazendo backup..." else "Fazer backup agora")
                }
                HorizontalDivider()

                if (!isPremium) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Conheça o Premium", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Itens ilimitados, histórico completo e backup automático.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    (context as? Activity)?.let { billingViewModel.launchBillingFlow(it) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Assinar Premium")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { billingViewModel.restorePurchases() },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Restaurar compra", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        responsavelViewModel.signOut()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sair da conta")
                }
                Spacer(modifier = Modifier.height(24.dp))

            } else {
                // Usuário não logado
                Icon(
                    Icons.Filled.PersonOff,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Você está sem conta",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crie uma conta para fazer backup dos seus dados e acessar recursos premium.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text("Entrar ou criar conta")
                }
            }
        }
    }
}

@Composable
private fun AccountInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
