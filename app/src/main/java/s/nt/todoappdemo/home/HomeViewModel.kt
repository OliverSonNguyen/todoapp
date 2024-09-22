package s.nt.todoappdemo.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.local.Note
import java.util.Calendar
import java.util.Date


class HomeViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.UiStateLoading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            noteRepository.getFlowPagedNotes().cachedIn(viewModelScope).collectLatest {
                val uiState = UiState.UiStateReady(pagingData = it)
                _uiState.value = uiState
            }
        }
    }

    fun insert2000Items() {
        viewModelScope.launch {
            noteRepository.deleteAll()
            val currentTime = Calendar.getInstance().timeInMillis
            for (i in 1..2000) {
                val createdDate = Date(currentTime - 60 * i * 1000L)
                val updatedDate = Date(currentTime - 60 * i * 900L)
                val note = Note(
                    title = "Note $i",
                    content = "This is content for note item $i",
                    createdDate = createdDate,
                    updatedDate = updatedDate
                )
                noteRepository.insert(note)
            }
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            noteRepository.deleteAll()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }

    sealed interface UiState {
        data object UiStateLoading : UiState
        data class UiStateReady(val pagingData: PagingData<Note>) : UiState
        data class UiStateError(val message: String? = null) : UiState
    }
}