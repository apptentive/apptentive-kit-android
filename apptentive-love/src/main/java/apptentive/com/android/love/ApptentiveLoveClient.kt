package apptentive.com.android.love

internal interface ApptentiveLoveClient {
    fun send(entity: LoveEntity)

    companion object {
        val NULL : ApptentiveLoveClient = ApptentiveLoveClientNull()
    }
}

private class ApptentiveLoveClientNull : ApptentiveLoveClient {
    override fun send(entity: LoveEntity) {
    }
}
