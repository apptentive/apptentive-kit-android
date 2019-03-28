package apptentive.com.android.love

interface LoveSender {
    fun send(
        entity: LoveEntity,
        onSend: ((entity: LoveEntity) -> Unit)? = null,
        onError: ((entity: LoveEntity, error: Exception) -> Unit)? = null
    )
}