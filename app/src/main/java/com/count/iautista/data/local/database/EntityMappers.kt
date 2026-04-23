package com.count.iautista.data.local.database

import com.count.iautista.data.local.entity.*
import com.count.iautista.domain.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// ── CommunicationCategory ────────────────────────────────────────────────────

fun CommunicationCategoryEntity.toDomain() = CommunicationCategory(
    id = id,
    name = name,
    emoji = emoji,
    backgroundColor = backgroundColor,
    order = order,
    isDefault = isDefault,
)

fun CommunicationCategory.toEntity() = CommunicationCategoryEntity(
    id = id,
    name = name,
    emoji = emoji,
    backgroundColor = backgroundColor,
    order = order,
    isDefault = isDefault,
)

// ── CommunicationItem ────────────────────────────────────────────────────────

fun CommunicationItemEntity.toDomain() = CommunicationItem(
    id = id,
    categoryId = categoryId,
    text = text,
    emoji = emoji,
    imageRes = imageRes,
    imageUri = imageUri,
    audioUri = audioUri,
    isFavorite = isFavorite,
    isDefault = isDefault,
    order = order,
    usageCount = usageCount,
    createdAt = createdAt,
)

fun CommunicationItem.toEntity() = CommunicationItemEntity(
    id = id,
    categoryId = categoryId,
    text = text,
    emoji = emoji,
    imageRes = imageRes,
    imageUri = imageUri,
    audioUri = audioUri,
    isFavorite = isFavorite,
    isDefault = isDefault,
    order = order,
    usageCount = usageCount,
    createdAt = createdAt,
)

// ── RoutineItem ──────────────────────────────────────────────────────────────

fun RoutineItemEntity.toDomain() = RoutineItem(
    id = id,
    profileId = profileId,
    text = text,
    emoji = emoji,
    imageUri = imageUri,
    status = runCatching { RoutineStatus.valueOf(status) }.getOrDefault(RoutineStatus.LATER),
    order = order,
    suggestedHour = suggestedHour,
    completedAt = completedAt,
)

fun RoutineItem.toEntity() = RoutineItemEntity(
    id = id,
    profileId = profileId,
    text = text,
    emoji = emoji,
    imageUri = imageUri,
    status = status.name,
    order = order,
    suggestedHour = suggestedHour,
    completedAt = completedAt,
)

// ── PhraseHistory ────────────────────────────────────────────────────────────

fun PhraseHistoryEntity.toDomain() = PhraseHistory(
    id = id,
    profileId = profileId,
    phraseText = phraseText,
    itemIds = itemIds,
    createdAt = Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime(),
    hourOfDay = hourOfDay,
    appMode = runCatching { AppMode.valueOf(appMode) }.getOrDefault(AppMode.CASA),
)

fun PhraseHistory.toEntity() = PhraseHistoryEntity(
    id = id,
    profileId = profileId,
    phraseText = phraseText,
    itemIds = itemIds,
    createdAt = createdAt
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),
    hourOfDay = hourOfDay,
    appMode = appMode.name,
)

// ── ChildProfile ─────────────────────────────────────────────────────────────

fun ChildProfileEntity.toDomain() = ChildProfile(
    id = id,
    name = name,
    photoUri = photoUri,
    avatarId = avatarId,
    createdAt = createdAt,
)

fun ChildProfile.toEntity() = ChildProfileEntity(
    id = id,
    name = name,
    photoUri = photoUri,
    avatarId = avatarId,
    createdAt = createdAt,
)
