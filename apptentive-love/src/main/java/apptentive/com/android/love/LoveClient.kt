package apptentive.com.android.love

import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.concurrent.Promise
import apptentive.com.android.core.NotInitializedException
import apptentive.com.android.util.Resource
import java.lang.IllegalStateException

internal interface LoveClient : LoveSender {
    fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>>

    companion object {
        val NULL: LoveClient = LoveClientNull()
    }
}

private class LoveClientNull : LoveClient {
    override fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>> {
        val promise = AsyncPromise<Resource<List<LoveEntitySnapshot>>>()
        promise.reject(IllegalStateException("SDK is not properly initialized"))
        return promise
    }

    override fun send(entity: LoveEntity, callback: LoveSender.SendCallback?) {
        callback?.onSendFail(entity, NotInitializedException("Can't send entity"))
    }
}
