package com.count.iautista.ui.screens.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.ui.components.CommunicationItemCard
import com.count.iautista.ui.components.RoutineCard
import com.count.iautista.ui.components.SectionHeader
import com.count.iautista.ui.theme.ColorAccentYellow
import com.count.iautista.ui.theme.ShapeCard
import com.count.iautista.ui.theme.ShapeChip
import com.count.iautista.ui.theme.ShapeEmojiContainer

@Composable
fun InicioScreen(
    navController: NavController,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {

        // ── 1. SAUDAÇÃO ──────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 8.dp),
            ) {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "O que você precisa?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── 2. ATALHOS RÁPIDOS ───────────────────────────────────────────────
        // Os 4 primeiros formam um grid 2×2 limpo.
        // "Ajuda" ocupa a linha toda — layout intencional, não sobra de grid.
        item {
            val gridActions = listOf(
                "🚽" to "Banheiro",
                "💧" to "Água",
                "🍽️" to "Comida",
                "😴" to "Cansado",
            )
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                gridActions.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { (emoji, label) ->
                            QuickCard(
                                emoji = emoji,
                                label = label,
                                onClick = { viewModel.speakPhrase(label) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                // "Ajuda" — ação de urgência, linha inteira, layout horizontal
                QuickCard(
                    emoji = "🆘",
                    label = "Ajuda",
                    onClick = { viewModel.speakPhrase("Ajuda") },
                    modifier = Modifier.fillMaxWidth(),
                    isWide = true,
                )
            }
        }

        // ── 3. COMO ESTOU — scroll horizontal de emoções ────────────────────
        item {
            SectionHeader(
                title = "Como estou",
                leadingIcon = Icons.Filled.Mood,
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    listOf(
                        "😊" to "Feliz",
                        "😢" to "Triste",
                        "😠" to "Bravo",
                        "😨" to "Assustado",
                        "🤕" to "Dói",
                        "😴" to "Cansado",
                    )
                ) { (emoji, label) ->
                    EmotionCard(
                        emoji = emoji,
                        label = label,
                        onClick = { viewModel.speakPhrase(label) },
                    )
                }
            }
        }
        // Respiro extra após a seção de emoções
        item { Spacer(Modifier.height(6.dp)) }

        // ── 4. AGORA — card featured da atividade atual ──────────────────────
        if (state.routineNow.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Agora",
                    leadingIcon = Icons.Filled.Star,
                    iconTint = ColorAccentYellow,   // estrela dourada — seção principal
                )
            }
            item {
                val nowItem = state.routineNow.first()
                NowFeaturedCard(
                    item = nowItem,
                    onClick = { viewModel.speakPhrase(nowItem.text) },
                )
            }
            // Outros itens "agora" em scroll menor
            if (state.routineNow.size > 1) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 10.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.routineNow.drop(1)) { item ->
                            RoutineCard(
                                item = item,
                                onClick = { viewModel.speakPhrase(item.text) },
                            )
                        }
                    }
                }
            }
        }

        // ── 5. FALAR NOVAMENTE — chips horizontais ───────────────────────────
        if (state.recentPhrases.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Falar novamente",
                    leadingIcon = Icons.Filled.History,
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.recentPhrases.take(6)) { phrase ->
                        ElevatedCard(
                            onClick = { viewModel.speakPhrase(phrase.phraseText) },
                            shape = ShapeChip,
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = 1.dp,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 11.dp,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = phrase.phraseText,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 6. MAIS USADAS — scroll horizontal (opcional) ───────────────────
        if (state.mostUsedItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Mais usadas",
                    leadingIcon = Icons.Filled.Favorite,
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.mostUsedItems) { item ->
                        CommunicationItemCard(
                            item = item,
                            onClick = { viewModel.speakItem(item) },
                            buttonSize = ButtonSize.MEDIUM,
                        )
                    }
                }
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

/**
 * Card compacto para o grid de atalhos rápidos.
 *
 * [isWide] = false → layout vertical (coluna), para uso em grid 2 colunas.
 * [isWide] = true  → layout horizontal (linha), para uso em row inteira (ex: "Ajuda").
 */
@Composable
private fun QuickCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(if (isWide) 64.dp else 92.dp),
        shape = ShapeCard,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(ShapeEmojiContainer)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(ShapeEmojiContainer)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Card de emoção para a seção "Como estou".
 */
@Composable
private fun EmotionCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.size(92.dp),
        shape = ShapeCard,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(ShapeEmojiContainer)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Card destaque da atividade atual da rotina.
 * É o bloco principal da tela Início — hierarquia máxima.
 */
@Composable
private fun NowFeaturedCard(
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
                    .size(90.dp)
                    .clip(ShapeEmojiContainer)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = item.emoji, fontSize = 48.sp)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Atividade de agora",
                    style = MaterialTheme.typography.labelLarge.copy(
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
