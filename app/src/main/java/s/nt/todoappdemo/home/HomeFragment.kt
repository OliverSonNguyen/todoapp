package s.nt.todoappdemo.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import s.nt.todoappdemo.databinding.FragmentHomeBinding
import s.nt.todoappdemo.home.data.NoteRepository
import s.nt.todoappdemo.home.data.NoteRepositoryImpl
import s.nt.todoappdemo.home.data.local.AppDatabase
import s.nt.todoappdemo.home.view.adapter.HomeAdapter


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    //using Hilt for injection
    val noteRepository: NoteRepository by lazy { NoteRepositoryImpl(AppDatabase.getDatabase(requireContext()).noteDao())}
    private val viewModel: HomeViewModel by lazy { HomeViewModel(noteRepository) }

    private lateinit var adapter: HomeAdapter
    private val backtrackChange = FragmentManager.OnBackStackChangedListener {
        if (parentFragmentManager.backStackEntryCount == 0) {
            viewModel.refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragmentManager.addOnBackStackChangedListener(backtrackChange)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun initView() {
        binding.btnAdd.setOnClickListener {
        }
        adapter = HomeAdapter { item ->
            Log.d("", ">>>item click:$item")
        }
        binding.homeRcv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.homeRcv.adapter = adapter
        binding.homeRcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
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

                    is HomeViewModel.UiState.UiStateLoaded -> {
                        binding.loadingIndicator.isVisible = false
                        adapter.submitList(uiState.items)
                    }

                    is HomeViewModel.UiState.UiStateError -> {
                        binding.loadingIndicator.isVisible = false
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        parentFragmentManager.removeOnBackStackChangedListener(backtrackChange)
        super.onDestroy()
    }


}