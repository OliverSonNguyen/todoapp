package s.nt.todoappdemo.home.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import s.nt.todoappdemo.home.data.local.Note
import s.nt.todoappdemo.home.data.local.NoteDao

const val PAGING = 20

interface NoteRepository {
    suspend fun getListDataOffset(limit: Int = PAGING, offset: Int): List<Note>
    suspend fun getNote(nodeId: Long): Note?
    suspend fun insert(note: Note): Long
    suspend fun insertList(list: List<Note>)
    suspend fun delete(note: Note): Int
    suspend fun deleteById(noteId: Long): Int
    suspend fun deleteAll()
}

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {


    override suspend fun getListDataOffset(limit: Int, offset: Int): List<Note> {
        return withContext(Dispatchers.IO) {
            noteDao.getListDataOffset(limit, offset)
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

    override suspend fun insertList(list: List<Note>) {
        return withContext(Dispatchers.IO) {
            noteDao.insertList(list)
        }
    }

    override suspend fun delete(note: Note): Int {
        return withContext(Dispatchers.IO) {
            noteDao.delete(note)
        }
    }

    override suspend fun deleteById(noteId: Long): Int {
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
