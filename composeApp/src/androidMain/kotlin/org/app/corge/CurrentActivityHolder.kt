package org.app.corge

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object CurrentActivityHolder : Application.ActivityLifecycleCallbacks {
    @Volatile private var ref = WeakReference<Activity?>(null)

    fun current(): Activity? = ref.get()

    override fun onActivityResumed(activity: Activity)   { ref = WeakReference(activity) }
    override fun onActivityCreated(a: Activity, s: Bundle?) {}
    override fun onActivityStarted(a: Activity) {}
    override fun onActivityPaused(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(a: Activity) {
        if (ref.get() === a) ref = WeakReference(null)
    }
}
