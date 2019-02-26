package apptentive.com.android.convert

import apptentive.com.android.TestCase
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.ImmediateExecutorQueue
import apptentive.com.android.convert.json.JsonStreamDeserializer
import apptentive.com.android.convert.json.JsonStreamSerializer
import org.junit.Assert.*
import org.junit.Test

import org.junit.rules.TemporaryFolder
import org.junit.Rule

class SerializationTest : TestCase() {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSerializeObject() {
        val expected = MyClass()
        expected.value = "value"

        val file = tempFolder.newFile("data.json")

        val immediateExecutorQueue = ImmediateExecutorQueue("dispatch", dispatchManually = true)
        val executors = Executors(immediateExecutorQueue, immediateExecutorQueue)

        val serializer = JsonStreamSerializer.fromFile(file)
        val deserializer = JsonStreamDeserializer.fromFile<MyClass>(file)

        val serialization: Serialization = SerializationImpl(executors, serializer, deserializer)

        var serialized = false
        serialization.serializeObject(expected).then {
            serialized = true
        }
        immediateExecutorQueue.dispatchAll() // first dispatch will schedule the serialization
        immediateExecutorQueue.dispatchAll() // second dispatch will dispatch the callback
        assertTrue(serialized)

        var actual: MyClass? = null
        serialization.deserializeObject().then { value -> actual = value as MyClass }
        immediateExecutorQueue.dispatchAll() // first dispatch will schedule the serialization
        immediateExecutorQueue.dispatchAll() // second dispatch will dispatch the callback
        assertEquals(expected, actual)
    }
}

private class MyClass {
    var value: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyClass

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}