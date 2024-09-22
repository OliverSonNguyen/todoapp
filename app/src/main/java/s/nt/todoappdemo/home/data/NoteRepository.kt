package s.nt.todoappdemo.home.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import s.nt.todoappdemo.home.data.local.Note
import s.nt.todoappdemo.home.data.local.NoteDao

const val PAGING = 20

interface NoteRepository {
    fun getFlowPagedNotes(): Flow<PagingData<Note>>
    suspend fun getNote(nodeId: Long): Note?
    suspend fun insert(note: Note): Long
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
    suspend fun deleteById(noteId: Long)
    suspend fun deleteAll()
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {

    override fun getFlowPagedNotes(): Flow<PagingData<Note>> {
        return Pager(
            config = PagingConfig(pageSize = PAGING),
            initialKey = null,
            pagingSourceFactory = {
                noteDao.getPagingNote()
            }
        ).flow
    }

    override suspend fun getNote(nodeId: Long): Note? {
        return withContext(Dispatchers.IO) {
            noteDao.getNote(nodeId)
        }
    }

    override suspend fun insert(note: Note): Long {
        return withContext(Dispatchers.IO) {
            noteDao.insert(note)
        }
    }

    override suspend fun update(note: Note) {
        return withContext(Dispatchers.IO) {
            noteDao.update(note)
        }
    }

    override suspend fun delete(note: Note) {
        return withContext(Dispatchers.IO) {
            noteDao.delete(note)
        }
    }

    override suspend fun deleteById(noteId: Long) {
        return withContext(Dispatchers.IO) {
            noteDao.deleteById(noteId)
        }
    }

    override suspend fun deleteAll() {
        return withContext(Dispatchers.IO) {
            noteDao.deleteAll()
        }
    }
}
