package s.nt.todoappdemo.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.local.Note

const val PAGING = 20

class HomeViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.UiStateLoading)
    val uiState = _uiState.asStateFlow()
    private val _refresh = MutableSharedFlow<Unit>()
    private val _originalData = MutableStateFlow<MutableList<Note>>(mutableListOf())

    //paging manually, lazy load
    private var offset = 0

    init {
        viewModelScope.launch {
            try {
                _refresh.flatMapLatest {
                    Log.d("", ">>>start1 ${Thread.currentThread()}")
                    val items = noteRepository.getAllNotes()
                    _originalData.value = items.toMutableList()
                    if (items.isNotEmpty()) {
                        if (offset + PAGING < items.size) {
                            offset += PAGING
                        } else {
                            offset = items.size
                        }
                    }
                    Log.d("", ">>>start2 ${Thread.currentThread()}")
                    val uiState = UiState.UiStateLoaded(items = _originalData.value.take(offset))
                    _uiState.value = uiState
                    flow {
                        emit(uiState)
                    }

                }
                    .flowOn(Dispatchers.IO)

                    .collect { newState: UiState.UiStateLoaded ->
                        _uiState.value = newState
                    }

            } catch (e: Exception) {
                val uiState = UiState.UiStateError(e.message)
                _uiState.value = uiState
            }


        }
        viewModelScope.launch {
            _refresh.emit(Unit)
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            try {
                if (offset >= _originalData.value.size) {
                    return@launch
                }
                _uiState.value = UiState.UiStateLoading

                if (offset + PAGING < _originalData.value.size) {
                    offset += PAGING
                } else {
                    offset = _originalData.value.size
                }
                val uiState = UiState.UiStateLoaded(items = _originalData.value.take(offset))
                _uiState.value = uiState
            } catch (e: Exception) {
                val uiState = UiState.UiStateError(e.message)
                _uiState.value = uiState
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _refresh.emit(Unit)
        }
    }

    sealed class UiState {
        data object UiStateLoading : UiState()
        data class UiStateLoaded(val items: List<Note> = emptyList()) : UiState()
        data class UiStateError(val message: String? = null) : UiState()
    }
}