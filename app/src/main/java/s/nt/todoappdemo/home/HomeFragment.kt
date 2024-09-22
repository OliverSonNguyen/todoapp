package s.nt.todoappdemo.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import s.nt.todoappdemo.R
import s.nt.todoappdemo.databinding.FragmentHomeBinding
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.NoteRepositoryImpl
import s.nt.todoappdemo.home.data.local.AppDatabase
import s.nt.todoappdemo.home.data.local.Note
import s.nt.todoappdemo.home.view.adapter.HomePagingAdapter
import s.nt.todoappdemo.nodedetails.NoteDetailFragment


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    //using Hilt for injection
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }
    private lateinit var adapterPaging: HomePagingAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupToolbar()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = getString(R.string.app_name)
            navigationIcon = null
            inflateMenu(R.menu.menu_home)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.actionInstall -> {
                        viewModel.insert2000Items()
                        true
                    }

                    R.id.actionClean -> {
                        viewModel.deleteAllItems()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun initView() {
        binding.btnAdd.setOnClickListener {
            navigateToNoteDetailFragment()
        }

        adapterPaging = HomePagingAdapter(noteRemove = {
            Toast.makeText(requireContext(), "Deleted" + it.title, Toast.LENGTH_SHORT).show()
            viewModel.deleteNote(it)
        }, itemClickCallback = {
            navigateToNoteDetailFragment(it)
        })
        binding.homeRcv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.homeRcv.adapter = adapterPaging
        adapterPaging.addLoadStateListener { loadState ->
            when (loadState.append) {
                is LoadState.Loading -> {
                    // Log when a new page is being loaded
                    Log.d("Paging", ">>> Loading next page...")
                }

                is LoadState.NotLoading -> {
                    // Log when loading is finished
                    Log.d("Paging", ">>> Finished loading the current page.")
                }

                is LoadState.Error -> {
                    // Log the error
                    val error = loadState.append as LoadState.Error
                    Log.e("Paging", ">>> Error loading page: ${error.error.message}")
                }
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is HomeViewModel.UiState.UiStateLoading -> {
                        binding.loadingIndicator.isVisible = true
                    }

                    is HomeViewModel.UiState.UiStateError -> {
                        binding.loadingIndicator.isVisible = false
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }

                    is HomeViewModel.UiState.UiStateReady -> {
                        binding.loadingIndicator.isVisible = false
                        adapterPaging.submitData(uiState.pagingData)

                    }
                }
            }
        }
    }

    private fun navigateToNoteDetailFragment(note: Note? = null) {
        val fragment = if (note != null) NoteDetailFragment.newInstance(note.id) else NoteDetailFragment()
        parentFragmentManager.beginTransaction().replace(R.id.homeContainer, fragment).addToBackStack(null).commit()
    }

}

//remove when using Hilt or dagger
@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val appDatabase = AppDatabase.getDatabase(context)
            return HomeViewModel(noteRepository = NoteRepositoryImpl(appDatabase.noteDao())) as T
        }
        throw IllegalArgumentException("Unknown HomeViewModel")
    }
}