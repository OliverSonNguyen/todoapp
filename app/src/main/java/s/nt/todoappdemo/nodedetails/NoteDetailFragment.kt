package s.nt.todoappdemo.nodedetails

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import s.nt.todoappdemo.databinding.FragmentNodeDetailBinding
import s.nt.todoappdemo.home.data.NoteRepositoryImpl
import s.nt.todoappdemo.home.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class NoteDetailFragment : Fragment() {
    private lateinit var binding: FragmentNodeDetailBinding

    private val viewModel: NoteDetailViewModel by viewModels {
        NodeDetailViewModelFactory(requireContext())
    }

    companion object {
        private const val ARG_NOTE_ID = "note_id"

        fun newInstance(noteId: Long): NoteDetailFragment {
            val args = Bundle().apply {
                putLong(ARG_NOTE_ID, noteId)
            }
            val fragment = NoteDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNodeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observe()
        loadNote()

    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }


    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is NoteDetailViewModel.UiState.UiStateView -> {
                        binding.edtTitle.text = uiState.node.title
                        binding.edtContent.text = uiState.node.content
                        binding.createdDate.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                            uiState.node.createdDate
                        )
                    }

                    else -> {

                    }

                }
            }
        }
    }

    private fun loadNote() {
        val noteId = arguments?.getLong(ARG_NOTE_ID)
        if (noteId != null) {
            viewModel.loadNote(noteId)
        }
    }

}

//remove when using Hilt
@Suppress("UNCHECKED_CAST")
class NodeDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            val appDatabase = AppDatabase.getDatabase(context)
            return NoteDetailViewModel(noteRepository = NoteRepositoryImpl(appDatabase.noteDao())) as T
        }
        throw IllegalArgumentException("Unknown NoteDetailViewModel")
    }
}