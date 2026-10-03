package com.example.campuslostfound.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campuslostfound.data.model.LostItem
import com.example.campuslostfound.data.repository.LostItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LostItemUiState {

    data object Idle : LostItemUiState

    data object Loading : LostItemUiState

    data class Success(
        val itemId: String
    ) : LostItemUiState

    data class Error(
        val message: String
    ) : LostItemUiState
}

class LostItemViewModel(
    private val repository: LostItemRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<LostItemUiState>(LostItemUiState.Idle)

    val uiState: StateFlow<LostItemUiState> =
        _uiState.asStateFlow()

    fun createLostItem(item: LostItem) {
        viewModelScope.launch {

            _uiState.value = LostItemUiState.Loading

            repository
                .createLostItem(item)
                .onSuccess { itemId ->
                    _uiState.value =
                        LostItemUiState.Success(itemId)
                }
                .onFailure { exception ->
                    _uiState.value =
                        LostItemUiState.Error(
                            exception.message
                                ?: "Failed to create lost item."
                        )
                }
        }
    }

    fun resetState() {
        _uiState.value = LostItemUiState.Idle
    }
}