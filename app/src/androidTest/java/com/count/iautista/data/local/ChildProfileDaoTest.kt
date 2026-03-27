package com.count.iautista.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.count.iautista.data.local.dao.ChildProfileDao
import com.count.iautista.data.local.database.IautistaDatabase
import com.count.iautista.data.local.entity.ChildProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChildProfileDaoTest {

    private lateinit var database: IautistaDatabase
    private lateinit var dao: ChildProfileDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IautistaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.childProfileDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAllProfiles() = runTest {
        val profile1 = ChildProfileEntity(name = "Ana", createdAt = 1000L)
        val profile2 = ChildProfileEntity(name = "Pedro", createdAt = 2000L)

        dao.insert(profile1)
        dao.insert(profile2)

        val all = dao.getAllProfiles().first()
        assertEquals(2, all.size)
        assertTrue(all.any { it.name == "Ana" })
        assertTrue(all.any { it.name == "Pedro" })
    }

    @Test
    fun deleteProfileRemovesIt() = runTest {
        val id = dao.insert(ChildProfileEntity(name = "Maria", createdAt = 1000L))
        val entity = dao.getById(id) ?: error("Entity not found")

        dao.delete(entity)

        val all = dao.getAllProfiles().first()
        assertTrue(all.none { it.name == "Maria" })
    }

    @Test
    fun getByIdReturnsCorrectProfile() = runTest {
        val id = dao.insert(ChildProfileEntity(name = "Lucas", createdAt = 5000L))
        val result = dao.getById(id)

        assertNotNull(result)
        assertEquals("Lucas", result!!.name)
        assertEquals(id, result.id)
    }

    @Test
    fun updateProfileChangesName() = runTest {
        val id = dao.insert(ChildProfileEntity(name = "Old Name", createdAt = 1000L))
        val existing = dao.getById(id)!!

        dao.update(existing.copy(name = "New Name"))

        val updated = dao.getById(id)
        assertEquals("New Name", updated?.name)
    }

    @Test
    fun profilesAreOrderedByCreatedAt() = runTest {
        dao.insert(ChildProfileEntity(name = "B", createdAt = 3000L))
        dao.insert(ChildProfileEntity(name = "A", createdAt = 1000L))
        dao.insert(ChildProfileEntity(name = "C", createdAt = 2000L))

        val all = dao.getAllProfiles().first()
        assertEquals("A", all[0].name)
        assertEquals("C", all[1].name)
        assertEquals("B", all[2].name)
    }
}
