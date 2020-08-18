package ua.com.cuteteam.core.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CoreModule(val application: Application) {

    @Singleton
    @Provides
    fun provideAppContext(): Context? {
        return application.applicationContext
    }
}