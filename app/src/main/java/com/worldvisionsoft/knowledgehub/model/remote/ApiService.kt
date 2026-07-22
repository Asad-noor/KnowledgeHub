package com.worldvisionsoft.knowledgehub.model.remote

import com.worldvisionsoft.knowledgehub.BuildConfig
import com.worldvisionsoft.knowledgehub.model.remote.dtos.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/")
    suspend fun getImages(
        @Query("key") apiKey: String = BuildConfig.PIXABAY_API_KEY,
        @Query("q") query: String
    ): PixabayResponse

}