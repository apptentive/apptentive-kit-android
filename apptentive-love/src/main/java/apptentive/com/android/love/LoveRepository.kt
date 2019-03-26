package apptentive.com.android.love

import apptentive.com.android.concurrent.Promise
import apptentive.com.android.util.Resource

internal interface LoveRepository {
    fun send(entity: LoveEntity): Promise<Unit>
    fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>>
}

internal class DefaultLoveRepository(private val service: APIService) : LoveRepository {
    override fun getEntities(): Promise<Resource<List<LoveEntitySnapshot>>> {
        return service.getEntities()
    }

    override fun send(entity: LoveEntity): Promise<Unit> {
        return service.send(entity)
    }
}