package apptentive.com.android.concurrent

import apptentive.com.android.core.concurrent.Executors

internal val mockExecutors = Executors(
    state = ImmediateExecutor,
    main = ImmediateExecutor
)
