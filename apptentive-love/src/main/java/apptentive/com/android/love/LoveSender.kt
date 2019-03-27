package apptentive.com.android.love

interface LoveSender {
    fun send(entity: LoveEntity, callback: SendCallback? = null)

    interface SendCallback {
        fun onSendFinished(entity: LoveEntity)
        fun onSendFail(entity: LoveEntity, error: Exception)
    }
}