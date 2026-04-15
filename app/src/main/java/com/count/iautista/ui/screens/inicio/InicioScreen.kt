package com.count.iautista.ui.screens.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.*
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.count.iautista.domain.model.AppMode
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.ui.components.CommunicationItemCard
import com.count.iautista.ui.components.SectionHeader
import com.count.iautista.ui.sound.LocalSoundManager
import com.count.iautista.ui.theme.ShapeButton
import com.count.iautista.ui.theme.ShapeCard
import com.count.iautista.ui.theme.ShapeChip
import com.count.iautista.ui.theme.ShapeCircle
import com.count.iautista.ui.theme.ShapeEmojiContainer

// ── Dados contextuais por modo ────────────────────────────────────────────────
// Os itens vêm de AppMode.items — fonte única de verdade no domínio.

private fun subtitleFor(mode: AppMode): String = when (mode) {
    AppMode.CASA    -> "O que você precisa agora?"
    AppMode.ESCOLA  -> "O que quer dizer na escola?"
    AppMode.TERAPIA -> "Como você está se sentindo?"
}

private fun contextHintFor(mode: AppMode): String = when (mode) {
    AppMode.CASA    -> "Nenhuma atividade agora · explore abaixo"
    AppMode.ESCOLA  -> "Em sala · use os atalhos para se comunicar"
    AppMode.TERAPIA -> "Em sessão · escolha como você está"
}

private val universalNeeds = listOf(
    "🚽" to "Banheiro",
    "💧" to "Água",
    "🤕" to "Dói",
    "🆘" to "Ajuda",
    "😴" to "Cansado",
)

// Fallback estático caso o banco ainda não tenha carregado
private val emotionsFallback = listOf(
    "😊" to "Feliz",
    "😢" to "Triste",
    "😠" to "Bravo",
    "😨" to "Assustado",
    "🤕" to "Dói",
    "😴" to "Cansado",
)

