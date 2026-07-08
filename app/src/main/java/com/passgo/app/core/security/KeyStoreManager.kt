package com.passgo.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyStoreManager @Inject constructor() {

    private val secureRandom = SecureRandom()

    fun keyExists(alias: String = KEY_ALIAS): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.containsAlias(alias)
    }

    fun generateKey(alias: String = KEY_ALIAS): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setInvalidatedByBiometricEnrollment(false)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun generateAndStoreDatabaseKey(alias: String = KEY_ALIAS): ByteArray {
        if (!keyExists(alias)) {
            generateKey(alias)
        }

        val databaseKey = ByteArray(32)
        secureRandom.nextBytes(databaseKey)

        val encryptedData = encryptWithAlias(databaseKey, alias)
        encryptedKeyCache = PersistentEncryptedKey(encryptedData.data, encryptedData.iv, alias)

        return databaseKey
    }

    fun loadDatabaseKey(alias: String = KEY_ALIAS): ByteArray? {
        val cached = encryptedKeyCache ?: return null
        if (cached.alias != alias || !keyExists(alias)) return null
        return decryptWithAlias(EncryptedData(cached.data, cached.iv), alias)
    }

    private fun getKey(alias: String = KEY_ALIAS): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(alias, null) as SecretKey
    }

    fun encrypt(data: ByteArray): EncryptedData {
        return encryptWithAlias(data, KEY_ALIAS)
    }

    private fun encryptWithAlias(data: ByteArray, alias: String): EncryptedData {
        if (!keyExists(alias)) {
            generateKey(alias)
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(alias))
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return EncryptedData(encrypted, iv)
    }

    fun decrypt(encryptedData: EncryptedData): ByteArray {
        return decryptWithAlias(encryptedData, KEY_ALIAS)
    }

    private fun decryptWithAlias(encryptedData: EncryptedData, alias: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, encryptedData.iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(alias), spec)
        return cipher.doFinal(encryptedData.data)
    }

    fun clearCache() {
        encryptedKeyCache = null
    }

    fun deleteKey(alias: String = KEY_ALIAS) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.deleteEntry(alias)
        encryptedKeyCache = null
    }

    data class EncryptedData(
        val data: ByteArray,
        val iv: ByteArray
    )

    private data class PersistentEncryptedKey(
        val data: ByteArray,
        val iv: ByteArray,
        val alias: String
    )

    companion object {
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val KEY_ALIAS = "passgo_master_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val GCM_IV_LENGTH_BYTES = 12

        @Volatile
        private var encryptedKeyCache: PersistentEncryptedKey? = null
    }
}
