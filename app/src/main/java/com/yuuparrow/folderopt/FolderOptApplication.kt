package com.yuuparrow.folderopt

import android.app.Application
import com.yuuparrow.folderopt.di.AppContainer

class FolderOptApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
