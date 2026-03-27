package com.count.iautista.ui.screens.responsavel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.CommunicationItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarItensScreen(
    onBack: () -> Unit,
    onNavigateToAddItem: (categoryId: Long?) -> Unit = {},
    viewModel: GerenciarItensViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Snackbar para erro de deleção de item padrão
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.deleteError) {
        state.deleteError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearDeleteError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar itens") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddItem(state.expandedCategoryId) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo item") },
            )
        },
    ) { padding ->
        if (state.categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp), // espaço para FAB
        ) {
            state.categories.forEach { category ->
                val isExpanded = state.expandedCategoryId == category.id

                // Cabeçalho da categoria — toque expande/recolhe
                item(key = "cat_${category.id}") {
                    Surface(
                        onClick = { viewModel.toggleCategory(category.id) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = category.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = if (isExpanded)
                                    Icons.Filled.KeyboardArrowUp
                                else
                                    Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider()
                }

                // Itens da categoria expandida
                if (isExpanded) {
                    if (state.isLoadingItems) {
                        item(key = "loading_${category.id}") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    } else if (state.categoryItems.isEmpty()) {
                        item(key = "empty_${category.id}") {
                            Text(
                                text = "Nenhum item nesta categoria",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 56.dp, vertical = 12.dp),
                            )
                        }
                    } else {
                        items(
                            items = state.categoryItems,
                            key = { "item_${it.id}" },
                        ) { item ->
                            ItemRow(
                                item = item,
                                onDelete = { viewModel.deleteItem(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: CommunicationItem,
    onDelete: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remover item?") },
            text = { Text("\"${item.text}\" será removido permanentemente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = item.emoji.ifBlank { "📌" }, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.text, style = MaterialTheme.typography.bodyLarge)
            if (!item.isDefault) {
                Text(
                    text = "Personalizado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (!item.isDefault) {
            IconButton(onClick = { showConfirm = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remover",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}
