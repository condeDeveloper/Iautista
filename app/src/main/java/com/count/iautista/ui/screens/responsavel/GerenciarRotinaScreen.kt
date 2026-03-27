package com.count.iautista.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarRotinaScreen(
    onBack: () -> Unit,
    viewModel: GerenciarRotinaViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var editingItem by remember { mutableStateOf<RoutineItem?>(null) }

    if (state.showAddDialog) {
        AddRoutineItemDialog(
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { text, emoji, hour -> viewModel.addItem(text, emoji, hour) },
        )
    }

    editingItem?.let { item ->
        EditRoutineItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { text, emoji, hour ->
                viewModel.updateItem(item, text, emoji, hour)
                editingItem = null
            },
        )
    }

    if (state.showLimitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLimitDialog() },
            title = { Text("Limite atingido") },
            text = {
                Text("A versão gratuita suporta até 5 atividades na rotina. Faça upgrade para adicionar mais.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissLimitDialog() },
                    shape = ShapeButton,
                ) { Text("Entendido") }
            },
            shape = ShapeCard,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rotina do dia",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.tryShowAddDialog() },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nova atividade") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->

        if (state.items.isEmpty()) {
            // Estado vazio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("📅", fontSize = 48.sp)
                    Text(
                        text = "Nenhuma atividade ainda.",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Toque em \"Nova atividade\" para montar a rotina do dia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 96.dp, // espaço para o FAB
            ),
        ) {
            itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                RoutineItemRow(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == state.items.lastIndex,
                    onMoveUp = { viewModel.moveUp(item) },
                    onMoveDown = { viewModel.moveDown(item) },
                    onEdit = { editingItem = item },
                    onDelete = { viewModel.removeItem(item) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun RoutineItemRow(
    item: RoutineItem,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remover atividade?") },
            text = { Text("\"${item.text}\" será removida da rotina.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Emoji container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(ShapeEmojiContainer)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.emoji, fontSize = 22.sp)
        }

        // Texto + badge de horário
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.suggestedHour != null) {
                Text(
                    text = "${item.suggestedHour}h",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Reordenação
        Column {
            IconButton(
                onClick = onMoveUp,
                enabled = !isFirst,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Subir",
                    modifier = Modifier.size(20.dp),
                    tint = if (!isFirst)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = !isLast,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Descer",
                    modifier = Modifier.size(20.dp),
                    tint = if (!isLast)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }

        // Editar
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Editar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Deletar
        IconButton(onClick = { showDeleteConfirm = true }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Remover",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddRoutineItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (text: String, emoji: String, hour: Int?) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var hourInput by remember { mutableStateOf("") }
    var textError by remember { mutableStateOf(false) }
    var emojiError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nova atividade",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nome da atividade
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        textError = false
                    },
                    label = { Text("Nome da atividade") },
                    placeholder = { Text("Ex: Café da manhã") },
                    isError = textError,
                    supportingText = if (textError) {
                        { Text("Campo obrigatório") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )

                // Emoji
                OutlinedTextField(
                    value = emoji,
                    onValueChange = {
                        // Limita a 2 caracteres para suportar emojis compostos
                        if (it.length <= 2) {
                            emoji = it
                            emojiError = false
                        }
                    },
                    label = { Text("Emoji") },
                    placeholder = { Text("Ex: 🥐") },
                    isError = emojiError,
                    supportingText = if (emojiError) {
                        { Text("Informe um emoji") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )

                // Horário sugerido
                OutlinedTextField(
                    value = hourInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        val num = filtered.toIntOrNull()
                        if (filtered.isEmpty() || (num != null && num in 0..23)) {
                            hourInput = filtered
                        }
                    },
                    label = { Text("Horário sugerido (opcional)") },
                    placeholder = { Text("Ex: 8  → 8h da manhã") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    textError = text.isBlank()
                    emojiError = emoji.isBlank()
                    if (!textError && !emojiError) {
                        onConfirm(text, emoji, hourInput.toIntOrNull())
                    }
                },
                shape = ShapeButton,
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = ShapeCard,
    )
}

@Composable
private fun EditRoutineItemDialog(
    item: RoutineItem,
    onDismiss: () -> Unit,
    onConfirm: (text: String, emoji: String, hour: Int?) -> Unit,
) {
    var text by remember { mutableStateOf(item.text) }
    var emoji by remember { mutableStateOf(item.emoji) }
    var hourInput by remember { mutableStateOf(item.suggestedHour?.toString() ?: "") }
    var textError by remember { mutableStateOf(false) }
    var emojiError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar atividade",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; textError = false },
                    label = { Text("Nome da atividade") },
                    isError = textError,
                    supportingText = if (textError) { { Text("Campo obrigatório") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = {
                        if (it.length <= 2) { emoji = it; emojiError = false }
                    },
                    label = { Text("Emoji") },
                    isError = emojiError,
                    supportingText = if (emojiError) { { Text("Informe um emoji") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )
                OutlinedTextField(
                    value = hourInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        val num = filtered.toIntOrNull()
                        if (filtered.isEmpty() || (num != null && num in 0..23)) {
                            hourInput = filtered
                        }
                    },
                    label = { Text("Horário sugerido (opcional)") },
                    placeholder = { Text("Ex: 8  → 8h da manhã") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeCard,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    textError = text.isBlank()
                    emojiError = emoji.isBlank()
                    if (!textError && !emojiError) {
                        onConfirm(text, emoji, hourInput.toIntOrNull())
                    }
                },
                shape = ShapeButton,
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = ShapeCard,
    )
}
