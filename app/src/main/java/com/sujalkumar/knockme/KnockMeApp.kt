package com.sujalkumar.knockme

import android.app.Application
import com.sujalkumar.knockme.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KnockMeApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KnockMeApp)
            modules(appModule)
        }
    }
}