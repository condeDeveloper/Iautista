package com.count.iautista.ui.screens.comunicar

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
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.ui.components.CategoryCard
import com.count.iautista.ui.components.CommunicationItemCard
import com.count.iautista.ui.components.SectionHeader

@Composable
fun ComunicarScreen(
    onCategoryClick: (Long) -> Unit,
    viewModel: ComunicarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // Título da tela — alinhado com estilo da Home
        Text(
            text = "O que você quer dizer?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )

        // Favoritos — só aparece quando existem itens marcados
        if (state.favorites.isNotEmpty()) {
            SectionHeader(
                title = "Favoritos",
                leadingIcon = Icons.Filled.Star,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.favorites) { item ->
                    CommunicationItemCard(
                        item = item,
                        onClick = {
                            viewModel.addItemToPhrase(item)
                            viewModel.speakItem(item)
                        },
                        buttonSize = ButtonSize.SMALL,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        // Grade de categorias
        // V2: 2 colunas — 10 categorias = 5 linhas perfeitas, sem item isolado,
        // cards maiores e mais legíveis para crianças
        if (state.categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 20.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = state.categories,
                    key = { it.id },
                ) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) },
                    )
                }
            }
        }
    }
}
