package com.sitsofe.scanner.di

import android.content.Context
import com.sitsofe.scanner.core.auth.SessionPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {

    @Provides
    @Singleton
    fun provideSessionPrefs(@ApplicationContext context: Context): SessionPrefs {
        return SessionPrefs(context)
    }
}
