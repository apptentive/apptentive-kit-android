package apptentive.com.android.love

import android.app.Application
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.util.Resource

object ApptentiveLove {
    private var client: LoveClient = LoveClient.NULL

    fun register(application: Application, apptentiveKey: String, apptentiveSignature: String) {
        DependencyProvider.register(application)
        client = LoveDefaultClient(application.applicationContext, apptentiveKey, apptentiveSignature)
    }

    fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>> {
        return client.getEntities()
    }

    fun send(entity: LoveEntity, callback: ((Boolean) -> Unit) = {}) {
        client.send(entity, callback)
    }
}