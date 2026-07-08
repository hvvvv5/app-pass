package com.passgo.app.core.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordHasher @Inject constructor() {

    private val secureRandom = SecureRandom()

    fun hashPassword(password: CharArray): HashResult {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        val hash = deriveKey(password, salt)
        return HashResult(hash, salt)
    }

    fun verifyPassword(password: CharArray, salt: ByteArray, expectedHash: ByteArray): Boolean {
        val hash = deriveKey(password, salt)
        val result = hash.contentEquals(expectedHash)
        clearPassword(password)
        return result
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val secretKey = factory.generateSecret(spec)
        spec.clearPassword()
        return secretKey.encoded
    }

    fun clearPassword(password: CharArray) {
        password.fill('\u0000')
    }

    data class HashResult(
        val hash: ByteArray,
        val salt: ByteArray
    )

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 600_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH = 32
    }
}
