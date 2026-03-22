package com.count.iautista.ui.screens.configuracoes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.AppTheme
import com.count.iautista.domain.model.ButtonSize
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    onBack: () -> Unit,
    viewModel: ConfiguracoesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {

            // ── Tamanho dos cartões ───────────────────────────────────────────
            item {
                PrefSectionTitle("Tamanho dos cartões")
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Preview ao vivo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        ButtonSize.values().forEach { size ->
                            val sizeDp = when (size) {
                                ButtonSize.SMALL  -> 88.dp
                                ButtonSize.MEDIUM -> 104.dp
                                ButtonSize.LARGE  -> 120.dp
                            }
                            val emojiSp = when (size) {
                                ButtonSize.SMALL  -> 28.sp
                                ButtonSize.MEDIUM -> 36.sp
                                ButtonSize.LARGE  -> 44.sp
                            }
                            val isSelected = state.buttonSize == size
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(sizeDp, sizeDp + 24.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 1.dp,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    Text("😊", fontSize = emojiSp, textAlign = TextAlign.Center)
                                    Text(
                                        text = "Feliz",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    // Seletor segmentado
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            ButtonSize.SMALL  to "Pequeno",
                            ButtonSize.MEDIUM to "Médio",
                            ButtonSize.LARGE  to "Grande",
                        )
                        options.forEachIndexed { index, (size, label) ->
                            SegmentedButton(
                                selected = state.buttonSize == size,
                                onClick = { viewModel.setButtonSize(size) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = options.size,
                                ),
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
            }

            // ── Voz (TTS) ─────────────────────────────────────────────────────
            item {
                PrefSectionTitle("Voz")
            }
            item {
                // Switch: fala ativada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fala automática", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Falar ao tocar em um cartão",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.ttsEnabled,
                        onCheckedChange = { viewModel.setTtsEnabled(it) },
                    )
                }
            }
            if (state.ttsEnabled) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Velocidade da voz", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${(state.ttsRate * 10).roundToInt() / 10.0}×",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Slider(
                            value = state.ttsRate,
                            onValueChange = { viewModel.setTtsRate(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 14,  // 0.1 por passo
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Lenta", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rápida", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // ── Aparência ─────────────────────────────────────────────────────
            item {
                PrefSectionTitle("Aparência")
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    listOf(
                        AppTheme.LIGHT         to "Claro",
                        AppTheme.DARK          to "Escuro (em breve)",
                        AppTheme.HIGH_CONTRAST to "Alto contraste (em breve)",
                    ).forEach { (theme, label) ->
                        val isAvailable = theme == AppTheme.LIGHT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.appTheme == theme,
                                onClick = { if (isAvailable) viewModel.setAppTheme(theme) },
                                enabled = isAvailable,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isAvailable)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun PrefSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}
