package com.count.iautista.ui.screens.historico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.PhraseHistory
import java.time.format.DateTimeFormatter

@Composable
fun HistoricoScreen(
    viewModel: HistoricoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val tabs = listOf("Recentes", "Hoje", "Esta semana")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        )

        TabRow(selectedTabIndex = state.selectedTab.ordinal) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab.ordinal == index,
                    onClick = { viewModel.selectTab(HistoricoTab.values()[index]) },
                    text = { Text(title) },
                )
            }
        }

        if (state.phrases.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhuma frase ainda.\nComece a se comunicar!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.phrases, key = { it.id }) { phrase ->
                    PhraseHistoryItem(
                        phrase = phrase,
                        onSpeakAgain = { viewModel.speakAgain(phrase) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PhraseHistoryItem(
    phrase: PhraseHistory,
    onSpeakAgain: () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phrase.phraseText,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phrase.createdAt.format(DateTimeFormatter.ofPattern("HH:mm · dd/MM")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onSpeakAgain) {
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Falar novamente",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
