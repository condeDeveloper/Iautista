package com.count.iautista.ui.screens.rotina

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.ui.components.RoutineCard
import com.count.iautista.ui.components.SectionHeader

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
        // Cabeçalho com botão de reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Rotina de hoje",
                style = MaterialTheme.typography.displayLarge,
            )
            IconButton(onClick = { showResetDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reiniciar dia",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Estado vazio global
        if (state.nowItems.isEmpty() && state.nextItems.isEmpty() && state.laterItems.isEmpty() && state.doneItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A rotina está vazia.\nAdicione atividades na aba Responsável.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            RoutineSection(
                title = "⭐ Agora",
                items = state.nowItems,
                emptyText = "Nenhuma atividade agora",
                onItemClick = { viewModel.markAsDone(it) },
            )
            RoutineSection(
                title = "➡️ A seguir",
                items = state.nextItems,
                emptyText = "Nada em seguida",
                onItemClick = { viewModel.markAsNow(it) },
            )
            RoutineSection(
                title = "🕐 Mais tarde",
                items = state.laterItems,
                emptyText = "Nada mais tarde",
                onItemClick = { viewModel.markAsNow(it) },
            )
            if (state.doneItems.isNotEmpty()) {
                RoutineSection(
                    title = "✅ Concluído",
                    items = state.doneItems,
                    emptyText = "",
                    onItemClick = {},
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RoutineSection(
    title: String,
    items: List<RoutineItem>,
    emptyText: String,
    onItemClick: (RoutineItem) -> Unit,
) {
    SectionHeader(title = title)

    if (items.isEmpty() && emptyText.isNotBlank()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                RoutineCard(
                    item = item,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
