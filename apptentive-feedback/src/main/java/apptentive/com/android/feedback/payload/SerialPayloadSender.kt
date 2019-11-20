package apptentive.com.android.feedback.payload

import apptentive.com.android.util.Result

class SerialPayloadSender(
    private val payloadService: PayloadService,
    private val payloadQueue: PayloadQueue,
    private val callback: (Result<Payload>) -> Unit
) : PayloadSender {
    private var active: Boolean = true
    private var busySending: Boolean = false

    override fun sendPayload(payload: Payload) {
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

    private fun handleSentPayload(payload: Payload) {
        payloadQueue.deletePayload(payload)
        notifySuccess(payload)
        sendNextUnsentPayload()
    }

    private fun handleFailedPayload(payload: Payload, error: Throwable) {
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

        payloadService.sendPayload(nextPayload) {
            busySending = false

            when (it) {
                is Result.Success -> handleSentPayload(nextPayload)
                is Result.Error -> handleFailedPayload(nextPayload, it.error)
            }
        }
    }

    fun setPayloadService(service: PayloadService) {
        TODO()
    }

    private fun notifySuccess(payload: Payload) {
        try {
            callback.invoke(Result.Success(payload))
        } catch (e: Exception) {
            // FIXME: print error message
        }
    }

    private fun notifyFailure(error: Throwable, payload: Payload) {
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