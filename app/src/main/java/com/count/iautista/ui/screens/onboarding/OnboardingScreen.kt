package com.count.iautista.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.count.iautista.ui.theme.ShapeButton
import com.count.iautista.ui.theme.ShapeCircle
import com.count.iautista.ui.theme.ShapeEmojiContainer
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
)

private val pages = listOf(
    OnboardingPage(
        emoji = "💬",
        title = "Comunicação fácil",
        description = "Seu filho pode se comunicar tocando em imagens, com voz e texto simples.",
    ),
    OnboardingPage(
        emoji = "📅",
        title = "Rotina visual",
        description = "Organize o dia com cartões visuais claros. Seu filho saberá o que vem a seguir.",
    ),
    OnboardingPage(
        emoji = "❤️",
        title = "Feito para vocês",
        description = "Personalize com fotos, nomes e voz da família. Simples, calmo e confiável.",
    ),
)

// V2: onboarding com mais hierarquia e identidade visual
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 36.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Emoji em container visual consistente
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(ShapeEmojiContainer)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(page.emoji, fontSize = 64.sp)
                }
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp,
                )
            }
        }

        // Indicadores de página
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .clip(ShapeCircle)
                        .size(
                            width = if (isSelected) 28.dp else 8.dp,
                            height = 8.dp,
                        )
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        ),
                )
            }
        }

        // Navegação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onComplete) {
                Text(
                    "Pular",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .height(52.dp)
                    .widthIn(min = 140.dp),
                shape = ShapeButton,
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Próximo" else "Começar",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
