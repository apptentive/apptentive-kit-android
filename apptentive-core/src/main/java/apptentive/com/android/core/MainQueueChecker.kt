package apptentive.com.android.core

import android.os.Looper

interface MainQueueChecker : Providable {
    fun isMainQueue(): Boolean
}

internal class MainQueueCheckerImpl : MainQueueChecker {
    override fun isMainQueue(): Boolean {
        return Looper.getMainLooper() == Looper.myLooper()
    }
}