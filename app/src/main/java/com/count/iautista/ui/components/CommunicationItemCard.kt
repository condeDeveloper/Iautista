package com.count.iautista.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.count.iautista.domain.model.ButtonSize
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.ui.theme.ShapeCard
import com.count.iautista.ui.theme.ShapeEmojiContainer

@Composable
fun CommunicationItemCard(
    item: CommunicationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: ButtonSize = ButtonSize.MEDIUM,
    isSelected: Boolean = false,
    isLoading: Boolean = false,
    isPlaying: Boolean = false,
) {
    val cardSize = when (buttonSize) {
        ButtonSize.SMALL  -> 88.dp
        ButtonSize.MEDIUM -> 104.dp
        ButtonSize.LARGE  -> 120.dp
    }
    val emojiContainerSize = when (buttonSize) {
        ButtonSize.SMALL  -> 40.dp
        ButtonSize.MEDIUM -> 50.dp
        ButtonSize.LARGE  -> 60.dp
    }
    val emojiFontSize = when (buttonSize) {
        ButtonSize.SMALL  -> 22.sp
        ButtonSize.MEDIUM -> 28.sp
        ButtonSize.LARGE  -> 36.sp
    }

    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    val emojiContainerColor = when {
        isLoading || isPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .size(cardSize, cardSize + 28.dp)
            .clickable(onClick = onClick),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    .size(emojiContainerSize)
                    .clip(ShapeEmojiContainer)
                    .background(emojiContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                val imageUrl = item.displayImageUri
                val context = LocalContext.current
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(emojiContainerSize * 0.45f),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    isPlaying -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(emojiContainerSize * 0.55f),
                    )
                    imageUrl != null && imageUrl.startsWith("android.resource://") -> {
                        // Recurso local bundlado — painterResource é síncrono,
                        // zero flash de emoji, zero crossfade.
                        val resId = remember(imageUrl) {
                            context.resources.getIdentifier(
                                imageUrl.substringAfterLast("/"),
                                "drawable",
                                context.packageName,
                            )
                        }
                        if (resId != 0) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = item.text,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(emojiContainerSize)
                                    .clip(ShapeEmojiContainer),
                            )
                        } else {
                            Text(
                                text = item.emoji.ifBlank { "📌" },
                                fontSize = emojiFontSize,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    imageUrl != null -> {
                        // URL remota ou foto do usuário — emoji como fallback enquanto carrega
                        Text(
                            text = item.emoji.ifBlank { "📌" },
                            fontSize = emojiFontSize,
                            textAlign = TextAlign.Center,
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(300)
                                .build(),
                            contentDescription = item.text,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(emojiContainerSize)
                                .clip(ShapeEmojiContainer),
                        )
                    }
                    else -> Text(
                        text = item.emoji.ifBlank { "📌" },
                        fontSize = emojiFontSize,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
