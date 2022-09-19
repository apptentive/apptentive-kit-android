package apptentive.com.android.feedback.payload

import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PAYLOADS
import apptentive.com.android.util.Result

internal class SerialPayloadSender(
    private val payloadQueue: PayloadQueue,
    private val callback: (Result<PayloadData>) -> Unit
) : PayloadSender {
    private var active: Boolean = true
    private var busySending: Boolean = false
    private var payloadService: PayloadService? = null

    override fun sendPayload(payload: Payload) {
        Log.v(PAYLOADS, "Adding Payload to queue: $payload")
        val payloadData = getPayloadData(payload)
        if (payloadData != null) {
            payloadQueue.enqueuePayload(payloadData)
        }
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
        payloadQueue.deletePayloadAndAssociatedFiles(payload)
        notifySuccess(payload)
        sendNextUnsentPayload()
    }

    private fun handleFailedPayload(payload: PayloadData, error: Throwable) {
        val shouldDeletePayload = shouldDeletePayload(error)
        if (shouldDeletePayload) {
            payloadQueue.deletePayloadAndAssociatedFiles(payload)
            notifyFailure(error, payload)
            sendNextUnsentPayload()
        } else {
            notifyFailure(error, payload)
        }
    }

    private fun shouldDeletePayload(error: Throwable): Boolean {
        return when (error) {
            is PayloadSendException -> {
                Log.d(PAYLOADS, "Payload failed to send... deleting")
                true
            }
            else -> {
                // Don't delete, retry on next launch
                Log.w(PAYLOADS, "Unknown payload exception: $error")
                false
            }
        }
    }

    private fun sendNextUnsentPayload() {
        val service = payloadService
        if (service == null) {
            Log.w(PAYLOADS, "Unable to send payload: ${PayloadService::class.java.simpleName} is null")
            return
        }

        if (!active) {
            Log.w(PAYLOADS, "Unable to send payload: payload sender is not active")
            return
        }

        if (busySending) {
            Log.d(PAYLOADS, "Unable to send payload: another payload being sent")
            return
        }

        val nextPayload = payloadQueue.nextUnsentPayload()
        if (nextPayload == null) {
            Log.d(PAYLOADS, "Unable to send payload: payload queue is empty")
            return
        }

        busySending = true

        Log.v(PAYLOADS, "Start sending payload: $nextPayload")

        service.sendPayload(nextPayload) {
            busySending = false
            Log.v(PAYLOADS, "Payload send finished")

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

    private fun getPayloadData(payload: Payload): PayloadData? {
        try {
            return payload.toPayloadData()
        } catch (e: Exception) {
            Log.e(PAYLOADS, "Exception while creating payload data: $payload", e)
        }

        return null
    }

    private fun notifySuccess(payload: PayloadData) {
        try {
            callback.invoke(Result.Success(payload))
        } catch (e: Exception) {
            Log.e(PAYLOADS, "Payload sent successfully. Callback exception", e)
        }
    }

    private fun notifyFailure(error: Throwable, payload: PayloadData) {
        try {
            if (error is PayloadSendException) {
                callback(Result.Error(payload, error))
            } else {
                callback(Result.Error(payload, PayloadSendException(payload, cause = error)))
            }
        } catch (e: Exception) {
            Log.e(PAYLOADS, "Payload NOT sent with exception", e)
        }
    }
}
