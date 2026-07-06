package com.passgo.app.feature.autofill.repository

import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.VaultItem
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.repository.VaultItemRepository
import com.passgo.app.data.repository.VaultRepository
import com.passgo.app.data.session.SessionManager
import com.passgo.app.feature.autofill.matcher.CredentialMatcher
import com.passgo.app.feature.autofill.model.AutofillCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutofillRepository @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val vaultItemRepository: VaultItemRepository,
    private val sessionManager: SessionManager,
    private val credentialMatcher: CredentialMatcher,
    private val logger: PassGoLogger
) {
    fun isVaultUnlocked(): Boolean = sessionManager.isUnlocked()

    fun getAllAvailableCredentials(): List<AutofillCredential> {
        if (!isVaultUnlocked()) return emptyList()

        return runBlocking(Dispatchers.IO) {
            try {
                val vault = vaultRepository.getActiveVault().first() ?: return@runBlocking emptyList()
                val items = vaultItemRepository.getActiveItems(vault.id).first()
                items.map { it.toAutofillCredential() }
            } catch (e: Exception) {
                logger.error("AutofillRepository", "Failed to retrieve credentials: ${e.message}")
                emptyList()
            }
        }
    }

    fun performSave(
        packageName: String,
        domain: String?,
        username: String,
        email: String,
        password: String
    ) {
        if (!isVaultUnlocked()) return

        runBlocking(Dispatchers.IO) {
            try {
                val vault = vaultRepository.getActiveVault().first() ?: return@runBlocking
                val existingItems = vaultItemRepository.getActiveItems(vault.id).first()
                val existing = existingItems.firstOrNull { item ->
                    item.url.isNotBlank() && credentialMatcher.matchesAppContext(item.url, packageName, domain)
                }

                if (existing != null) {
                    val updated = existing.copy(
                        username = username.ifEmpty { existing.username },
                        email = email.ifEmpty { existing.email },
                        password = password,
                        updatedAt = System.currentTimeMillis()
                    )
                    when (val result = vaultItemRepository.update(updated)) {
                        is AppResult.Success -> logger.info("AutofillRepository", "Updated existing credential: ${existing.id}")
                        is AppResult.Error -> logger.warn("AutofillRepository", "Failed to update credential: ${result.exception.message}")
                    }
                } else {
                    val newItem = VaultItem(
                        id = UUID.randomUUID().toString(),
                        vaultId = vault.id,
                        category = VaultItemCategory.OTHER,
                        name = (domain ?: packageName).take(128),
                        username = username,
                        email = email,
                        password = password,
                        url = domain?.let { "https://$it" } ?: "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    when (val result = vaultItemRepository.insert(newItem)) {
                        is AppResult.Success -> logger.info("AutofillRepository", "Created new credential from autofill")
                        is AppResult.Error -> logger.warn("AutofillRepository", "Failed to create credential: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                logger.error("AutofillRepository", "Failed to save credentials: ${e.message}")
            }
        }
    }

    private fun VaultItem.toAutofillCredential() = AutofillCredential(
        id = id,
        name = name,
        username = username,
        email = email,
        password = password,
        url = url,
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
