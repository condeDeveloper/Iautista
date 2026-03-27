package com.count.iautista.ui.screens.rotina

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.ui.sound.LocalSoundManager
import com.count.iautista.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RotinaScreen(
    onNavigateToGerenciarRotina: () -> Unit = {},
    viewModel: RotinaViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sound = LocalSoundManager.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reiniciar rotina?") },
            text = { Text("Todos os itens voltarão para o início. Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetDay()
                    showResetDialog = false
                }) { Text("Reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") }
            },
        )
    }

    val allItems = state.nowItems + state.nextItems + state.laterItems + state.doneItems
    val isEmpty = allItems.isEmpty()
    val total = allItems.size
    val doneCount = state.doneItems.size
    val progress = if (total > 0) doneCount.toFloat() / total else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Rotina de hoje",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )
                ModeIndicatorBadge(mode = state.appMode)
            }
            if (!isEmpty) {
                IconButton(onClick = { showResetDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reiniciar dia",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── Estado vazio ─────────────────────────────────────────────────────
        if (isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 64.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("📅", style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = "A rotina está vazia.",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Monte a rotina do dia para que as atividades apareçam aqui.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onNavigateToGerenciarRotina,
                        shape = ShapeButton,
                    ) {
                        Text("Montar rotina")
                    }
                }
            }
            return@Column
        }

        // ── Progresso ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (doneCount == total)
                        "Tudo concluído hoje! 🎉"
                    else
                        "$doneCount de $total concluídas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(ShapeButton),
                color = if (doneCount == total) ColorSuccess else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        // ── Tudo concluído ───────────────────────────────────────────────────
        if (doneCount == total) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("⭐", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = "Parabéns!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Você completou todas as atividades do dia.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Column
        }

        // ── Atividade atual (NOW) ─────────────────────────────────────────────
        val nowItem = state.nowItems.firstOrNull()

        if (nowItem != null) {
            NowCard(
                item = nowItem,
                mode = state.appMode,
                onClick = {
                    sound.playComplete()
                    viewModel.markAsDone(nowItem)
                },
            )
            OutlinedButton(
                onClick = {
                    sound.playTap()
                    viewModel.skipItem(nowItem)
                },
                shape = ShapeButton,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp),
            ) {
                Text(
                    text = "Pular esta atividade",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            // Itens existem mas nenhum está em NOW — estado entre atividades
            NoActivityBox(mode = state.appMode)
        }

        // ── Dicas contextuais ─────────────────────────────────────────────────
        // Visível sempre que há atividades — com ou sem atividade ativa.
        // Com atividade: ajuda a criança a comunicar sobre o que está fazendo.
        // Sem atividade: ajuda a criança a comunicar durante o intervalo.
        val hints = contextHintsFor(state.appMode, nowItem)
        if (hints.isNotEmpty()) {
            ContextualHintsRow(
                hints = hints,
                activityText = nowItem?.text,
                onSpeak = { text ->
                    sound.playTap()
                    viewModel.speakHint(text)
                },
            )
        }

        // ── Próxima atividade (NEXT) — preview discreto ──────────────────────
        val nextItem = state.nextItems.firstOrNull()
        if (nextItem != null) {
            NextPreview(item = nextItem)
        }

        // ── Atividades restantes (LATER) — contagem discreta ────────────────
        val laterCount = state.laterItems.size + state.nextItems.drop(1).size
        if (laterCount > 0) {
            Text(
                text = if (laterCount == 1) "Mais 1 atividade depois" else "Mais $laterCount atividades depois",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Card principal — atividade em andamento ───────────────────────────────────

@Composable
private fun NowCard(
    item: RoutineItem,
    mode: AppMode,
    onClick: () -> Unit,
) {
    var showCheck by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val overlayAlpha by animateFloatAsState(
        targetValue = if (showCheck) 0.88f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "overlay",
    )

    ElevatedCard(
        onClick = {
            if (!showCheck) {
                showCheck = true
                scope.launch {
                    delay(480)
                    onClick()
                    showCheck = false
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ShapeCard,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Instrução + modo em linha — conecta a atividade ao contexto ativo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Toque quando terminar",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Surface(
                        shape = ShapeChip,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "${mode.emoji} ${mode.label}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(ShapeCard)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = item.emoji, fontSize = 52.sp)
                }
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                if (item.suggestedHour != null) {
                    Text(
                        text = "${item.suggestedHour}h",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Overlay de confirmação
            if (overlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(ShapeCard)
                        .background(ColorSuccess.copy(alpha = overlayAlpha)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp),
                    )
                }
            }
        }
    }
}

// ── Estado entre atividades ───────────────────────────────────────────────────

private fun noActivityMessageFor(mode: AppMode): String = when (mode) {
    AppMode.CASA    -> "Momento livre · aproveite"
    AppMode.ESCOLA  -> "Entre atividades na escola"
    AppMode.TERAPIA -> "Aguardando a próxima atividade"
}

@Composable
private fun NoActivityBox(mode: AppMode) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ShapeCard,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(ShapeEmojiContainer)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = mode.emoji, fontSize = 24.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = noActivityMessageFor(mode),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Nenhuma atividade ativa no momento.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

// ── Badge do modo ativo ───────────────────────────────────────────────────────

@Composable
private fun ModeIndicatorBadge(mode: AppMode) {
    Text(
        text = "${mode.emoji} ${mode.label}",
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 2.dp),
    )
}

// ── Dicas contextuais de comunicação ──────────────────────────────────────────

/**
 * Seleciona até 3 frases do modo atual priorizando necessidades universais
 * (ajuda, água, dor, pausa, terminou, banheiro) — sempre relevantes durante
 * qualquer atividade.
 *
 * [nowItem] é recebido para extensões futuras (ex: sugestões específicas por atividade).
 */
private fun contextHintsFor(mode: AppMode, nowItem: RoutineItem?): List<Pair<String, String>> {
    val universal = listOf("ajuda", "água", "dor", "pausa", "terminou", "banheiro")
    val items = mode.items
    val prioritized = items.filter { (_, label) ->
        universal.any { label.lowercase().contains(it) }
    }
    val rest = items.filter { it !in prioritized }
    return (prioritized + rest).take(3)
}

@Composable
private fun ContextualHintsRow(
    hints: List<Pair<String, String>>,
    activityText: String?,
    onSpeak: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (activityText != null) "Durante $activityText:" else "O que você quer dizer?",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(hints) { (emoji, label) ->
                SuggestionChip(
                    onClick = { onSpeak(label) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    border = null,
                )
            }
        }
    }
}

// ── Preview discreta da próxima atividade ─────────────────────────────────────

@Composable
private fun NextPreview(item: RoutineItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(ShapeEmojiContainer)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.emoji, fontSize = 20.sp)
        }
        Column {
            Text(
                text = "A seguir",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
