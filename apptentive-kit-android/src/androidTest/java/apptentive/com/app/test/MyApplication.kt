package apptentive.com.app.test

import android.app.Application
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.InternalUseOnly

class MyApplication : Application() {
    @OptIn(InternalUseOnly::class)
    override fun onCreate() {
        super.onCreate()

        // register dependency providers
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        // DependencyProvider.register(AndroidExecutorFactoryProvider())
        // DependencyProvider.register(AndroidFileSystemProvider(applicationContext, "apptentive.com.android.feedback"))
    }
}
