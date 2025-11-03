package org.app.corge

import android.app.Application
import org.app.corge.di.initKoin
import org.koin.android.ext.koin.androidContext

class CorgeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityHolder)

        AppContextProvider.init(this)
        initKoin { androidContext(this@CorgeApplication) }
    }
}

