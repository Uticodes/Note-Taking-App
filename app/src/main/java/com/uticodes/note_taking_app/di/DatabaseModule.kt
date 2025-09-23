package com.uticodes.note_taking_app.di

import android.content.Context
import com.uticodes.note_taking_app.data.AppDatabase
import com.uticodes.note_taking_app.data.local.dao.NoteDao

class DatabaseModule(private val context: Context) {
    private val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val noteDao: NoteDao by lazy {
        appDatabase.noteDao()
    }
}