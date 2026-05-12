package apptentive.com.android.core.concurrent

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
data class Executors(
    val state: Executor,
    val main: Executor
)
