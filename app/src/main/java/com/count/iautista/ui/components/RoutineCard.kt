package com.count.iautista.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.ui.theme.*

// V2: cada status tem tratamento visual próprio e claramente distinto
@Composable
fun RoutineCard(
    item: RoutineItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone  = item.status == RoutineStatus.DONE
    val isNow   = item.status == RoutineStatus.NOW
    val isLater = item.status == RoutineStatus.LATER

    // NOW: azul primário — destaque
    // NEXT: branco/surface — em seguida, normal
    // LATER: surfaceVariant — aguardando, muted
    // DONE: verde suave — concluído
    val bgColor = when {
        isDone  -> ColorSuccessContainer
        isNow   -> MaterialTheme.colorScheme.primaryContainer
        isLater -> MaterialTheme.colorScheme.surfaceVariant
        else    -> MaterialTheme.colorScheme.surface  // NEXT
    }

    // LATER e DONE reduzem alfa para indicar visualmente que não é o foco agora
    val alphaValue = when {
        isDone  -> 0.60f
        isLater -> 0.75f
        else    -> 1f
    }

    val emojiContainerColor = when {
        isNow   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        isDone  -> ColorSuccess.copy(alpha = 0.18f)
        else    -> MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    }

    Card(
        modifier = modifier
            .width(128.dp)
            .height(152.dp)
            .clickable(onClick = onClick)
            .alpha(alphaValue),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isNow  -> 3.dp
                isDone -> 0.dp
                else   -> 1.dp
            }
        ),
        border = when {
            isNow  -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            isDone -> null
            else   -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(ShapeEmojiContainer)
                        .background(emojiContainerColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.emoji,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Badge de concluído — círculo verde com check
            if (isDone) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(ShapeCircle)
                        .background(ColorSuccess),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Concluído",
                        tint = ColorTextOnDark,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
