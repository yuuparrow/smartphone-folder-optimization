package com.yuuparrow.folderopt.di

import android.content.Context
import com.yuuparrow.folderopt.data.file.FileDeleter
import com.yuuparrow.folderopt.data.repository.StorageRepository
import com.yuuparrow.folderopt.data.scanner.StorageScanner

class AppContainer(context: Context) {
    val storageScanner = StorageScanner()
    val storageRepository = StorageRepository(storageScanner)
    val fileDeleter = FileDeleter(context.applicationContext)
}
