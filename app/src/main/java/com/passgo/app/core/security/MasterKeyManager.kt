package com.passgo.app.core.security

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterKeyManager @Inject constructor(
    private val keyDerivation: KeyDerivation,
    private val keyStoreManager: KeyStoreManager
) {

    @Volatile
    private var cachedMasterKey: ByteArray? = null

    fun getOrCreateMasterKey(): ByteArray {
        cachedMasterKey?.let { return it }

        val alias = KEY_ALIAS
        val storedKey = keyStoreManager.loadDatabaseKey(alias)
        if (storedKey != null) {
            cachedMasterKey = storedKey
            return storedKey
        }

        val newKey = keyStoreManager.generateAndStoreDatabaseKey(alias)
        cachedMasterKey = newKey
        return newKey
    }

    fun createMasterKey(password: CharArray): MasterKeyResult {
        val salt = keyDerivation.generateSalt()
        val derivedKey = keyDerivation.deriveKey(password, salt)
        keyDerivation.clearPassword(password)

        val encryptedDbKey = keyStoreManager.encrypt(derivedKey)

        return MasterKeyResult(
            encryptedDbKey = encryptedDbKey,
            derivedKey = derivedKey,
            salt = salt
        )
    }

    fun unlockWithPassword(password: CharArray, salt: ByteArray): ByteArray {
        val derivedKey = keyDerivation.deriveKey(password, salt)
        keyDerivation.clearPassword(password)
        cachedMasterKey = derivedKey
        return derivedKey
    }

    fun unlockWithBiometric(encryptedDbKey: KeyStoreManager.EncryptedData): ByteArray {
        val key = keyStoreManager.decrypt(encryptedDbKey)
        cachedMasterKey = key
        return key
    }

    fun rotateKey(
        currentPassword: CharArray,
        newPassword: CharArray,
        salt: ByteArray
    ): MasterKeyResult {
        val currentKey = unlockWithPassword(currentPassword, salt)
        currentKey.fill(0)

        val newSalt = keyDerivation.generateSalt()
        val newDerivedKey = keyDerivation.deriveKey(newPassword, newSalt)
        keyDerivation.clearPassword(newPassword)

        val newEncryptedDbKey = keyStoreManager.encrypt(newDerivedKey)
        cachedMasterKey = newDerivedKey

        return MasterKeyResult(
            encryptedDbKey = newEncryptedDbKey,
            derivedKey = newDerivedKey,
            salt = newSalt
        )
    }

    fun clearCache() {
        cachedMasterKey?.fill(0)
        cachedMasterKey = null
    }

    fun clearOnLock() {
        clearCache()
    }

    data class MasterKeyResult(
        val encryptedDbKey: KeyStoreManager.EncryptedData,
        val derivedKey: ByteArray,
        val salt: ByteArray
    )

    companion object {
        private const val KEY_ALIAS = "passgo_master_key"
    }
}
