package com.passgo.app.core.security

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordGeneratorTest {

    private val passwordGenerator = PasswordGenerator()

    @Test
    fun `generate returns CharArray`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions())
        assertTrue(result is CharArray)
    }

    @Test
    fun `generate returns expected length`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 16))
        assertEquals(16, result.size)
    }

    @Test
    fun `generate with length 32 returns 32 chars`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 32))
        assertEquals(32, result.size)
    }

    @Test
    fun `generate returns different values each call`() {
        val result1 = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 20))
        val result2 = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 20))
        assertNotEquals(result1.contentToString(), result2.contentToString())
    }

    @Test
    fun `generate with no character sets returns empty array`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(
            length = 20,
            includeUppercase = false,
            includeLowercase = false,
            includeDigits = false,
            includeSymbols = false
        ))
        assertEquals(0, result.size)
    }

    @Test
    fun `wipePassword zeros all characters`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 20))
        passwordGenerator.wipePassword(result)
        for (c in result) {
            assertEquals('\u0000', c)
        }
    }

    @Test
    fun `wipePassword on already wiped array is safe`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 10))
        passwordGenerator.wipePassword(result)
        assertDoesNotThrow { passwordGenerator.wipePassword(result) }
    }

    @Test
    fun `generate produces at least one of each selected character type`() {
        val result = passwordGenerator.generate(PasswordGenerator.GeneratorOptions(
            length = 100,
            includeUppercase = true,
            includeLowercase = true,
            includeDigits = true,
            includeSymbols = true
        ))
        val resultStr = String(result)
        passwordGenerator.wipePassword(result)
        assertTrue(resultStr.any { it.isUpperCase() }, "No uppercase chars")
        assertTrue(resultStr.any { it.isLowerCase() }, "No lowercase chars")
        assertTrue(resultStr.any { it.isDigit() }, "No digit chars")
        assertTrue(resultStr.any { !it.isLetterOrDigit() }, "No symbol chars")
    }

    @Test
    fun `wipePassword is available as public helper`() {
        assertDoesNotThrow { passwordGenerator::wipePassword }
    }
}
