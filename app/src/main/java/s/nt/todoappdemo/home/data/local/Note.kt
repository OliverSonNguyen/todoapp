package s.nt.todoappdemo.home.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity(tableName = "note")
data class Note(
    val title: String?,
    val content: String?,
    val createdDate: Date = Calendar.getInstance().time,
    val updatedDate: Date = Calendar.getInstance().time,
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
)