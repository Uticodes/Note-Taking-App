package com.uticodes.note_taking_app

import android.app.Application
import com.uticodes.note_taking_app.di.AppModule

class NoteApp : Application() {

    // Manual Dependency Injection setup for the application scope
    lateinit var appModule: AppModule

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(applicationContext)
    }
}
