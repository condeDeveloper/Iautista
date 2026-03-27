package com.count.iautista.ui.screens.responsavel

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AdicionarItemScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    viewModel: AdicionarItemViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Navegação após salvar
    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onBack()
    }

    // Permissões
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermission  = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Launchers
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> viewModel.onPhotoCaptured(success) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> viewModel.onPhotoSelected(uri) }

    if (state.premiumLimitReached) {
        AlertDialog(
            onDismissRequest = { viewModel.clearPremiumAlert() },
            title = { Text("Limite atingido") },
            text = { Text("Você atingiu o limite de ${10} itens personalizados no plano gratuito. Assine o Premium para itens ilimitados.") },
            confirmButton = {
                Button(onClick = { viewModel.clearPremiumAlert(); onNavigateToPremium() }) {
                    Text("Ver Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearPremiumAlert() }) { Text("Agora não") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = !state.isSaving,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Salvar")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Nome e Emoji ──────────────────────────────────────────────────
            OutlinedTextField(
                value = state.text,
                onValueChange = { viewModel.updateText(it) },
                label = { Text("Nome do item") },
                singleLine = true,
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.emoji,
                onValueChange = { viewModel.updateEmoji(it) },
                label = { Text("Emoji (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Categoria ─────────────────────────────────────────────────────
            if (state.categories.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val selected = state.categories.find { it.id == state.selectedCategoryId }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = "${selected?.emoji ?: ""} ${selected?.name ?: ""}".trim(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        state.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.emoji} ${cat.name}") },
                                onClick = {
                                    viewModel.selectCategory(cat.id)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // ── Foto ──────────────────────────────────────────────────────────
            Text("Imagem", style = MaterialTheme.typography.titleSmall)

            if (state.imageUri != null) {
                Box(modifier = Modifier.size(120.dp)) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Imagem do item",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                    IconButton(
                        onClick = { viewModel.clearPhoto() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remover foto",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (cameraPermission.status.isGranted) {
                                val uri = viewModel.createPhotoUri()
                                takePictureLauncher.launch(uri)
                            } else {
                                cameraPermission.launchPermissionRequest()
                            }
                        },
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Câmera")
                    }
                    OutlinedButton(
                        onClick = { pickImageLauncher.launch("image/*") },
                    ) {
                        Icon(Icons.Filled.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galeria")
                    }
                }
            }

            HorizontalDivider()

            // ── Áudio ─────────────────────────────────────────────────────────
            Text("Áudio personalizado", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Grave a voz do responsável como alternativa ao TTS",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.audioUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledIconButton(
                        onClick = {
                            if (state.isPlaying) viewModel.stopAudio() else viewModel.playAudio()
                        },
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "Parar" else "Reproduzir",
                        )
                    }
                    Text(
                        text = if (state.isPlaying) "Reproduzindo..." else "Áudio gravado",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { viewModel.clearAudio() }) {
                        Text("Remover", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        if (audioPermission.status.isGranted) {
                            if (state.isRecording) viewModel.stopRecording()
                            else viewModel.startRecording()
                        } else {
                            audioPermission.launchPermissionRequest()
                        }
                    },
                    colors = if (state.isRecording)
                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    else
                        ButtonDefaults.outlinedButtonColors(),
                ) {
                    Icon(
                        imageVector = if (state.isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (state.isRecording) "Parar gravação" else "Gravar áudio")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
