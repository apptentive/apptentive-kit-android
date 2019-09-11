package apptentive.com.android.feedback.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

interface CoroutineLauncher {
    suspend fun <T> withContext(block: suspend CoroutineScope.() -> T): T
}

object SingleThreadCoroutineLauncher : CoroutineLauncher {
    private val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override suspend fun <T> withContext(block: suspend CoroutineScope.() -> T): T =
        withContext(context, block)
}