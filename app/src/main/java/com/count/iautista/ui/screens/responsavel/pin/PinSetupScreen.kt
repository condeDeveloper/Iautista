package com.count.iautista.ui.screens.responsavel.pin

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
fun PinSetupScreen(
    onComplete: () -> Unit,
    viewModel: ResponsavelViewModel = hiltViewModel(),
) {
    var firstPin   by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step       by remember { mutableStateOf(PinSetupStep.CREATE) }
    var hasError   by remember { mutableStateOf(false) }

    // Avança para confirmação ao completar 4 dígitos
    LaunchedEffect(firstPin) {
        if (firstPin.length == 4 && step == PinSetupStep.CREATE) {
            step = PinSetupStep.CONFIRM
        }
    }

    // Valida e salva ao completar confirmação
    LaunchedEffect(confirmPin) {
        if (confirmPin.length == 4 && step == PinSetupStep.CONFIRM) {
            if (confirmPin == firstPin) {
                viewModel.savePin(confirmPin)
                onComplete()
            } else {
                hasError   = true
                firstPin   = ""
                confirmPin = ""
                step       = PinSetupStep.CREATE
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
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = step,
                label = "pin_step",
            ) { currentStep ->
                Text(
                    text = when (currentStep) {
                        PinSetupStep.CREATE  -> "Crie um PIN de 4 dígitos"
                        PinSetupStep.CONFIRM -> "Confirme o PIN"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }

            Text(
                text = "O PIN protege a área do responsável.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (hasError) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "PINs não coincidem. Tente novamente.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            PinDots(
                pinLength = 4,
                filledCount = if (step == PinSetupStep.CREATE) firstPin.length
                              else confirmPin.length,
            )

            Spacer(modifier = Modifier.height(44.dp))

            PinKeyboard(
                onDigit = { digit ->
                    hasError = false
                    if (step == PinSetupStep.CREATE) {
                        if (firstPin.length < 4) firstPin += digit
                    } else {
                        if (confirmPin.length < 4) confirmPin += digit
                    }
                },
                onBackspace = {
                    if (step == PinSetupStep.CREATE) {
                        if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                    } else {
                        if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    }
                },
            )
        }
    }
}

private enum class PinSetupStep { CREATE, CONFIRM }
