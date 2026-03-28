package com.count.iautista.ui.screens.comunicar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.ui.components.CategoryCard
import com.count.iautista.ui.components.CommunicationItemCard
import com.count.iautista.ui.components.PhraseBar
import com.count.iautista.ui.components.SectionHeader
import com.count.iautista.ui.sound.LocalSoundManager
import com.count.iautista.ui.theme.ShapeChip

// Título contextual por modo
private fun contextTitleFor(mode: AppMode): String = when (mode) {
    AppMode.CASA    -> "Para agora em casa"
    AppMode.ESCOLA  -> "Para agora na escola"
    AppMode.TERAPIA -> "Para agora na terapia"
}

@Composable
fun ComunicarScreen(
    onCategoryClick: (Long) -> Unit,
    viewModel: ComunicarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sound = LocalSoundManager.current

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Barra de frase (aparece quando há itens selecionados) ─────────────
        AnimatedVisibility(
            visible = state.phraseItems.isNotEmpty(),
            enter = slideInVertically(tween(250)) { -it } + fadeIn(tween(250)),
            exit = slideOutVertically(tween(200)) { -it } + fadeOut(tween(200)),
        ) {
            PhraseBar(
                selectedItems = state.phraseItems,
                onSpeak = { viewModel.speakPhrase() },
                onClear = { viewModel.clearPhrase() },
                onRemoveItem = { viewModel.removeItemFromPhrase(it) },
            )
        }

        // ── Título ────────────────────────────────────────────────────────────
        Text(
            text = "O que você quer dizer?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        // ── Sugestões contextuais do modo ─────────────────────────────────────
        val suggestions = state.appMode.items
        SectionHeader(
            title = contextTitleFor(state.appMode),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(suggestions, key = { it.second }) { (emoji, label) ->
                ContextSuggestionChip(
                    emoji = emoji,
                    label = label,
                    onClick = {
                        sound.playTap()
                        viewModel.speakQuick(label)
                    },
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        // ── Favoritos ─────────────────────────────────────────────────────────
        if (state.favorites.isNotEmpty()) {
            SectionHeader(title = "Favoritos", leadingIcon = Icons.Filled.Star)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.favorites) { item ->
                    CommunicationItemCard(
                        item = item,
                        onClick = {
                            sound.playTap()
                            viewModel.addItemToPhrase(item)
                            viewModel.speakItem(item)
                        },
                        buttonSize = ButtonSize.SMALL,
                        isSelected = state.phraseItems.contains(item),
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }

        // ── Grade de categorias (modo-aware) ────────────────────────────────
        if (state.categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val featured = state.featuredCategories
            val others   = state.otherCategories

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp, top = 0.dp, bottom = 20.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // ── Seção: Em destaque ────────────────────────────────────
                // Guard: só exibe se os IDs do modo existem no DB
                if (featured.isNotEmpty()) {
                    item(key = "header_featured", span = { GridItemSpan(maxLineSpan) }) {
                        GridSectionHeader(
                            title = "Em destaque · ${state.appMode.label}",
                            badge = state.appMode.emoji,
                        )
                    }
                    items(items = featured, key = { "f_${it.id}" }) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { sound.playTap(); onCategoryClick(category.id) },
                        )
                    }
                }

                // ── Seção: Todas as categorias ────────────────────────────
                if (others.isNotEmpty()) {
                    item(key = "header_all", span = { GridItemSpan(maxLineSpan) }) {
                        GridSectionHeader(
                            title = if (featured.isEmpty()) "Categorias" else "Mais categorias",
                            muted = featured.isNotEmpty(),
                        )
                    }
                    items(items = others, key = { "o_${it.id}" }) { category ->
                        CategoryCard(
                            category = category,
                            onClick = { sound.playTap(); onCategoryClick(category.id) },
                        )
                    }
                }
            }
        }
    }
}

// ── Header de seção dentro do LazyVerticalGrid ───────────────────────────────
// Sem padding horizontal próprio — o contentPadding do grid cuida disso.

@Composable
private fun GridSectionHeader(
    title: String,
    badge: String? = null,
    muted: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (muted) FontWeight.Normal else FontWeight.SemiBold,
            ),
            color = if (muted)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        if (badge != null) {
            Surface(
                shape = ShapeChip,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

// ── Chip de sugestão contextual ───────────────────────────────────────────────

@Composable
private fun ContextSuggestionChip(
    emoji: String,
    label: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
