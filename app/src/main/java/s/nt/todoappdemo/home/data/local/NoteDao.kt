package s.nt.todoappdemo.home.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    @Query("SELECT * FROM note ORDER BY createdDate DESC LIMIT :limit OFFSET :offset")
    fun getListDataOffset(limit: Int, offset: Int): List<Note>


    @Query("SELECT * FROM note WHERE id = :noteId LIMIT 1")
    suspend fun getNote(noteId: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: List<Note>)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note): Int

    @Query("DELETE FROM note WHERE id = :noteId")
    suspend fun deleteById(noteId: Long): Int

    @Query("DELETE FROM note")
    suspend fun deleteAll()
}