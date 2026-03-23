package com.count.iautista.ui.screens.historico

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.History
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
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.ui.components.SectionHeader
import com.count.iautista.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoricoScreen(
    viewModel: HistoricoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Recentes", "Hoje", "Esta semana")

    // Frases mais repetidas — top 5 por frequência de texto
    val topPhrases = remember(state.phrases) {
        state.phrases
            .groupBy { it.phraseText }
            .entries
            .sortedByDescending { it.value.size }
            .take(5)
            .map { it.key to it.value.size }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 4.dp),
        )

        // ── Tabs ──────────────────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            itemsIndexed(tabs) { index, label ->
                val selected = state.selectedTab.ordinal == index
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectTab(HistoricoTab.values()[index]) },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    shape = ShapeChip,
                )
            }
        }

        if (state.phrases.isEmpty()) {
            // ── Estado vazio ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("💬", style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = "Nenhuma frase ainda.",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Comece a se comunicar na aba Comunicar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {

                // ── Falar novamente — frases mais usadas ──────────────────────
                if (topPhrases.isNotEmpty()) {
                    item(key = "speak_again_header") {
                        SectionHeader(
                            title = "Falar novamente",
                            leadingIcon = Icons.AutoMirrored.Filled.VolumeUp,
                            iconTint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    item(key = "speak_again_row") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(topPhrases, key = { it.first }) { (text, count) ->
                                SpeakAgainCard(
                                    phraseText = text,
                                    count = count,
                                    onClick = {
                                        // Encontra a frase mais recente com esse texto e repete
                                        state.phrases
                                            .firstOrNull { it.phraseText == text }
                                            ?.let { viewModel.speakAgain(it) }
                                    },
                                )
                            }
                        }
                    }
                    item(key = "divider_after_speak") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }

                // ── Lista agrupada por dia (SEMANA) ou por horário (HOJE) ─────
                if (state.selectedTab == HistoricoTab.SEMANA) {
                    val grouped = state.phrases
                        .groupBy { it.createdAt.toLocalDate() }
                        .entries
                        .sortedByDescending { it.key }

                    grouped.forEach { (date, dayPhrases) ->
                        item(key = "day_$date") {
                            DayGroupHeader(date = date)
                        }
                        items(dayPhrases, key = { "w_${it.id}" }) { phrase ->
                            PhraseHistoryItem(
                                phrase = phrase,
                                showTime = true,
                                onSpeakAgain = { viewModel.speakAgain(phrase) },
                            )
                        }
                    }
                } else if (state.selectedTab == HistoricoTab.HOJE) {
                    val grouped = state.phrases
                        .groupBy { timeSlot(it.createdAt.hour) }
                        .entries
                        .sortedBy { timeSlotOrder(it.key) }

                    if (grouped.isEmpty()) {
                        item(key = "empty_today") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Nada registrado hoje",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        grouped.forEach { (slot, slotPhrases) ->
                            item(key = "slot_$slot") {
                                TimeSlotHeader(slot = slot, count = slotPhrases.size)
                            }
                            items(slotPhrases, key = { "t_${it.id}" }) { phrase ->
                                PhraseHistoryItem(
                                    phrase = phrase,
                                    showTime = true,
                                    onSpeakAgain = { viewModel.speakAgain(phrase) },
                                )
                            }
                        }
                    }
                } else {
                    // RECENTES — lista simples com data relativa
                    item(key = "recent_header") {
                        SectionHeader(
                            title = "Mais recentes",
                            leadingIcon = Icons.Filled.History,
                            iconTint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    items(state.phrases, key = { it.id }) { phrase ->
                        PhraseHistoryItem(
                            phrase = phrase,
                            showTime = true,
                            onSpeakAgain = { viewModel.speakAgain(phrase) },
                        )
                    }
                }
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

/**
 * Card compacto para a seção "Falar novamente" — scroll horizontal.
 * Mostra texto da frase + quantas vezes foi usada.
 */
@Composable
private fun SpeakAgainCard(
    phraseText: String,
    count: Int,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(88.dp),
        shape = ShapeCard,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = phraseText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (count > 1) "$count vezes" else "1 vez",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(ShapeCircle)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Falar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Cabeçalho de grupo de dia para a aba "Esta semana".
 */
@Composable
private fun DayGroupHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
            .replaceFirstChar { it.uppercaseChar() } +
                " · " + date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 4.dp),
    )
}

/**
 * Cabeçalho de faixa de horário para a aba "Hoje".
 */
@Composable
private fun TimeSlotHeader(slot: String, count: Int) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = slot,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .clip(ShapeChip)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Item individual de frase no histórico.
 * Botão de falar proeminente no lado direito.
 */
@Composable
private fun PhraseHistoryItem(
    phrase: PhraseHistory,
    showTime: Boolean,
    onSpeakAgain: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSpeakAgain)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phrase.phraseText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (showTime) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = phrase.createdAt.format(
                        DateTimeFormatter.ofPattern("HH:mm · dd/MM"),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        // Botão de falar — maior e mais proeminente
        FilledTonalIconButton(
            onClick = onSpeakAgain,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Falar novamente",
                modifier = Modifier.size(22.dp),
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

// ── Helpers de horário ────────────────────────────────────────────────────────

private fun timeSlot(hour: Int): String = when (hour) {
    in 5..11  -> "Manhã"
    in 12..17 -> "Tarde"
    in 18..22 -> "Noite"
    else      -> "Madrugada"
}

private fun timeSlotOrder(slot: String): Int = when (slot) {
    "Manhã"     -> 0
    "Tarde"     -> 1
    "Noite"     -> 2
    "Madrugada" -> 3
    else        -> 4
}
