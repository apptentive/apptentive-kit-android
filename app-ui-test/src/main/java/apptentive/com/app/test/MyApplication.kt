package apptentive.com.app.test

import androidx.multidex.MultiDexApplication
import apptentive.com.android.core.AndroidLoggerProvider
import apptentive.com.android.core.DependencyProvider

class MyApplication:  MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        // register dependency providers
        DependencyProvider.register(AndroidLoggerProvider("Apptentive"))
        // DependencyProvider.register(AndroidExecutorFactoryProvider())
        // DependencyProvider.register(AndroidFileSystemProvider(applicationContext, "apptentive.com.android.feedback"))
    }
}