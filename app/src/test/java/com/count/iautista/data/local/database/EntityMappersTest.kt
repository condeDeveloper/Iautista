package com.count.iautista.data.local.database

import com.count.iautista.data.local.entity.ChildProfileEntity
import com.count.iautista.data.local.entity.RoutineItemEntity
import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.model.RoutineItem
import com.count.iautista.domain.model.RoutineStatus
import org.junit.Assert.*
import org.junit.Test

class EntityMappersTest {

    // ── ChildProfile ──────────────────────────────────────────────────────────

    @Test
    fun `ChildProfileEntity toDomain maps all fields correctly`() {
        val entity = ChildProfileEntity(
            id        = 42L,
            name      = "Maria",
            photoUri  = "file:///photo.jpg",
            createdAt = 1_000_000L,
        )
        val domain = entity.toDomain()
        assertEquals(42L, domain.id)
        assertEquals("Maria", domain.name)
        assertEquals("file:///photo.jpg", domain.photoUri)
        assertEquals(1_000_000L, domain.createdAt)
    }

    @Test
    fun `ChildProfile toEntity maps all fields correctly`() {
        val domain = ChildProfile(
            id        = 7L,
            name      = "João",
            photoUri  = null,
            createdAt = 2_000_000L,
        )
        val entity = domain.toEntity()
        assertEquals(7L, entity.id)
        assertEquals("João", entity.name)
        assertNull(entity.photoUri)
        assertEquals(2_000_000L, entity.createdAt)
    }

    @Test
    fun `ChildProfile roundtrip entity-domain-entity preserves data`() {
        val original = ChildProfileEntity(id = 1L, name = "Ana", photoUri = null, createdAt = 999L)
        val roundtripped = original.toDomain().toEntity()
        assertEquals(original.id, roundtripped.id)
        assertEquals(original.name, roundtripped.name)
        assertEquals(original.photoUri, roundtripped.photoUri)
        assertEquals(original.createdAt, roundtripped.createdAt)
    }

    // ── RoutineItem ───────────────────────────────────────────────────────────

    @Test
    fun `RoutineItemEntity toDomain maps status correctly`() {
        val entity = RoutineItemEntity(
            id            = 1L,
            text          = "Café",
            emoji         = "☕",
            imageUri      = null,
            status        = "NOW",
            order         = 0,
            suggestedHour = 8,
            completedAt   = null,
        )
        val domain = entity.toDomain()
        assertEquals(RoutineStatus.NOW, domain.status)
        assertEquals(8, domain.suggestedHour)
        assertTrue(domain.isNow)
    }

    @Test
    fun `RoutineItemEntity toDomain falls back to LATER on invalid status`() {
        val entity = RoutineItemEntity(
            id            = 2L,
            text          = "Test",
            emoji         = "🧪",
            imageUri      = null,
            status        = "INVALID_VALUE",
            order         = 0,
            suggestedHour = null,
            completedAt   = null,
        )
        assertEquals(RoutineStatus.LATER, entity.toDomain().status)
    }

    @Test
    fun `RoutineItem toEntity stores status as name string`() {
        val domain = RoutineItem(
            id     = 3L,
            text   = "Almoço",
            emoji  = "🍽️",
            status = RoutineStatus.DONE,
        )
        val entity = domain.toEntity()
        assertEquals("DONE", entity.status)
    }

    @Test
    fun `RoutineItem roundtrip preserves all fields`() {
        val original = RoutineItem(
            id            = 5L,
            text          = "Banho",
            emoji         = "🛁",
            imageUri      = "file:///img.jpg",
            status        = RoutineStatus.NEXT,
            order         = 2,
            suggestedHour = 19,
            completedAt   = 12345L,
        )
        val roundtripped = original.toEntity().toDomain()
        assertEquals(original.id, roundtripped.id)
        assertEquals(original.text, roundtripped.text)
        assertEquals(original.emoji, roundtripped.emoji)
        assertEquals(original.imageUri, roundtripped.imageUri)
        assertEquals(original.status, roundtripped.status)
        assertEquals(original.order, roundtripped.order)
        assertEquals(original.suggestedHour, roundtripped.suggestedHour)
        assertEquals(original.completedAt, roundtripped.completedAt)
    }
}
