package com.worldvisionsoft.knowledgehub.model.remote

import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import com.worldvisionsoft.knowledgehub.model.remote.dtos.PixabayResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
     * It just returns whatever [PixabayResponse] we hand it — no HTTP involved.
     */
    private class FakeApiService(
        private val response: PixabayResponse
    ) : ApiService {
        override suspend fun getImages(apiKey: String, query: String): PixabayResponse {
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
}
