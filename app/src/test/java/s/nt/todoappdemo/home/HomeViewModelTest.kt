@file:OptIn(ExperimentalCoroutinesApi::class)

package s.nt.todoappdemo.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
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
        val mockNote = Note(
            title = "Note 1",
            content = "This is content for note 1",
            createdDate = Date(),
            updatedDate = Date()
        )
        val mockPagingData = PagingData.from(listOf(mockNote))
        every { noteRepository.getFlowPagedNotes() } coAnswers {
            delay(100)
            flowOf(mockPagingData)
        }

        viewModel = HomeViewModel(noteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState is set to UiStateReady when paged data is collected`() = runTest {
        // given

        val mockNote = Note(
            title = "Note 1",
            content = "This is content for note 1",
            createdDate = Date(),
            updatedDate = Date()
        )
        val mockPagingData = PagingData.from(listOf(mockNote))
        every { noteRepository.getFlowPagedNotes() } returns flowOf(mockPagingData)

        viewModel = HomeViewModel(noteRepository)

        Truth.assertThat(viewModel.uiState.value).isInstanceOf(HomeViewModel.UiState.UiStateReady::class.java)

        coVerify { noteRepository.getFlowPagedNotes() }
    }

    @Test
    fun `deleteAllItems calls repository to delete all notes`() = runTest {
        // given
        coEvery { noteRepository.deleteAll() } just Runs

        // when
        viewModel.deleteAllItems()

        // then
        coVerify { noteRepository.deleteAll() }
    }

    @Test
    fun `deleteNote calls repository to delete note`() = runTest {
        // given
        val mockNote = Note(
            title = "Note 1",
            content = "This is content for note 1",
            createdDate = Date(),
            updatedDate = Date()
        )
        coEvery { noteRepository.delete(mockNote) } just Runs

        // when
        viewModel.deleteNote(mockNote)

        // Then
        coVerify { noteRepository.delete(mockNote) }
    }
}




