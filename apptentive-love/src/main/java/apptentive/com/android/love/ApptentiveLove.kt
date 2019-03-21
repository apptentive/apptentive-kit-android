package apptentive.com.android.love

import android.app.Application
import apptentive.com.android.core.DependencyProvider

object ApptentiveLove {
    private var client: LoveClient = LoveClient.NULL

    fun register(application: Application, apptentiveKey: String, apptentiveSignature: String) {
        DependencyProvider.register(application)
        client = LoveDefaultClient(application.applicationContext, apptentiveKey, apptentiveSignature)
    }

    fun send(entity: LoveEntity, callback: ((Boolean) -> Unit) = {}) {
        client.send(entity, callback)
    }
}