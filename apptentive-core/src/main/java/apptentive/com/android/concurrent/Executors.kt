package apptentive.com.android.concurrent

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
data class Executors(
    val state: Executor,
    val main: Executor
)
