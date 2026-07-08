package com.passgo.app.di

import android.content.Context
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.AttachmentManager
import com.passgo.app.core.security.KeyStoreManager
import com.passgo.app.core.security.MasterKeyManager
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.data.session.SessionManager
import com.passgo.app.data.settings.FailedAttemptStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePassGoLogger(): PassGoLogger {
        return PassGoLogger()
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        logger: PassGoLogger,
        passwordStore: MasterPasswordStore,
        failedAttemptStore: FailedAttemptStore,
        masterKeyManager: MasterKeyManager,
        keyStoreManager: KeyStoreManager
    ): SessionManager {
        return SessionManager(logger, passwordStore, failedAttemptStore, masterKeyManager, keyStoreManager)
    }

    @Provides
    @Singleton
    fun provideAttachmentManager(
        keyStoreManager: KeyStoreManager,
        @ApplicationContext context: Context
    ): AttachmentManager {
        return AttachmentManager(keyStoreManager, context)
    }
}
