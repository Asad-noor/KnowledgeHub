package com.worldvisionsoft.knowledgehub.model.remote

import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit

interface ImageRepository {
    suspend fun getImages(query: String): Result<List<Hit>>
}
