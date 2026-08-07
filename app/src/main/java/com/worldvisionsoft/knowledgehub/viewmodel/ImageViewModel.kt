package com.worldvisionsoft.knowledgehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldvisionsoft.knowledgehub.model.remote.ImageRepository
import com.worldvisionsoft.knowledgehub.model.remote.dtos.Hit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")

    fun updateQuery(query: String) {
        _query.update { query }
    }

    fun selectHit(hit: Hit?) {
        _uiState.update { it.copy(selectedHit = hit) }
    }

    init {
        viewModelScope.launch {
            _query.filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .debounce { 500 }
                .collectLatest { query -> getImages(query) }
        }
    }

    /**
     * Suspends instead of launching its own coroutine on purpose: it runs inside the
     * [collectLatest] above, so a newer query cancels the in-flight request and its
     * (now stale) response can never overwrite the newer one in [_uiState].
     */
    private suspend fun getImages(query: String) {
        _uiState.update { it.copy(isLoading = true, error = "") }

        val result = imageRepository.getImages(query)

        if (result.isSuccess) {
            _uiState.update {
                it.copy(isLoading = false, data = result.getOrThrow(), error = "")
            }
        } else {
            _uiState.update {
                it.copy(isLoading = false, error = result.exceptionOrNull()?.message.toString())
            }
        }
    }

}

data class UiState(
    val isLoading: Boolean= false,
    val error: String ="",
    val data: List<Hit>? = null,
    val selectedHit: Hit? = null
)