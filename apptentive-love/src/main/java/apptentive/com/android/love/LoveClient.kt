package apptentive.com.android.love

internal interface LoveClient {
    fun send(entity: LoveEntity, callback: (Boolean) -> Unit)

    companion object {
        val NULL: LoveClient = LoveClientNull()
    }
}

private class LoveClientNull : LoveClient {
    override fun send(entity: LoveEntity, callback: (Boolean) -> Unit) {
        callback(false)
    }
}
