package com.passgo.app.core.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyDerivation @Inject constructor() {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 600_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 32
    }

    private val secureRandom = SecureRandom()

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        secureRandom.nextBytes(salt)
        return salt
    }

    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val secretKey = factory.generateSecret(spec)
        spec.clearPassword()
        return secretKey.encoded
    }

    fun clearPassword(password: CharArray) {
        password.fill('\u0000')
    }
}
