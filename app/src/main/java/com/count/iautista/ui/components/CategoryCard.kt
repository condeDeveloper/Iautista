package com.count.iautista.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.count.iautista.domain.model.CommunicationCategory
import com.count.iautista.ui.theme.ShapeCard
import com.count.iautista.ui.theme.ShapeEmojiContainer

// V2: card otimizado para 2 colunas — emoji maior, texto mais legível
@Composable
fun CategoryCard(
    category: CommunicationCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(category.backgroundColor)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Container do emoji com fundo branco suave — visual uniforme entre todos os cards
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(ShapeEmojiContainer)
                    .background(Color.White.copy(alpha = 0.62f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 34.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
