package com.passgo.app.core

import org.junit.BeforeClass

abstract class BaseInstrumentedTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun initSqlCipher() {
            System.loadLibrary("sqlcipher")
        }
    }
}
