package com.count.iautista.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.count.iautista.domain.model.CommunicationItem
import com.count.iautista.ui.theme.ColorAccentYellow
import com.count.iautista.ui.theme.ShapeChip

@Composable
fun PhraseBar(
    selectedItems: List<CommunicationItem>,
    onSpeak: () -> Unit,
    onClear: () -> Unit,
    onRemoveItem: (CommunicationItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = selectedItems.isEmpty(),
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150)),
                ) {
                    Text(
                        text = "Toque nos itens para montar uma frase...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                selectedItems.forEach { item ->
                    key(item.id) {
                        AnimatedVisibility(
                            visible = true,
                            enter = expandHorizontally(tween(200)) + fadeIn(tween(200)),
                            exit = shrinkHorizontally(tween(150)) + fadeOut(tween(150)),
                        ) {
                            InputChip(
                                selected = false,
                                onClick = { onRemoveItem(item) },
                                label = { Text(text = "${item.emoji} ${item.text}") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remover",
                                        modifier = Modifier.size(14.dp),
                                    )
                                },
                                shape = ShapeChip,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            AnimatedVisibility(
                visible = selectedItems.isNotEmpty(),
                enter = fadeIn(tween(200)) + expandHorizontally(tween(200)),
                exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150)),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledIconButton(
                        onClick = onClear,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpar")
                    }
                    Button(
                        onClick = onSpeak,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentYellow),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "Falar frase",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Falar", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
