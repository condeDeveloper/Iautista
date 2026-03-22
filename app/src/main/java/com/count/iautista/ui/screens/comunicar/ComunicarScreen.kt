package com.count.iautista.ui.screens.comunicar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Text(
            text = "O que você quer dizer?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )

        // Favoritos — só aparece quando existem itens marcados
        if (state.favorites.isNotEmpty()) {
            SectionHeader(title = "⭐ Favoritos")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        // Grid de categorias
        if (state.categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) },
                    )
                }
            }
        }
    }
}
