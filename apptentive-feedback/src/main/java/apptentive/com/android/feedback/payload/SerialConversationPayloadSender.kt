package apptentive.com.android.feedback.payload

class SerialConversationPayloadSender(
    val payloadService: PayloadService,
    val payloadQueue: PayloadQueue
) : PayloadSender {

    var active: Boolean = false
        set(value) {
            val oldValue = field
            field = value
            if (value && !oldValue) {
                // TODO: resume sending
            }
        }

    var busySending: Boolean = false

    override fun sendPayload(payload: Payload, callback: (Result<Payload>) -> Unit) {
        // TODO: store payload to the disk

        if (!active) {
            // TODO: log message: con't send while being inactive
            return
        }

        if (busySending) {
            // TODO: log message: con't send while still busy
            return
        }

        busySending = true

        // TODO: send payload
        // pick payload from the queue
        // send payload
    }


    fun setPayloadService(service: PayloadService) {
        //todo
    }
}