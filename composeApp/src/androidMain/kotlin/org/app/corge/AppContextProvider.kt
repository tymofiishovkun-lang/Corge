package org.app.corge

import android.annotation.SuppressLint
import android.content.Context

object AppContextProvider {
    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var ctx: Context? = null

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    fun get(): Context =
        ctx ?: error("AppContextProvider not initialized. Call AppContextProvider.init() in Application.onCreate().")
}