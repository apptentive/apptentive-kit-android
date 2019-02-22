package apptentive.com.android.convert

import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.Promise

interface Serialization {
    fun readObject(): Promise<Any>
    fun writeObject(obj: Any): Promise<Unit>
}

class SerializationImpl(
    private val executors: Executors,
    private val serializer: Serializer,
    private val deserializer: Deserializer
) : Serialization {
    override fun readObject(): Promise<Any> {
        val promise = AsyncPromise<Any>(executors.callback)
        executors.io.execute {
            try {
                val obj = readObjectSync()
                promise.resolve(obj)
            } catch (e: Exception) {
                promise.reject(e)
            }
        }
        return promise
    }

    override fun writeObject(obj: Any): Promise<Unit> {
        val promise = AsyncPromise<Unit>(executors.callback)
        executors.io.execute {
            try {
                writeObjectSync(obj)
                promise.resolve(Unit)
            } catch (e: Exception) {
                promise.reject(e)
            }
        }
        return promise
    }

    private fun readObjectSync(): Any {
        return deserializer.deserialize()
    }

    private fun writeObjectSync(obj: Any) {
        serializer.serialize(obj)
    }
}