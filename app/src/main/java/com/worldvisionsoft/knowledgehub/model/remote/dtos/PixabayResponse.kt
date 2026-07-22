package com.worldvisionsoft.knowledgehub.model.remote.dtos

data class PixabayResponse(
    val hits: List<Hit>,
    val total: Int,
    val totalHits: Int
)