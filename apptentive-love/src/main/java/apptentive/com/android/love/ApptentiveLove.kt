package apptentive.com.android.love

object ApptentiveLove {
    private var client: ApptentiveLoveClient = ApptentiveLoveClient.NULL

    fun send(entity: LoveEntity) {
        client.send(entity)
    }
}