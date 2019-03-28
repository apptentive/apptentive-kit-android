package apptentive.com.android.love

import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.util.Resource

internal class LoveDefaultClient(
    context: Context,
    apptentiveKey: String,
    apptentiveSignature: String
) : LoveClient {
    private val applicationContext: Context = context.applicationContext
    private val repository: LoveRepository = createLoveRepository(apptentiveKey, apptentiveSignature)

    override fun send(
        entity: LoveEntity,
        onSend: ((entity: LoveEntity) -> Unit)?,
        onError: ((entity: LoveEntity, error: Exception) -> Unit)?
    ) {
        repository.send(entity)
            .then { if (onSend != null) onSend(entity) }
            .catch { error -> if (onError != null) onError(entity, error) }
    }

    override fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>> {
        return repository.getEntities()
    }

    private fun createLoveRepository(
        apptentiveKey: String,
        apptentiveSignature: String
    ): LoveRepository {
        val service: APIService = FakeAPIService(apptentiveKey, apptentiveSignature, ExecutorQueue.mainQueue)
        return DefaultLoveRepository(service)
    }
}
