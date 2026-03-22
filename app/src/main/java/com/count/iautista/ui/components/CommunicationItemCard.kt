package com.count.iautista.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.ui.theme.ShapeCard

@Composable
fun CommunicationItemCard(
    item: CommunicationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: ButtonSize = ButtonSize.MEDIUM,
    isSelected: Boolean = false,
) {
    val cardSize = when (buttonSize) {
        ButtonSize.SMALL -> 88.dp
        ButtonSize.MEDIUM -> 104.dp
        ButtonSize.LARGE -> 120.dp
    }
    val emojiSize = when (buttonSize) {
        ButtonSize.SMALL -> 32.sp
        ButtonSize.MEDIUM -> 40.sp
        ButtonSize.LARGE -> 48.sp
    }

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .size(cardSize, cardSize + 24.dp)
            .clip(ShapeCard)
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) BorderStroke(2.dp, borderColor) else null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.emoji.ifBlank { "📌" },
                fontSize = emojiSize,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
