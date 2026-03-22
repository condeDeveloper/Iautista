package com.count.iautista.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import com.count.iautista.ui.theme.*

@Composable
fun RoutineCard(
    item: RoutineItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = item.status == RoutineStatus.DONE
    val isNow = item.status == RoutineStatus.NOW

    val bgColor = when {
        isDone -> ColorSuccessContainer
        isNow -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val border = when {
        isNow -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> null
    }

    Card(
        modifier = modifier
            .width(120.dp)
            .height(140.dp)
            .clip(ShapeCard)
            .clickable(onClick = onClick)
            .alpha(if (isDone) 0.6f else 1f),
        shape = ShapeCard,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNow) 4.dp else 2.dp),
        border = border,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.emoji,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            if (isDone) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Concluído",
                    tint = ColorSuccess,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp),
                )
            }
        }
    }
}
