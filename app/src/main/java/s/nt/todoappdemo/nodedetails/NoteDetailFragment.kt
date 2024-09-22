package s.nt.todoappdemo.nodedetails

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import s.nt.todoappdemo.R
import s.nt.todoappdemo.databinding.FragmentNodeDetailBinding
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.NoteRepositoryImpl
import s.nt.todoappdemo.home.data.local.AppDatabase
import java.lang.IllegalArgumentException

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNodeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initView()
        observe()
        loadNote()

    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            inflateMenu(R.menu.menu_node_detail)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        viewModel.deleteNote()
                        true
                    }

                    else -> false
                }
            }
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun initView() {
        binding.edtTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onTitleUpdated(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.edtContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onContentUpdated(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is NoteDetailViewModel.UiState.UiStateView -> {
                        binding.edtTitle.setText(uiState.node.title)
                        binding.edtContent.setText(uiState.node.content)
                        binding.txtState.text = ""
                    }

                    is NoteDetailViewModel.UiState.UiStateInit -> {
                        binding.txtState.text = ""
                    }

                    is NoteDetailViewModel.UiState.UiStateEditing -> {
                        binding.txtState.text = getString(R.string.editting)
                    }

                    is NoteDetailViewModel.UiState.UiStateSaved -> {
                        binding.txtState.text = getString(R.string.saved)
                    }

                    is NoteDetailViewModel.UiState.UiStateDelete -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.deleted), Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun loadNote() {
        val noteId = arguments?.getLong(ARG_NOTE_ID)
        if (noteId != null) {
            viewModel.loadNote(noteId)
            enableEditing(false)
        } else {
            enableEditing(true)
        }
    }

    private fun enableEditing(enable: Boolean) {
        binding.edtTitle.isEnabled = enable
        binding.edtContent.isEnabled = enable
        if (enable) {
            binding.edtTitle.requestFocus()
            showKeyboard(binding.edtTitle)
        }
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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