package com.worldvisionsoft.knowledgehub.model.remote

import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ImageRepository {

    override suspend fun getImages(query: String): Result<List<Hit>> {
        return try {
            val response = apiService.getImages(query = query)
            Result.success(response.hits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
