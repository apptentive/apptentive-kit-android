package apptentive.com.android.concurrent

val mockExecutors = Executors(
    state = ImmediateExecutor,
    main = ImmediateExecutor
)