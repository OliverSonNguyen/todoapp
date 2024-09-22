package s.nt.todoappdemo.nodedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.local.Note
import java.util.Calendar
import java.util.Date

class NoteDetailViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.UiStateInit)
    val uiState = _uiState.asStateFlow()

    private val _titleState = MutableStateFlow("")
    private val _contentState = MutableStateFlow("")
    private var currNodeId: Long? = null
    private var createdDate: Date? = null
    private var modeView: Boolean = false

    init {
        viewModelScope.launch {
            combine(_titleState, _contentState) { title, content ->
                if (modeView || (title.isEmpty() && content.isEmpty())) {
                    _uiState.value = UiState.UiStateInit
                } else {
                    _uiState.value = UiState.UiStateEditing
                }
                title to content
            }.debounce(500)
                .collectLatest { (title, content) ->
                    if (modeView || (title.isEmpty() && content.isEmpty())) {
                        return@collectLatest
                    }
                    currNodeId?.let { nodeId ->
                        noteRepository.update(
                            Note(
                                title,
                                content,
                                id = nodeId,
                                createdDate = createdDate ?: Calendar.getInstance().time,
                                updatedDate = Calendar.getInstance().time
                            )
                        )
                    } ?: run {
                        val nodeId = noteRepository.insert(Note(title, content))
                        currNodeId = nodeId
                    }
                    _uiState.value = UiState.UiStateSaved
                }
        }

    }

    fun onTitleUpdated(newTitle: String) {
        _titleState.value = newTitle
    }

    fun onContentUpdated(newContent: String) {
        _contentState.value = newContent
    }

    fun loadNote(noteId: Long) {
        viewModelScope.launch {
            val note: Note? = noteRepository.getNote(noteId)
            note?.let {
                modeView = true
                currNodeId = noteId
                createdDate = note.createdDate
                _uiState.value = UiState.UiStateView(note)
            }
        }
    }

    fun editMode() {
        modeView = false
    }

    fun deleteNote() {
        viewModelScope.launch {
            currNodeId?.let { noteId ->
                noteRepository.deleteById(noteId)
                _uiState.value = UiState.UiStateDelete(noteId)
            }
        }
    }

    sealed class UiState {
        //let update user when app save when user type
        data object UiStateInit : UiState()
        data object UiStateEditing : UiState()
        data object UiStateSaved : UiState()
        data class UiStateView(val node: Note) : UiState()
        data class UiStateDelete(val id: Long) : UiState()
    }
}