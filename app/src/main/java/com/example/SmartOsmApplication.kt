package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.AppContainer

class SmartOsmApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
