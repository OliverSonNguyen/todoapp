package s.nt.todoappdemo.home.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
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

class HomePagingAdapter(private val itemClickCallback: ((Note) -> Unit)?, val noteRemove: ((Note) -> Unit)? = null) :
    PagingDataAdapter<Note, HomePagingAdapter.HomePagingViewHolder>(NoteDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePagingViewHolder {
        val binding = RowHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomePagingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomePagingViewHolder, position: Int) {
        val note = getItem(position)
        note?.let {
            holder.bind(it, itemClickCallback, noteRemove)
        }
    }

    class HomePagingViewHolder(
        private val binding: RowHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Note, itemClickCallback: ((Note) -> Unit)?,
            noteRemove: ((Note) -> Unit)? = null
        ) {
            binding.rowTitle.text = item.title
            binding.rowContent.text = item.content
            binding.rowUpdatedDate.text = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(item.createdDate)

            binding.root.setOnClickListener {
                itemClickCallback?.invoke(item)
            }
            binding.removeNote.setOnClickListener {
                noteRemove?.invoke(item)
            }
        }
    }
}