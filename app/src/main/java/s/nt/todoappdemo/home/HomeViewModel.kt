package s.nt.todoappdemo.home

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.local.Note
import java.util.Calendar
import java.util.Date


@ExperimentalCoroutinesApi
class HomeViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.UiStateLoading)
    val uiState = _uiState.asStateFlow()
    private val _loadMore = MutableSharedFlow<Unit>()

    @VisibleForTesting
    val _originalData = mutableListOf<Note>()

    private var offset = 0
    private var isLoadMore = false

    init {
        viewModelScope.launch {
            try {
                _loadMore.flatMapLatest {
                    _uiState.value = UiState.UiStateLoading
                    val appendedList = noteRepository.getListDataOffset(offset = offset)
                    if (isLoadMore) {
                        _originalData.addAll(appendedList)
                    } else {
                        _originalData.clear()
                        _originalData.addAll(appendedList)
                    }
                    val uiState = UiState.UiStateReady(todoList = _originalData.toList())
                    _uiState.value = uiState
                    if (isLoadMore) {

                        offset += appendedList.size
                        isLoadMore = false
                    }

                    flow {
                        emit(uiState)
                    }
                }.flowOn(Dispatchers.IO)
                    .collect { uiState ->
                        _uiState.value = uiState
                    }

            } catch (e: Exception) {
                _uiState.value = UiState.UiStateError(message = e.message)
                isLoadMore = false
            }
        }

        //trigger init
        viewModelScope.launch {
            loadMore()
        }

    }

    fun loadMore() {
        if (isLoadMore) return
        viewModelScope.launch {
            isLoadMore = true
            _loadMore.emit(Unit)
        }
    }


    fun insert2000Items() {
        viewModelScope.launch {
            noteRepository.deleteAll()
            _originalData.clear()
            val uiState = UiState.UiStateReady(todoList = _originalData.toList())
            _uiState.value = uiState
            offset = 0

            val currentTime = Calendar.getInstance().timeInMillis
            val list = mutableListOf<Note>()
            for (i in 1..2000) {
                val createdDate = Date(currentTime - 60 * i * 1000L)
                val updatedDate = Date(currentTime - 60 * i * 900L)
                val note = Note(
                    title = "Note $i",
                    content = "This is content for note item $i",
                    createdDate = createdDate,
                    updatedDate = updatedDate
                )
                list.add(note)
            }
            noteRepository.insertList(list)
            isLoadMore = true
            _loadMore.emit(Unit)
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch {
            noteRepository.deleteAll()
            offset = 0
            _originalData.clear()
            val uiState = UiState.UiStateReady(todoList = _originalData.toList())
            _uiState.value = uiState
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            if (noteRepository.deleteById(note.id) == 1) {
                _originalData.remove(note)
                val uiState = UiState.UiStateReady(todoList = _originalData.toList())
                _uiState.value = uiState
            }
        }
    }

    private fun insertNote(note: Note) {
        viewModelScope.launch {
            val id = noteRepository.insert(note)
            _originalData.add(0, note.copy(id = id))
            val uiState = UiState.UiStateReady(todoList = _originalData.toList())
            _uiState.value = uiState
        }
    }

    fun addTodo(title: String, description: String) {
        val note = Note(title = title, content = description)
        insertNote(note)
    }

    sealed interface UiState {
        data object UiStateLoading : UiState
        data class UiStateReady(val todoList: List<Note> = mutableListOf()) : UiState
        data class UiStateError(val message: String? = null) : UiState
    }
}