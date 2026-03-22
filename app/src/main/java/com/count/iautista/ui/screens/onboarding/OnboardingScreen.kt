package com.count.iautista.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(page.emoji, fontSize = 80.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(pages.size) { index ->
                Surface(
                    modifier = Modifier.size(
                        width = if (pagerState.currentPage == index) 24.dp else 8.dp,
                        height = 8.dp,
                    ),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = if (pagerState.currentPage == index)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                ) {}
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onComplete) {
                Text("Pular")
            }
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier.height(48.dp),
            ) {
                Text(
                    if (pagerState.currentPage < pages.size - 1) "Próximo" else "Começar"
                )
            }
        }
    }
}
