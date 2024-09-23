@file:OptIn(ExperimentalCoroutinesApi::class)

package s.nt.todoappdemo.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.local.Note
import java.util.Date
import java.util.TreeSet

class HomeViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel

    private var noteRepository: NoteRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        coEvery { noteRepository.getListDataOffset(20, 0) } returns emptyList()
        viewModel = HomeViewModel(noteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `deleteAllItems calls repository to delete all notes and updates uiState`() = runTest {
        // given
        coEvery { noteRepository.deleteAll() } just Runs

        // when
        viewModel.deleteAllItems()

        // then
        coVerify { noteRepository.deleteAll() }
        Truth.assertThat(viewModel.uiState.value as HomeViewModel.UiState.UiStateReady)
            .isEqualTo(HomeViewModel.UiState.UiStateReady(todoList = mutableListOf()))

    }

    @Test
    fun `deleteNote calls repository to delete note and removes from uiState`() = runTest {
        // given
        val sysTime = System.currentTimeMillis()
        val note1 = Note(
            id = 1L,
            title = "Note 1",
            content = "This is content for note 1",
            createdDate = Date(sysTime),
            updatedDate = Date()
        )
        val note2 = Note(
            id = 2L,
            title = "Note 2",
            content = "This is content for note 2",
            createdDate = Date(sysTime - 1000L),
            updatedDate = Date()
        )

        viewModel._originalData.clear()
        viewModel._originalData.add(note1)
        viewModel._originalData.add(note2)

        coEvery { noteRepository.deleteById(note1.id) } returns 1

        // when
        viewModel.deleteNote(note1)

        // then
        coVerify { noteRepository.deleteById(note1.id) }

        Truth.assertThat(viewModel.uiState.value as HomeViewModel.UiState.UiStateReady)
            .isEqualTo(HomeViewModel.UiState.UiStateReady(todoList = listOf(note2)))
    }

    @Test
    fun `loadMore triggers repository call and updates uiState`() = runTest {
        // given
        val sysTime = System.currentTimeMillis()
        val note1 =  Note(
            id = 1L,
            title = "Note 1",
            content = "This is content for note 1",
            createdDate = Date(sysTime),
            updatedDate = Date()
        )
        val note2 = Note(
            id = 2L,
            title = "Note 2",
            content = "This is content for note 2",
            createdDate = Date(sysTime -1000L),
            updatedDate = Date()
        )
        val mocks = TreeSet<Note>(compareByDescending { it.createdDate }).apply {
            add(note1)
            add(note2)
        }

        coEvery { noteRepository.getListDataOffset(20, any()) } returns mocks.toList()

        viewModel._originalData.clear()

        // when
        viewModel.loadMore()

        // then
        coVerify { noteRepository.getListDataOffset(20, any()) }

        Truth.assertThat(viewModel.uiState.value).isEqualTo(HomeViewModel.UiState.UiStateReady(todoList = mocks.toList()))
    }

    @Test
    fun `addTodo adds new note and updates uiState2`() = runTest {
        // given
        val title = "New Note"
        val description = "Note description"
        val currentDate = Date() // Ensure both newNote and the repository note use the same date
        val sysTime = System.currentTimeMillis()
        val newNote = Note(
            title = title,
            content = description,
            createdDate = Date(sysTime),
            updatedDate = currentDate
        )
        coEvery { noteRepository.insert(any()) } returns 100L

        // Set up existing data in _originalData
        val note1 = Note(
            id = 1L,
            title = "Note 1",
            content = "This is content for note 1",
            createdDate = Date(sysTime - 1000L),
            updatedDate = Date()
        )
        viewModel._originalData.clear()
        viewModel._originalData.add(note1)

        // when
        viewModel.addTodo(title, description)

        // then
        coVerify { noteRepository.insert(any()) }

        val actualTodoList = (viewModel.uiState.value as HomeViewModel.UiState.UiStateReady).todoList

        val expectedTodoList = mutableListOf<Note>().apply {
            add(newNote.copy(id = 100L))
            add(note1)
        }.toList()

        // Check if uiState has the correct updated list by comparing properties
        Truth.assertThat(actualTodoList.size).isEqualTo(expectedTodoList.size)

        expectedTodoList.forEachIndexed { index, expectedNote ->
            val actualNote = actualTodoList[index]
            Truth.assertThat(actualNote.id).isEqualTo(expectedNote.id)
            Truth.assertThat(actualNote.title).isEqualTo(expectedNote.title)
            Truth.assertThat(actualNote.content).isEqualTo(expectedNote.content)
        }
    }
}




