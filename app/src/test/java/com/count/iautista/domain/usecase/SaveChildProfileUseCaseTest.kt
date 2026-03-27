package com.count.iautista.domain.usecase

import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.domain.usecase.responsavel.SaveChildProfileUseCase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SaveChildProfileUseCaseTest {

    private lateinit var repository: ProfileRepository
    private lateinit var useCase: SaveChildProfileUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveChildProfileUseCase(repository)
    }

    @Test
    fun `when profile id is 0 calls saveProfile and returns new id`() = runTest {
        val newProfile = ChildProfile(id = 0L, name = "Maria")
        coEvery { repository.saveProfile(newProfile) } returns 42L

        val result = useCase(newProfile)

        assertEquals(42L, result)
        coVerify(exactly = 1) { repository.saveProfile(newProfile) }
        coVerify(exactly = 0) { repository.updateProfile(any()) }
    }

    @Test
    fun `when profile id is non-zero calls updateProfile and returns same id`() = runTest {
        val existingProfile = ChildProfile(id = 7L, name = "João")
        coEvery { repository.updateProfile(existingProfile) } just Runs

        val result = useCase(existingProfile)

        assertEquals(7L, result)
        coVerify(exactly = 1) { repository.updateProfile(existingProfile) }
        coVerify(exactly = 0) { repository.saveProfile(any()) }
    }
}
