package org.app.corge.screens.favorite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.app.corge.data.model.Message
import org.app.corge.data.repository.CorgeRepository
import kotlin.collections.orEmpty

class FavoritesViewModel(
    private val repo: CorgeRepository
) : ViewModel() {

    var ui by mutableStateOf<FavoritesUiState>(FavoritesUiState.Loading)
        private set

    init {
        viewModelScope.launch {
            repo.favoritesFlow().collect { list ->
                if (list.isNullOrEmpty()) {
                    ui = FavoritesUiState.Empty
                } else {
                    val dates: Map<Long, String> = list
                        .associate { m ->
                            val s = repo.getLastSessionForMessage(m.id)
                            m.id to (s?.date ?: "")
                        }
                    ui = FavoritesUiState.Loaded(list, dates)
                }
            }
        }
    }

    fun removeFromFavorites(id: Long) {
        viewModelScope.launch { repo.toggleFavorite(id) }
    }
}

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data object Empty   : FavoritesUiState()
    data class Loaded(val items: List<Message>, val lastDateById: Map<Long, String> ) : FavoritesUiState()
}