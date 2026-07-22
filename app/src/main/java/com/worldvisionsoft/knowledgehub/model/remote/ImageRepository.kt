package com.worldvisionsoft.knowledgehub.model.remote

import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit

class ImageRepository {

    private val apiService by lazy { RetrofitInstance.getApiService() }

    suspend fun getImages(query: String): Result<List<Hit>> {
        return try {
            val response = apiService.getImages(query = query)
            Result.success(response.hits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}