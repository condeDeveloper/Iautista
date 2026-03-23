package com.count.iautista.ui.screens.rotina

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.ui.components.RoutineCard
import com.count.iautista.ui.components.SectionHeader
import com.count.iautista.ui.theme.*

@Composable
fun RotinaScreen(
    viewModel: RotinaViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reiniciar rotina?") },
            text = { Text("Todos os itens voltarão para 'Mais tarde'. Continuar?") },
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Rotina de hoje",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
            IconButton(onClick = { showResetDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reiniciar dia",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Estado vazio ─────────────────────────────────────────────────────
        val isEmpty = state.nowItems.isEmpty() && state.nextItems.isEmpty() &&
                state.laterItems.isEmpty() && state.doneItems.isEmpty()

        if (isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                        text = "Adicione atividades na aba Responsável.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {

            // ── AGORA — destaque principal ────────────────────────────────────
            SectionHeader(
                title = "Agora",
                leadingIcon = Icons.Filled.Star,
                iconTint = MaterialTheme.colorScheme.primary,
            )

            if (state.nowItems.isEmpty()) {
                RoutineSectionEmpty(
                    text = "Nenhuma atividade no momento",
                )
            } else {
                // Primeiro item como card featured — mais proeminente
                RoutineFeaturedCard(
                    item = state.nowItems.first(),
                    onClick = { viewModel.markAsDone(state.nowItems.first()) },
                )
                // Demais itens "agora" em scroll horizontal menor
                if (state.nowItems.size > 1) {
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.nowItems.drop(1), key = { it.id }) { item ->
                            RoutineCard(item = item, onClick = { viewModel.markAsDone(item) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── A SEGUIR ──────────────────────────────────────────────────────
            SectionHeader(
                title = "A seguir",
                leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                iconTint = MaterialTheme.colorScheme.secondary,
            )

            if (state.nextItems.isEmpty()) {
                RoutineSectionEmpty(text = "Nada em seguida")
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.nextItems, key = { it.id }) { item ->
                        RoutineCard(item = item, onClick = { viewModel.markAsNow(item) })
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── MAIS TARDE — visual muted ─────────────────────────────────────
            SectionHeader(
                title = "Mais tarde",
                leadingIcon = Icons.Filled.Schedule,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.laterItems.isEmpty()) {
                RoutineSectionEmpty(text = "Nada mais tarde")
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.laterItems, key = { it.id }) { item ->
                        RoutineCard(item = item, onClick = { viewModel.markAsNow(item) })
                    }
                }
            }

            // ── CONCLUÍDO — só aparece quando há itens ────────────────────────
            if (state.doneItems.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                SectionHeader(
                    title = "Concluído (${state.doneItems.size})",
                    leadingIcon = Icons.Filled.CheckCircle,
                    iconTint = ColorSuccess,
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.doneItems, key = { it.id }) { item ->
                        RoutineCard(item = item, onClick = {})
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

/**
 * Card destaque para a primeira atividade de "Agora".
 * Visual mais proeminente que os demais RoutineCards.
 */
@Composable
private fun RoutineFeaturedCard(
    item: RoutineItem,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = ShapeCard,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(ShapeEmojiContainer)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.emoji, fontSize = 42.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Toque quando terminar",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Placeholder quando uma seção está vazia.
 */
@Composable
private fun RoutineSectionEmpty(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
    )
}