// ── Tela principal ────────────────────────────────────────────────────────────

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun InicioScreen(
    navController: NavController,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    // TTS coletado separadamente — mudanças de loading/playing NÃO recomputam uiState
    val loadingLabel by viewModel.loadingLabel.collectAsState()
    val playingLabel by viewModel.playingLabel.collectAsState()
    val sound = LocalSoundManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {

        // ── 1. SAUDAÇÃO ───────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 4.dp),
            ) {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitleFor(state.appMode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── 2. SELETOR DE MODO ────────────────────────────────────────────────
        item {
            ModeSelectorV2(
                selected = state.appMode,
                onSelect = { viewModel.setMode(it) },
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
            )
        }

        // ── 3. AGORA (rotina ou contexto) ─────────────────────────────────────
        item {
            SmartNowCard(
                routineItem = state.routineNow.firstOrNull(),
                appMode = state.appMode,
                onClick = {
                    state.routineNow.firstOrNull()?.let { item ->
                        sound.playTap()
                        viewModel.speakPhrase(item.text)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── 4. ATALHOS CONTEXTUAIS ────────────────────────────────────────────
        // Fonte: GetContextualSuggestionsUseCase — prioriza histórico recente e
        // faixa horária antes de cair nos itens padrão do modo.
        item {
            SectionHeader(title = "Para agora · ${state.appMode.label}")
        }
        item {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.contextSuggestions.forEach { suggestion ->
                    QuickCard(
                        emoji = suggestion.emoji,
                        label = suggestion.label,
                        imageUri = suggestion.imageUri,
                        isLoading = loadingLabel == suggestion.label,
                        isPlaying = playingLabel == suggestion.label,
                        onClick = {
                            sound.playTap()
                            viewModel.speakSuggestion(suggestion.label)
                        },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // ── 5. NECESSIDADES RÁPIDAS (universal) ───────────────────────────────
        item {
            SectionHeader(title = "Necessidades")
        }
        item {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (state.universalNeedItems.isNotEmpty()) {
                    state.universalNeedItems.forEach { item ->
                        CommunicationItemCard(
                            item = item,
                            onClick = {
                                sound.playTap()
                                viewModel.speakItem(item)
                            },
                            buttonSize = ButtonSize.SMALL,
                            isLoading = loadingLabel == item.text,
                            isPlaying = playingLabel == item.text,
                        )
                    }
                } else {
                    // Placeholder enquanto banco carrega — evita flash de emoji→ARASAAC
                    repeat(5) { ItemCardPlaceholder() }
                }
            }
        }

        // ── 6. COMO ESTOU ─────────────────────────────────────────────────────
        item {
            SectionHeader(title = "Como estou", leadingIcon = Icons.Filled.Mood)
        }
        if (state.emotionItems.isEmpty()) {
            // Placeholder enquanto banco carrega — evita flash de emoji→ARASAAC
            item(key = "emotions_loading") {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) { repeat(8) { ItemCardPlaceholder() } }
            }
        } else {
            // Cada linha é um item independente do LazyColumn — compõe 4 cards por frame
            // em vez de 8 de uma vez, reduzindo jank ao rolar pra cá pela primeira vez.
            items(
                items = state.emotionItems.chunked(4),
                key = { chunk -> "emotions_${chunk.first().id}" },
            ) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    rowItems.forEach { item ->
                        CommunicationItemCard(
                            item = item,
                            onClick = {
                                sound.playTap()
                                viewModel.speakItem(item)
                            },
                            buttonSize = ButtonSize.SMALL,
                            isLoading = loadingLabel == item.text,
                            isPlaying = playingLabel == item.text,
                        )
                    }
                    repeat(4 - rowItems.size) { Spacer(Modifier.size(88.dp, 116.dp)) }
                }
            }
        }

        // ── 7. FALAR NOVAMENTE ────────────────────────────────────────────────
        if (state.recentPhrases.isNotEmpty()) {
            item {
                SectionHeader(title = "Falar novamente", leadingIcon = Icons.Filled.History)
            }
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.recentPhrases.take(6).forEach { phrase ->
                        val isChipLoading = loadingLabel == phrase.phraseText
                        val isChipPlaying = playingLabel == phrase.phraseText
                        ElevatedCard(
                            onClick = {
                                sound.playTap()
                                viewModel.speakPhrase(phrase.phraseText)
                            },
                            shape = ShapeChip,
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                when {
                                    isChipLoading -> CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    isChipPlaying -> Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    else -> Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
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

        // ── 8. MAIS USADAS ────────────────────────────────────────────────────
        if (state.mostUsedItems.isNotEmpty()) {
            item {
                SectionHeader(title = "Mais usadas", leadingIcon = Icons.Filled.Favorite)
            }
            items(
                items = state.mostUsedItems.chunked(4),
                key = { chunk -> "mostused_${chunk.first().id}" },
            ) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    rowItems.forEach { item ->
                        CommunicationItemCard(
                            item = item,
                            onClick = {
                                sound.playTap()
                                viewModel.speakItem(item)
                            },
                            buttonSize = ButtonSize.SMALL,
                            isLoading = loadingLabel == item.text,
                            isPlaying = playingLabel == item.text,
                        )
                    }
                    repeat(4 - rowItems.size) { Spacer(Modifier.size(88.dp, 116.dp)) }
                }
            }
        }
    }
}

// ── Seletor de modo V2 ────────────────────────────────────────────────────────

@Composable
private fun ModeSelectorV2(
    selected: AppMode,
    onSelect: (AppMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeChip,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppMode.entries.forEach { mode ->
                val isSelected = mode == selected
                Surface(
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f),
                    shape = ShapeButton,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(text = mode.emoji, fontSize = 18.sp)
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Card "Agora" inteligente ──────────────────────────────────────────────────

@Composable
private fun SmartNowCard(
    routineItem: RoutineItem?,
    appMode: AppMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (routineItem != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = ShapeCard,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(ShapeCard)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) { Text(text = routineItem.emoji, fontSize = 34.sp) }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Agora",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Surface(
                            shape = ShapeCircle,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = "${appMode.emoji} ${appMode.label}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(
                        text = routineItem.text,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
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
                        .size(52.dp)
                        .clip(ShapeEmojiContainer)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) { Text(text = appMode.emoji, fontSize = 26.sp) }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = appMode.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = contextHintFor(appMode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

@Composable
private fun QuickCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    imageUri: String? = null,
    isLoading: Boolean = false,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .width(92.dp)
            .height(100.dp),
        shape = ShapeCard,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
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
                    .background(
                        if (isLoading || isPlaying)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center,
            ) {
                val context = LocalContext.current
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    isPlaying -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    imageUri != null && imageUri.startsWith("android.resource://") -> {
                        val resId = remember(imageUri) {
                            context.resources.getIdentifier(
                                imageUri.substringAfterLast("/"),
                                "drawable",
                                context.packageName,
                            )
                        }
                        if (resId != 0) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = label,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(36.dp),
                            )
                        } else {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                    imageUri != null -> {
                        Text(text = emoji, fontSize = 22.sp)
                        AsyncImage(
                            model = imageUri,
                            contentDescription = label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    else -> Text(text = emoji, fontSize = 22.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun NeedChip(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isPlaying: Boolean = false,
) {
    ElevatedCard(
        onClick = onClick,
        shape = ShapeChip,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                isPlaying -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                else -> Text(text = emoji, fontSize = 18.sp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun EmotionCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isPlaying: Boolean = false,
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
                    .background(
                        if (isLoading || isPlaying)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    isPlaying -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                    else -> Text(text = emoji, fontSize = 26.sp)
                }
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Card cinza sem conteúdo — substitui emojis durante carregamento inicial do banco. */
@Composable
private fun ItemCardPlaceholder() {
    Box(
        modifier = Modifier
            .size(88.dp, 116.dp)
            .clip(ShapeCard)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .alpha(0.5f),
    )
}
