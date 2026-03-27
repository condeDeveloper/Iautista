package com.count.iautista.domain.usecase

import com.count.iautista.domain.model.ChildProfile
import com.count.iautista.domain.repository.ProfileRepository
import com.count.iautista.ui.screens.responsavel.GerenciarPerfisViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GerenciarPerfisViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ProfileRepository
    private lateinit var viewModel: GerenciarPerfisViewModel

    private val profile1 = ChildProfile(id = 1L, name = "Ana")
    private val profile2 = ChildProfile(id = 2L, name = "Pedro")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.getAllProfiles() } returns flowOf(listOf(profile1, profile2))
        every { repository.getActiveProfileId() } returns flowOf(1L)
        viewModel = GerenciarPerfisViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has profiles and active id from repository`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.profiles.size)
        assertEquals(1L, state.activeProfileId)
    }

    @Test
    fun `showAddDialog sets showAddDialog state to true`() {
        viewModel.showAddDialog()
        assertTrue(viewModel.showAddDialog.value)
    }

    @Test
    fun `dismissDialog resets showAddDialog and editingProfile`() {
        viewModel.showAddDialog()
        viewModel.dismissDialog()
        assertFalse(viewModel.showAddDialog.value)
        assertNull(viewModel.editingProfile.value)
    }

    @Test
    fun `selectProfile calls repository setActiveProfileId`() = runTest {
        coEvery { repository.setActiveProfileId(2L) } just Runs

        viewModel.selectProfile(profile2)
        advanceUntilIdle()

        coVerify { repository.setActiveProfileId(2L) }
    }

    @Test
    fun `deleteProfile removes profile and switches active if deleted was active`() = runTest {
        coEvery { repository.deleteProfile(profile1) } just Runs
        coEvery { repository.setActiveProfileId(any()) } just Runs

        viewModel.deleteProfile(profile1)
        advanceUntilIdle()

        coVerify { repository.deleteProfile(profile1) }
        // Should switch to first remaining profile
        coVerify { repository.setActiveProfileId(profile2.id) }
    }

    @Test
    fun `addProfile with blank name does nothing`() = runTest {
        viewModel.addProfile("   ")
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveProfile(any()) }
    }

    @Test
    fun `addProfile with valid name calls repository saveProfile`() = runTest {
        coEvery { repository.saveProfile(any()) } returns 3L

        viewModel.addProfile("Carlos")
        advanceUntilIdle()

        coVerify { repository.saveProfile(match { it.name == "Carlos" }) }
    }
}
