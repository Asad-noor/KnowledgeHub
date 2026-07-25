package com.worldvisionsoft.knowledgehub.viewmodel

import com.worldvisionsoft.knowledgehub.model.remote.ImageRepository
import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import com.worldvisionsoft.knowledgehub.util.FakeImageRepository
import com.worldvisionsoft.knowledgehub.util.MainDispatcherRule
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
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

    @Test
    fun `only the last query typed is sent while the user is still typing`() = runTest {
        val viewModel = ImageViewModel(repository)

        // Someone typing "cat" one letter at a time, faster than the debounce
        viewModel.updateQuery("c")
        advanceTimeBy(100)
        viewModel.updateQuery("ca")
        advanceTimeBy(100)
        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        // Only one network round-trip, for the final text
        assertEquals(listOf("cat"), repository.queries)
    }

    @Test
    fun `an empty query is ignored`() = runTest {
        val viewModel = ImageViewModel(repository)

        // e.g. the user clearing the search field
        viewModel.updateQuery("")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        assertTrue(repository.queries.isEmpty())
        assertNull(viewModel.uiState.value.data)
    }

    @Test
    fun `re-submitting the same query does not hit the repository twice`() = runTest {
        val viewModel = ImageViewModel(repository)

        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()
        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        // distinctUntilChanged drops the duplicate
        assertEquals(listOf("cat"), repository.queries)
    }

    @Test
    fun `isLoading is true while the request is in flight`() = runTest {
        // A repository that stays suspended until we let it finish
        val gate = CompletableDeferred<Unit>()
        val slowRepository = object : ImageRepository {
            override suspend fun getImages(query: String): Result<List<Hit>> {
                gate.await()
                return Result.success(emptyList())
            }
        }
        val viewModel = ImageViewModel(slowRepository)

        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoading)

        gate.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `a slow response cannot overwrite the result of a newer query`() = runTest {
        val slowHits = listOf(Hit("http://img/cat.jpg"))
        val fastHits = listOf(Hit("http://img/dog.jpg"))
        val firstCallGate = CompletableDeferred<Unit>()
        val slowFirstRepository = object : ImageRepository {
            var calls = 0
            override suspend fun getImages(query: String): Result<List<Hit>> {
                // Read the counter BEFORE suspending — it will have moved on by the
                // time the first (slow) call resumes.
                val isFirstCall = ++calls == 1
                if (isFirstCall) firstCallGate.await()
                return Result.success(if (isFirstCall) slowHits else fastHits)
            }
        }
        val viewModel = ImageViewModel(slowFirstRepository)

        // First search hangs on a slow connection
        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        // The user gives up and searches for something else, which answers immediately
        viewModel.updateQuery("dog")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()
        assertEquals(fastHits, viewModel.uiState.value.data)

        // The first request finally comes back — it must not clobber the newer result
        firstCallGate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(fastHits, state.data)
        assertFalse(state.isLoading)
    }

    @Test
    fun `repository failure is surfaced as an error message`() = runTest {
        repository.result = Result.failure(IOException("no internet"))
        val viewModel = ImageViewModel(repository)

        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("no internet", state.error)
        assertNull(state.data)
    }

    @Test
    fun `a successful search clears a previous error`() = runTest {
        repository.result = Result.failure(IOException("no internet"))
        val viewModel = ImageViewModel(repository)

        viewModel.updateQuery("cat")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()
        assertEquals("no internet", viewModel.uiState.value.error)

        // Connectivity is back and the user searches for something else
        val hits = listOf(Hit("http://img/1.jpg"))
        repository.result = Result.success(hits)
        viewModel.updateQuery("dog")
        advanceTimeBy(debounceMillis)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.error)
        assertEquals(hits, state.data)
    }
}
