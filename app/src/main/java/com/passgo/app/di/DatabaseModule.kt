package com.passgo.app.di

import android.content.Context
import com.passgo.app.core.database.PassGoDatabase
import com.passgo.app.core.database.dao.AttachmentDao
import com.passgo.app.core.database.dao.CustomFieldDao
import com.passgo.app.core.database.dao.FolderDao
import com.passgo.app.core.database.dao.SearchHistoryDao
import com.passgo.app.core.database.dao.TagDao
import com.passgo.app.core.database.dao.VaultDao
import com.passgo.app.core.database.dao.VaultItemDao
import com.passgo.app.core.security.MasterKeyManager
import com.passgo.app.data.repository.AttachmentRepository
import com.passgo.app.data.repository.AttachmentRepositoryImpl
import com.passgo.app.data.repository.CustomFieldRepository
import com.passgo.app.data.repository.CustomFieldRepositoryImpl
import com.passgo.app.data.repository.FolderRepository
import com.passgo.app.data.repository.FolderRepositoryImpl
import com.passgo.app.data.repository.SearchHistoryRepository
import com.passgo.app.data.repository.SearchHistoryRepositoryImpl
import com.passgo.app.data.repository.TagRepository
import com.passgo.app.data.repository.TagRepositoryImpl
import com.passgo.app.data.repository.VaultItemRepository
import com.passgo.app.data.repository.VaultItemRepositoryImpl
import com.passgo.app.data.repository.VaultRepository
import com.passgo.app.data.repository.VaultRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository

    @Binds
    @Singleton
    abstract fun bindVaultItemRepository(impl: VaultItemRepositoryImpl): VaultItemRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository

    @Binds
    @Singleton
    abstract fun bindCustomFieldRepository(impl: CustomFieldRepositoryImpl): CustomFieldRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    companion object {
        @Provides
        @Singleton
        fun providePassGoDatabase(
            @ApplicationContext context: Context,
            masterKeyManager: MasterKeyManager
        ): PassGoDatabase {
            val masterKey = masterKeyManager.getOrCreateMasterKey()
            val dbPassphrase = masterKey.copyOf()
            return PassGoDatabase.build(context, dbPassphrase)
        }

        @Provides
        fun provideVaultDao(database: PassGoDatabase): VaultDao = database.vaultDao()

        @Provides
        fun provideVaultItemDao(database: PassGoDatabase): VaultItemDao = database.vaultItemDao()

        @Provides
        fun provideFolderDao(database: PassGoDatabase): FolderDao = database.folderDao()

        @Provides
        fun provideTagDao(database: PassGoDatabase): TagDao = database.tagDao()

        @Provides
        fun provideAttachmentDao(database: PassGoDatabase): AttachmentDao = database.attachmentDao()

        @Provides
        fun provideCustomFieldDao(database: PassGoDatabase): CustomFieldDao = database.customFieldDao()

        @Provides
        fun provideSearchHistoryDao(database: PassGoDatabase): SearchHistoryDao = database.searchHistoryDao()
    }
}
