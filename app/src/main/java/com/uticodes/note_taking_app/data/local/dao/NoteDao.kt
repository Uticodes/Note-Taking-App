package com.uticodes.note_taking_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.uticodes.note_taking_app.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNoteEntities(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteEntityById(noteId: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteEntity(noteEntity: NoteEntity)

    @Update
    suspend fun updateNoteEntity(noteEntity: NoteEntity)

    @Delete
    suspend fun deleteNoteEntity(noteEntity: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNoteEntities()
}