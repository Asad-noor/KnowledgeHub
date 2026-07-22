package com.worldvisionsoft.knowledgehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldvisionsoft.knowledgehub.model.remote.ImageRepository
import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val imageRepository: ImageRepository by lazy {
        ImageRepository()
    }

    private val _query = MutableStateFlow("")

    fun updateQuery(query: String) {
        _query.update { query }
    }

    init {
        viewModelScope.launch {
            _query.filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .debounce { 500 }
                .collectLatest { query -> getImages(query) }
        }
    }

    private fun getImages(query: String) = viewModelScope.launch {
        val result = imageRepository.getImages(query)

        if (result.isSuccess) {
            _uiState.update { UiState(data = result.getOrThrow()) }
        } else {
            _uiState.update { UiState(error = result.exceptionOrNull()?.message.toString()) }
        }
    }

}

data class UiState(
    val isLoading: Boolean= false,
    val error: String ="",
    val data: List<Hit>? = null
)