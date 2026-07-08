package com.passgo.app.core.security

import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordGenerator @Inject constructor() {

    private val secureRandom = SecureRandom()

    data class GeneratorOptions(
        val length: Int = 20,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeDigits: Boolean = true,
        val includeSymbols: Boolean = true,
        val excludeAmbiguous: Boolean = false
    )

    fun generate(options: GeneratorOptions): CharArray {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        val ambiguous = "0O1lI5S2Z8B"

        val chars = StringBuilder()

        if (options.includeUppercase) chars.append(uppercase)
        if (options.includeLowercase) chars.append(lowercase)
        if (options.includeDigits) chars.append(digits)
        if (options.includeSymbols) chars.append(symbols)

        if (options.excludeAmbiguous) {
            val filtered = StringBuilder(chars.length)
            for (c in chars) {
                if (c !in ambiguous) filtered.append(c)
            }
            chars.clear(); chars.append(filtered)
        }

        if (chars.isEmpty()) return CharArray(0)

        val password = CharArray(options.length)
        for (i in 0 until options.length) {
            password[i] = chars[secureRandom.nextInt(chars.length)]
        }

        return password
    }

    fun wipePassword(password: CharArray) {
        password.fill('\u0000')
    }
}
