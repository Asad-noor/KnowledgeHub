package com.worldvisionsoft.knowledgehub.model.remote.dtos

data class Hit(
    val id: Int,
    val largeImageURL: String,
    val tags: String,
    val user: String
)