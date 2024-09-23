@file:OptIn(ExperimentalCoroutinesApi::class)

package s.nt.todoappdemo.home

import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import s.nt.todoappdemo.R
import s.nt.todoappdemo.databinding.DialogInputBinding
import s.nt.todoappdemo.databinding.FragmentHomeBinding
import s.nt.todoappdemo.home.data.NoteRepositoryImpl
import s.nt.todoappdemo.home.data.local.AppDatabase
import s.nt.todoappdemo.home.data.local.Note
import s.nt.todoappdemo.home.view.adapter.HomeAdapter
import s.nt.todoappdemo.nodedetails.NoteDetailFragment


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    //using Hilt for injection
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }
    private lateinit var adapter: HomeAdapter


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
            showInputDialog { title, description ->
                viewModel.addTodo(title, description)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300) // Delay for 300ms
                    binding.homeRcv.smoothScrollToPosition(0) // Smooth scroll after the delay
                }
            }

        }



        adapter = HomeAdapter(noteRemove = {
            Toast.makeText(requireContext(), "Deleted" + it.title, Toast.LENGTH_SHORT).show()
            viewModel.deleteNote(it)
        }, itemClickCallback = {
            navigateToNoteDetailFragment(it)
        })

        binding.homeRcv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.homeRcv.adapter = adapter

        binding.homeRcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                if ( (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    Log.d("",">>>recyclerview trigger load more")
                    viewModel.loadMore()
                }
            }
        })
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
                        adapter.submitList(uiState.todoList)

                    }
                }
            }
        }
    }

    private fun navigateToNoteDetailFragment(note: Note? = null) {
        val fragment = if (note != null) NoteDetailFragment.newInstance(note.id) else NoteDetailFragment()
        parentFragmentManager.beginTransaction().replace(R.id.homeContainer, fragment).addToBackStack(null).commit()
    }

    private fun showInputDialog(onInputProvided: (title: String, description: String) -> Unit) {
        // Use view binding for the dialog layout
        val dialogBinding = DialogInputBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_a_new_todo))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
                val title = dialogBinding.editTextTitle.text.toString().trim()
                val description = dialogBinding.editTextDescription.text.toString().trim()
                onInputProvided(title, description)
                dialogInterface.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
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