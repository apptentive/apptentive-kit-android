package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

class SerialPayloadSender(
    private val payloadQueue: PayloadQueue,
    private val callback: (Result<PayloadData>) -> Unit
) : PayloadSender {
    private var active: Boolean = true
    private var busySending: Boolean = false
    private var payloadService: PayloadService? = null

    override fun sendPayload(payload: PayloadData) {
        payloadQueue.enqueuePayload(payload)
        sendNextUnsentPayload()
    }

    fun pauseSending() {
        active = false
    }

    fun resumeSending() {
        val wasActive = active
        active = true
        if (!wasActive) {
            sendNextUnsentPayload()
        }
    }

    private fun handleSentPayload(payload: PayloadData) {
        payloadQueue.deletePayload(payload)
        notifySuccess(payload)
        sendNextUnsentPayload()
    }

    private fun handleFailedPayload(payload: PayloadData, error: Throwable) {
        val shouldDeletePayload = shouldDeletePayload(error)
        if (shouldDeletePayload) {
            payloadQueue.deletePayload(payload)
            notifyFailure(error, payload)
            sendNextUnsentPayload()
        } else {
            notifyFailure(error, payload)
        }
    }

    private fun shouldDeletePayload(error: Throwable): Boolean {
        return when (error) {
            is PayloadRejectedException -> {
                return true
            }
            else -> false // FIXME: figure out an error resolution strategy
        }
    }

    private fun sendNextUnsentPayload() {
        if (payloadService == null) {
            //TODO: log message: can't send as payload service is null
            return
        }

        if (!active) {
            // TODO: log message: con't send while being inactive
            return
        }

        if (busySending) {
            // TODO: log message: con't send while still busy
            return
        }


        val nextPayload = payloadQueue.nextUnsentPayload()
            ?: // TODO: log message 'all done'
            return

        busySending = true

        payloadService?.sendPayload(nextPayload) {
            busySending = false

            when (it) {
                is Result.Success -> handleSentPayload(nextPayload)
                is Result.Error -> handleFailedPayload(nextPayload, it.error)
            }
        }
    }

    fun setPayloadService(service: PayloadService) {
        payloadService = service
        sendNextUnsentPayload()
    }

    val hasPayloadService get() = payloadService != null

    private fun notifySuccess(payload: PayloadData) {
        try {
            callback.invoke(Result.Success(payload))
        } catch (e: Exception) {
            // FIXME: print error message
        }
    }

    private fun notifyFailure(error: Throwable, payload: PayloadData) {
        try {
            if (error is PayloadSendException) {
                callback(Result.Error(error))
            } else {
                callback(Result.Error(PayloadSendException(payload, cause = error)))
            }
        } catch (e: Exception) {
            // FIXME: print error message
        }
    }
}