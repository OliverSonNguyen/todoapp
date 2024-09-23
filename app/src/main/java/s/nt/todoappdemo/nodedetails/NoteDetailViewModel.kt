package s.nt.todoappdemo.nodedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.local.Note

class NoteDetailViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.UiStateInit)
    val uiState = _uiState.asStateFlow()


    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            val note: Note? = noteRepository.getNote(noteId)
            note?.let {
                _uiState.value = UiState.UiStateView(note)
            }
        }
    }

    sealed class UiState {
        //let update user when app save when user type
        data object UiStateInit : UiState()
        data class UiStateView(val node: Note) : UiState()
    }
}