package com.count.iautista.ui.screens.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.ui.components.CommunicationItemCard
import com.count.iautista.ui.components.QuickActionButton
import com.count.iautista.ui.components.RoutineCard
import com.count.iautista.ui.components.SectionHeader

@Composable
fun InicioScreen(
    navController: NavController,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Saudação
        item {
            Text(
                text = state.greeting,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            )
        }

        // Necessidades rápidas
        item { SectionHeader(title = "Necessidades rápidas") }
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "🚽" to "Banheiro",
                    "💧" to "Água",
                    "😴" to "Cansado",
                    "🍽️" to "Comida",
                    "🆘" to "Ajuda",
                ).forEach { (emoji, label) ->
                    QuickActionButton(
                        emoji = emoji,
                        label = label,
                        onClick = { viewModel.speakPhrase(label) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Emoções rápidas
        item {
            SectionHeader(title = "Como estou", modifier = Modifier.padding(top = 8.dp))
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    listOf(
                        "😊" to "Feliz", "😢" to "Triste", "😠" to "Bravo",
                        "😨" to "Assustado", "🤕" to "Dói", "😴" to "Cansado",
                    )
                ) { (emoji, label) ->
                    ElevatedCard(
                        onClick = { viewModel.speakPhrase(label) },
                        modifier = Modifier.size(80.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(emoji, style = MaterialTheme.typography.titleLarge)
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Rotina: o que vem agora
        if (state.routineNow.isNotEmpty()) {
            item {
                SectionHeader(title = "⭐ Agora", modifier = Modifier.padding(top = 8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.routineNow) { item ->
                        RoutineCard(item = item, onClick = { viewModel.speakPhrase(item.text) })
                    }
                }
            }
        }

        // Mais usadas
        if (state.mostUsedItems.isNotEmpty()) {
            item {
                SectionHeader(title = "Mais usadas", modifier = Modifier.padding(top = 8.dp))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

        // Falar novamente
        if (state.recentPhrases.isNotEmpty()) {
            item {
                SectionHeader(title = "Falar novamente", modifier = Modifier.padding(top = 8.dp))
            }
            items(state.recentPhrases.take(5)) { phrase ->
                OutlinedCard(
                    onClick = { viewModel.speakPhrase(phrase.phraseText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = phrase.phraseText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
