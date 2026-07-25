package com.worldvisionsoft.knowledgehub.model.remote

import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import com.worldvisionsoft.knowledgehub.model.remote.dtos.PixabayResponse
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test for [ImageRepositoryImpl].
 *
 * The whole point of the interface + constructor-injection refactor: we can pass a
 * FAKE [ApiService] into the repository, so this test never touches the network.
 * It runs on the JVM in milliseconds — no emulator, no Retrofit, no Hilt.
 */
class ImageRepositoryImplTest {

    /**
     * A hand-written fake standing in for the real Retrofit-generated [ApiService].
     * It either returns the [response] we hand it or throws [error] — no HTTP involved.
     * It also records the arguments it was called with, so a test can assert on them.
     */
    private class FakeApiService(
        private val response: PixabayResponse = PixabayResponse(emptyList(), 0, 0),
        private val error: Exception? = null
    ) : ApiService {

        var callCount = 0
            private set
        var lastQuery: String? = null
            private set
        var lastApiKey: String? = null
            private set

        override suspend fun getImages(apiKey: String, query: String): PixabayResponse {
            callCount++
            lastApiKey = apiKey
            lastQuery = query
            error?.let { throw it }
            return response
        }
    }

    @Test
    fun `getImages returns success with the hits from the api`() = runTest {
        // Given — a fake api that will return two hits
        val hits = listOf(Hit("http://img/1.jpg"), Hit("http://img/2.jpg"))
        val fakeApi = FakeApiService(PixabayResponse(hits = hits, total = 2, totalHits = 2))
        val repository = ImageRepositoryImpl(fakeApi)

        // When — we ask the repository for images
        val result = repository.getImages("cat")

        // Then — it wraps the api's hits in a successful Result
        assertTrue(result.isSuccess)
        assertEquals(hits, result.getOrNull())
    }

    @Test
    fun `getImages returns success with an empty list when the api has no hits`() = runTest {
        // Given — the api answers with zero hits (a query nobody has photographed)
        val fakeApi = FakeApiService(PixabayResponse(hits = emptyList(), total = 0, totalHits = 0))
        val repository = ImageRepositoryImpl(fakeApi)

        // When
        val result = repository.getImages("qwertyuiop")

        // Then — an empty result is still a SUCCESS, not a failure
        assertTrue(result.isSuccess)
        assertEquals(emptyList<Hit>(), result.getOrNull())
    }

    @Test
    fun `getImages returns failure when the api throws`() = runTest {
        // Given — the api blows up the way it would with no connectivity
        val boom = IOException("no internet")
        val repository = ImageRepositoryImpl(FakeApiService(error = boom))

        // When
        val result = repository.getImages("cat")

        // Then — the exception is captured in the Result instead of escaping
        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertEquals(boom, result.exceptionOrNull())
    }

    @Test
    fun `getImages forwards the query to the api exactly once`() = runTest {
        // Given
        val fakeApi = FakeApiService()
        val repository = ImageRepositoryImpl(fakeApi)

        // When
        repository.getImages("sunset over the sea")

        // Then — the repository does not mangle, trim or re-encode the query
        assertEquals(1, fakeApi.callCount)
        assertEquals("sunset over the sea", fakeApi.lastQuery)
    }

    @Test
    fun `getImages lets the api supply the default api key`() = runTest {
        // Given — the repository never passes a key itself, it relies on the
        // default argument declared on ApiService (wired to BuildConfig)
        val fakeApi = FakeApiService()
        val repository = ImageRepositoryImpl(fakeApi)

        // When
        repository.getImages("cat")

        // Then — whatever the default is, the repository did not override it with something odd
        assertEquals(com.worldvisionsoft.knowledgehub.BuildConfig.PIXABAY_API_KEY, fakeApi.lastApiKey)
    }
}
