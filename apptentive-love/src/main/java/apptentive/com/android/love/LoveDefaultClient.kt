package apptentive.com.android.love

import android.content.Context
import apptentive.com.android.concurrent.ExecutorQueue

internal class LoveDefaultClient(
    context: Context,
    apptentiveKey: String,
    apptentiveSignature: String
) : LoveClient {
    private val applicationContext: Context = context.applicationContext
    private val repository: LoveRepository = createLoveRepository(apptentiveKey, apptentiveSignature)

    override fun send(entity: LoveEntity, callback: (Boolean) -> Unit) {
        repository.send(entity)
            .then { callback(true) }
            .catch { callback(false) }
    }

    private fun createLoveRepository(
        apptentiveKey: String,
        apptentiveSignature: String
    ): LoveRepository {
        val service: APIService = FakeAPIService(apptentiveKey, apptentiveSignature, ExecutorQueue.mainQueue)
        return DefaultLoveRepository(service)
    }
}
