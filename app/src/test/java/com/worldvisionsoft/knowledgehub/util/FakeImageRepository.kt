package com.worldvisionsoft.knowledgehub.util

import com.worldvisionsoft.knowledgehub.model.remote.ImageRepository
import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit

/**
 * In-memory stand-in for the real repository, used by the ViewModel tests.
 *
 * It records every query it is asked for so a test can assert how many network
 * round-trips the ViewModel would have made (debounce, distinctUntilChanged...).
 */
class FakeImageRepository(
    var result: Result<List<Hit>> = Result.success(emptyList())
) : ImageRepository {

    val queries = mutableListOf<String>()

    override suspend fun getImages(query: String): Result<List<Hit>> {
        queries.add(query)
        return result
    }
}
