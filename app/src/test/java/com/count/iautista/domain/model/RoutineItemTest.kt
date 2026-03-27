package com.count.iautista.domain.model

import org.junit.Assert.*
import org.junit.Test

class RoutineItemTest {

    private fun item(status: RoutineStatus) = RoutineItem(
        id     = 1L,
        text   = "Café da manhã",
        emoji  = "☕",
        status = status,
    )

    @Test
    fun `isNow returns true only for NOW status`() {
        assertTrue(item(RoutineStatus.NOW).isNow)
        assertFalse(item(RoutineStatus.NEXT).isNow)
        assertFalse(item(RoutineStatus.LATER).isNow)
        assertFalse(item(RoutineStatus.DONE).isNow)
    }

    @Test
    fun `isDone returns true only for DONE status`() {
        assertTrue(item(RoutineStatus.DONE).isDone)
        assertFalse(item(RoutineStatus.NOW).isDone)
        assertFalse(item(RoutineStatus.NEXT).isDone)
        assertFalse(item(RoutineStatus.LATER).isDone)
    }

    @Test
    fun `RoutineStatus isActive returns false only for DONE`() {
        assertTrue(RoutineStatus.NOW.isActive)
        assertTrue(RoutineStatus.NEXT.isActive)
        assertTrue(RoutineStatus.LATER.isActive)
        assertFalse(RoutineStatus.DONE.isActive)
    }

    @Test
    fun `default item has LATER status`() {
        val defaultItem = RoutineItem(text = "Test", emoji = "🧪")
        assertEquals(RoutineStatus.LATER, defaultItem.status)
    }

    @Test
    fun `completedAt is null by default`() {
        assertNull(item(RoutineStatus.LATER).completedAt)
    }
}
