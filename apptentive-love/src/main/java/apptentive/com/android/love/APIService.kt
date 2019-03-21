package apptentive.com.android.love

import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.core.TimeInterval
import apptentive.com.android.util.Resource
import java.util.*

internal interface APIService {
    fun send(entity: LoveEntity): Promise<Unit>
    fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>>
}

internal class FakeAPIService(
    private val apptentiveKey: String,
    private val apptentiveSignature: String,
    private val executorQueue: ExecutorQueue,
    private val callbackDelay: TimeInterval = 0.5
) : APIService {
    private val entities = mutableListOf<LoveEntitySnapshot>()

    override fun send(entity: LoveEntity): Promise<Unit> {
        val promise = AsyncPromise<Unit>()
        val existingIndex = entities.indexOfFirst { it.loveEntity.identifier == entity.identifier }
        val snapshot = LoveEntitySnapshot(entity, Date())
        if (existingIndex != -1) {
            entities[existingIndex] = snapshot
        } else {
            entities.add(snapshot)
        }
        executorQueue.execute(callbackDelay) { promise.resolve(Unit) }
        return promise
    }

    override fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>> {
        val promise = AsyncPromise<Resource<List<LoveEntitySnapshot>>>()
        executorQueue.execute(callbackDelay) {
            promise.resolve(Resource.success(entities))
        }
        return promise
    }
}

data class LoveEntitySnapshot(
    val loveEntity: LoveEntity,
    val timestamp: Date
) {
    fun description(): String {
        return "LoveEntity:\n\t$loveEntity\n\t$timestamp"
    }
}