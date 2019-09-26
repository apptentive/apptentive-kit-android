package apptentive.com.android.concurrent

import apptentive.com.android.TestCase
import org.junit.Test

class ExecutorKtTest : TestCase() {
    private val task = { addResult("Task") }
    private val executor = object : Executor {
        override fun execute(task: () -> Unit) {
            addResult("Executor")
            task()
        }
    }

    @Test
    fun execute() {
        val executor: Executor = executor
        executor.execute(task)
        assertResults("Executor", "Task")
    }

    @Test
    fun executeNullable() {
        val executor: Executor? = executor
        executor.execute(task)
        assertResults("Executor", "Task")
    }

    @Test
    fun executeNullReceiver() {
        val executor: Executor? = null
        executor.execute(task)

        assertResults("Task")
    }

    @Test
    fun executeNullReceiverNullSafety() {
        val executor: Executor? = null
        executor?.execute(task)

        assertResults()
    }

}