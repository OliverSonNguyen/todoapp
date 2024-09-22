package s.nt.todoappdemo.home.data

import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import s.nt.todoappdemo.home.data.local.Note
import s.nt.todoappdemo.home.data.local.NoteDao

interface NoteRepository {
    fun getPagedNotes(): PagingSource<Int, Note>
    suspend fun getAllNotes(): List<Note>
    suspend fun getNote(nodeId: Long): Note?
    suspend fun insert(note: Note): Long
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
    suspend fun deleteById(noteId: Long)
    suspend fun deleteAll()
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {

    override fun getPagedNotes(): PagingSource<Int, Note> {
        return noteDao.getPagingNote()
    }

    override suspend fun getAllNotes(): List<Note> {
        return withContext(Dispatchers.IO) {
            delay(5000)
            noteDao.getAllNote()
        }
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
