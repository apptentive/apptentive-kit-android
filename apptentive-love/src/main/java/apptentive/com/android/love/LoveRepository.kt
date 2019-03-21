package apptentive.com.android.love

import apptentive.com.android.concurrent.Promise

internal interface LoveRepository {
    fun send(entity: LoveEntity): Promise<Unit>
}

internal class DefaultLoveRepository(private val service: APIService) : LoveRepository {
    override fun send(entity: LoveEntity): Promise<Unit> {
        return service.send(entity)
    }
}