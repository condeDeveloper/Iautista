package com.count.iautista.ui.screens.responsavel.pin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.ui.components.PinDots
import com.count.iautista.ui.components.PinKeyboard
import com.count.iautista.ui.screens.responsavel.ResponsavelViewModel

@Composable
fun PinValidationScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ResponsavelViewModel = hiltViewModel(),
) {
    var pinInput by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) }

    LaunchedEffect(pinInput) {
        if (pinInput.length == 4) {
            viewModel.validatePin(pinInput) { valid ->
                if (valid) {
                    onSuccess()
                } else {
                    hasError  = true
                    attempts += 1
                    pinInput  = ""
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Botão voltar
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Digite o PIN",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
            )

            // Erro com contagem de tentativas
            if (hasError) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = buildString {
                            append("PIN incorreto")
                            if (attempts > 1) append(" ($attempts tentativas)")
                        },
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            PinDots(pinLength = 4, filledCount = pinInput.length)

            Spacer(modifier = Modifier.height(44.dp))

            PinKeyboard(
                onDigit = { digit ->
                    hasError = false
                    if (pinInput.length < 4) pinInput += digit
                },
                onBackspace = {
                    if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                },
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
