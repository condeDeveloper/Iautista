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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.PhraseHistory
import com.count.iautista.ui.components.SectionHeader
import com.count.iautista.ui.screens.auth.BillingViewModel
import com.count.iautista.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoricoScreen(
    onNavigateToPremium: () -> Unit = {},
    viewModel: HistoricoViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val isPremium by billingViewModel.isPremium.collectAsState()
    val tabs = listOf("Agora", "Hoje", "Esta semana")

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
        )

        // ── Tab bar de período ────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            itemsIndexed(tabs) { index, label ->
                val selected = state.selectedTab.ordinal == index
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectTab(HistoricoTab.entries[index]) },
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

        // ── Banner free tier ──────────────────────────────────────────────────
        if (!isPremium) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Histórico limitado a 7 dias no plano gratuito",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onNavigateToPremium) {
                        Text(
                            "Premium",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            ),
                        )
                    }
                }
            }
        }

        // ── Conteúdo ──────────────────────────────────────────────────────────
        if (state.phrases.isEmpty()) {
            EmptyState(tab = state.selectedTab)
        } else {
            val maxCount = state.topPhrases.firstOrNull()?.second?.toFloat() ?: 1f

            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {

                // ── Falar novamente ───────────────────────────────────────────
                item(key = "falar_header") {
                    SectionHeader(
                        title = falarNovamenteTitle(state.selectedTab),
                        leadingIcon = Icons.AutoMirrored.Filled.VolumeUp,
                        iconTint = MaterialTheme.colorScheme.primary,
                    )
                }
                item(key = "falar_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.topPhrases, key = { it.first }) { (text, count) ->
                            SpeakAgainCard(
                                phraseText  = text,
                                count       = count,
                                freqRatio   = count / maxCount,
                                onClick = {
                                    state.phrases
                                        .firstOrNull { it.phraseText == text }
                                        ?.let { viewModel.speakAgain(it) }
                                },
                            )
                        }
                    }
                }

                // ── Por modo (quando há dados em mais de 1 modo) ──────────────
                if (state.modeUsage.size > 1) {
                    item(key = "mode_divider") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                    item(key = "mode_usage") {
                        ModeUsageRow(usages = state.modeUsage)
                    }
                }

                item(key = "main_divider") {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }

                // ── Lista principal (varia por aba) ───────────────────────────
                when (state.selectedTab) {

                    HistoricoTab.AGORA -> {
                        item(key = "agora_header") {
                            SectionHeader(title = "Mais recentes")
                        }
                        items(state.phrases, key = { it.id }) { phrase ->
                            PhraseHistoryItem(
                                phrase = phrase,
                                onSpeakAgain = { viewModel.speakAgain(phrase) },
                            )
                        }
                    }

                    HistoricoTab.HOJE -> {
                        val bySlot = state.phrases
                            .groupBy { timeSlot(it.createdAt.hour) }
                            .entries
                            .sortedBy { timeSlotOrder(it.key) }

                        if (bySlot.isEmpty()) {
                            item(key = "hoje_empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Nada registrado hoje ainda.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            bySlot.forEach { (slot, slotPhrases) ->
                                item(key = "slot_$slot") {
                                    TimeSlotHeader(slot = slot, count = slotPhrases.size)
                                }
                                items(slotPhrases, key = { "t_${it.id}" }) { phrase ->
                                    PhraseHistoryItem(
                                        phrase = phrase,
                                        onSpeakAgain = { viewModel.speakAgain(phrase) },
                                    )
                                }
                            }
                        }
                    }

                    HistoricoTab.SEMANA -> {
                        val byDay = state.phrases
                            .groupBy { it.createdAt.toLocalDate() }
                            .entries
                            .sortedByDescending { it.key }

                        byDay.forEach { (date, dayPhrases) ->
                            item(key = "day_$date") {
                                DayGroupHeader(date = date)
                            }
                            items(dayPhrases, key = { "w_${it.id}" }) { phrase ->
                                PhraseHistoryItem(
                                    phrase = phrase,
                                    onSpeakAgain = { viewModel.speakAgain(phrase) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Helpers de título ─────────────────────────────────────────────────────────

private fun falarNovamenteTitle(tab: HistoricoTab): String = when (tab) {
    HistoricoTab.AGORA  -> "Mais usadas agora"
    HistoricoTab.HOJE   -> "Mais usadas hoje"
    HistoricoTab.SEMANA -> "Mais usadas na semana"
}

// ── Estado vazio ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(tab: HistoricoTab) {
    val subtitle = when (tab) {
        HistoricoTab.AGORA  -> "Nenhuma frase recente.\nComece a comunicar para aparecer aqui."
        HistoricoTab.HOJE   -> "Nenhuma frase hoje ainda.\nUse os atalhos da Home para começar."
        HistoricoTab.SEMANA -> "Nenhuma frase esta semana.\nComece a se comunicar para ver o histórico."
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("💬", style = MaterialTheme.typography.displayLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Seção: Por modo ───────────────────────────────────────────────────────────

@Composable
private fun ModeUsageRow(usages: List<ModeUsage>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Por modo",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            usages.forEach { (mode, count) ->
                Surface(
                    shape = ShapeChip,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = mode.emoji, style = MaterialTheme.typography.bodyMedium)
                        Column {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (count == 1) "1 frase" else "$count frases",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Card de repetição ─────────────────────────────────────────────────────────

/**
 * Card da seção "Falar novamente".
 * Barra de frequência relativa no rodapé indica quanto esse item foi usado
 * em relação ao mais frequente do período.
 */
@Composable
private fun SpeakAgainCard(
    phraseText: String,
    count: Int,
    freqRatio: Float,          // 0f..1f — proporção em relação ao item mais usado
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(168.dp)
            .height(108.dp),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Texto + contagem
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp),
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
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
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
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }

            // Barra de frequência relativa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(freqRatio.coerceIn(0.08f, 1f))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                )
            }
        }
    }
}

// ── Cabeçalho de grupo de dia ─────────────────────────────────────────────────

@Composable
private fun DayGroupHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today              -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else               -> date.dayOfWeek
            .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
            .replaceFirstChar { it.uppercaseChar() } +
                " · " + date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 6.dp),
    )
}

// ── Cabeçalho de slot de horário ──────────────────────────────────────────────

@Composable
private fun TimeSlotHeader(slot: String, count: Int) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 6.dp),
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

// ── Item individual de frase ──────────────────────────────────────────────────

@Composable
private fun PhraseHistoryItem(
    phrase: PhraseHistory,
    onSpeakAgain: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSpeakAgain)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phrase.phraseText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = phrase.createdAt.format(
                        DateTimeFormatter.ofPattern("HH:mm · dd/MM"),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${phrase.appMode.emoji} ${phrase.appMode.label}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
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
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

// ── Helpers de slot de horário ────────────────────────────────────────────────

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
