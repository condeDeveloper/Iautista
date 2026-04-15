package com.count.iautista.ui.screens.responsavel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.avatarBgColors
import com.count.iautista.domain.model.childAvatars
import com.count.iautista.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarPerfisScreen(
    onBack: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    viewModel: GerenciarPerfisViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingProfile by viewModel.editingProfile.collectAsState()
    val premiumRequired by viewModel.premiumRequired.collectAsState()

    if (premiumRequired) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPremiumRequired() },
            title = { Text("Recurso Premium") },
            text  = { Text("Múltiplos perfis de criança estão disponíveis no plano Premium.") },
            confirmButton = {
                Button(onClick = { viewModel.dismissPremiumRequired(); onNavigateToPremium() }) {
                    Text("Ver Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPremiumRequired() }) { Text("Agora não") }
            },
            shape = ShapeCard,
        )
    }

    if (showAddDialog) {
        ProfileNameDialog(
            title     = "Novo perfil",
            initial   = "",
            initialAvatar = null,
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { name, avatarId -> viewModel.addProfile(name, avatarId) },
        )
    }

    editingProfile?.let { profile ->
        ProfileNameDialog(
            title     = "Editar perfil",
            initial   = profile.name,
            initialAvatar = profile.avatarId,
            onDismiss = { viewModel.dismissDialog() },
            onConfirm = { name, avatarId -> viewModel.updateProfile(profile, name, avatarId) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfis",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick  = { viewModel.showAddDialog() },
                icon     = { Icon(Icons.Filled.Add, contentDescription = null) },
                text     = { Text("Novo perfil") },
            )
        },
    ) { padding ->
        if (state.profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("👦", style = MaterialTheme.typography.displayMedium)
                    Text(
                        text = "Nenhum perfil ainda.",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                    Text(
                        text = "Adicione um perfil para cada criança.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 96.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.profiles, key = { it.id }) { profile ->
                ProfileCard(
                    profile   = profile,
                    isActive  = profile.id == state.activeProfileId,
                    onSelect  = { viewModel.selectProfile(profile) },
                    onEdit    = { viewModel.editProfile(profile) },
                    onDelete  = {
                        if (state.profiles.size > 1) viewModel.deleteProfile(profile)
                    },
                    canDelete = state.profiles.size > 1,
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: ChildProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remover perfil?") },
            text  = { Text("\"${profile.name}\" será removido permanentemente.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            },
            shape = ShapeCard,
        )
    }

    Card(
        onClick = onSelect,
        shape   = ShapeCard,
        colors  = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface,
        ),
        border = if (isActive)
            null
        else
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AvatarCircle(
                avatarId = profile.avatarId,
                name     = profile.name,
                size     = 48,
                isActive = isActive,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = profile.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = if (isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                )
                if (isActive) {
                    Text(
                        text  = "Perfil ativo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Ativo",
                    tint    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (canDelete) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remover",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileNameDialog(
    title: String,
    initial: String,
    initialAvatar: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    var nameError by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(initialAvatar) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it; nameError = false },
                    label         = { Text("Nome") },
                    placeholder   = { Text("Ex: Maria") },
                    isError       = nameError,
                    supportingText = if (nameError) { { Text("Campo obrigatório") } } else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = ShapeCard,
                )

                Text(
                    text  = "Avatar",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(childAvatars) { avatar ->
                        val isSelected = selectedAvatar == avatar.id
                        val bgColor = Color(avatarBgColors[avatar.colorIndex])
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(ShapeCircle)
                                .background(bgColor)
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        ShapeCircle,
                                    ) else Modifier
                                )
                                .clickable { selectedAvatar = avatar.id },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = avatar.emoji, fontSize = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    if (!nameError) onConfirm(name, selectedAvatar)
                },
                shape = ShapeButton,
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = ShapeCard,
    )
}

@Composable
private fun AvatarCircle(
    avatarId: String?,
    name: String,
    size: Int,
    isActive: Boolean,
) {
    val avatar = childAvatars.find { it.id == avatarId }
    val bgColor = if (avatar != null)
        Color(avatarBgColors[avatar.colorIndex])
    else if (isActive)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(ShapeCircle)
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (avatar != null) {
            Text(text = avatar.emoji, fontSize = (size * 0.46f).sp)
        } else {
            Text(
                text  = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
