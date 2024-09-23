package s.nt.todoappdemo.home.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import s.nt.todoappdemo.databinding.RowHomeBinding
import s.nt.todoappdemo.home.data.local.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NoteDiff : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return (oldItem.title == newItem.title && oldItem.content == newItem.content && oldItem.updatedDate == newItem.updatedDate)
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}

class HomeAdapter(
    private val itemClickCallback: ((Note) -> Unit)?, private val noteRemove: ((Note) -> Unit)? = null
) : ListAdapter<Note, HomeAdapter.HomeViewHolder>(NoteDiff()) {
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = RowHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding, dateFormat)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickCallback, noteRemove)
    }

    class HomeViewHolder(
        private val binding: RowHomeBinding, private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Note, itemClickCallback: ((Note) -> Unit)?, noteRemove: ((Note) -> Unit)? = null
        ) {
            binding.rowTitle.text = item.title
            binding.rowContent.text = item.content
            binding.rowUpdatedDate.text = dateFormat.format(item.updatedDate)
            binding.root.setOnClickListener {
                itemClickCallback?.invoke(item)
            }
            binding.removeNote.setOnClickListener {
                noteRemove?.invoke(item)
            }
        }
    }
}