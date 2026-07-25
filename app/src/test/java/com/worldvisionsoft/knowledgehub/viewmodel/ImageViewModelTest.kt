package com.worldvisionsoft.knowledgehub.viewmodel

import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import com.worldvisionsoft.knowledgehub.util.FakeImageRepository
import com.worldvisionsoft.knowledgehub.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ImageViewModel].
 *
 * The ViewModel only depends on the [com.worldvisionsoft.knowledgehub.model.remote.ImageRepository]
 * interface, so a [FakeImageRepository] is enough to drive it. [MainDispatcherRule] gives
 * `viewModelScope` a dispatcher with a virtual clock, which is what lets us fast-forward
 * over the 500 ms debounce instead of waiting for it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeImageRepository()

    /** Long enough for the ViewModel's 500 ms debounce to elapse on the virtual clock. */
    private val debounceMillis = 600L

    @Test
    fun `initial state is idle`() = runTest {
        val viewModel = ImageViewModel(repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("", state.error)
        assertNull(state.data)
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `updateQuery loads the images for that query`() = runTest {
        val hits = listOf(Hit("http://img/1.jpg"), Hit("http://img/2.jpg"))
        repository.result = Result.success(hits)
        val viewModel = ImageViewModel(repository)

        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        assertEquals(listOf("cat"), repository.queries)
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("", state.error)
        assertEquals(hits, state.data)
    }

    @Test
    fun `query is not sent until the debounce window has passed`() = runTest {
        val viewModel = ImageViewModel(repository)

        viewModel.updateQuery("cat")
        advanceTimeBy(200)

        // Still inside the 500 ms window — the user may keep typing
        assertTrue(repository.queries.isEmpty())

        advanceTimeBy(debounceMillis)
        advanceUntilIdle()
        assertEquals(listOf("cat"), repository.queries)
    }
}
