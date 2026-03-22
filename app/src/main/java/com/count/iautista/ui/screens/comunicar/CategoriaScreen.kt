package com.count.iautista.ui.screens.comunicar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.ui.components.CommunicationItemCard
import com.count.iautista.ui.components.PhraseBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaScreen(
    categoryId: Long,
    onBack: () -> Unit,
    viewModel: ComunicarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = state.selectedCategory?.let { "${it.emoji} ${it.name}" } ?: "",
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
        )

        // Barra de frase montada
        PhraseBar(
            selectedItems = state.phraseItems,
            onSpeak = { viewModel.speakPhrase() },
            onClear = { viewModel.clearPhrase() },
            onRemoveItem = { viewModel.removeItemFromPhrase(it) },
        )

        // Grid de itens
        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.items, key = { it.id }) { item ->
                    Box {
                        CommunicationItemCard(
                            item = item,
                            onClick = {
                                viewModel.addItemToPhrase(item)
                                viewModel.speakItem(item)
                            },
                            buttonSize = ButtonSize.MEDIUM,
                            isSelected = state.phraseItems.contains(item),
                        )
                        // Botão de favorito no canto do card
                        IconButton(
                            onClick = { viewModel.toggleFavorite(item) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp),
                        ) {
                            Icon(
                                imageVector = if (item.isFavorite)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Filled.FavoriteBorder,
                                contentDescription = if (item.isFavorite) "Remover favorito" else "Favoritar",
                                tint = if (item.isFavorite)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
