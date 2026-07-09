package com.passgo.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class PassGoTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader,
        className: String?,
        context: Context
    ): Application {
        return super.newApplication(
            cl,
            PassGoApplication::class.java.name,
            context
        )
    }
}