package apptentive.com.android.feedback.payload

internal class MockPayloadQueue : PayloadQueue {
    private val payloads = mutableListOf<PayloadData>()

    override fun enqueuePayload(payload: PayloadData) {
        payloads.add(payload)
    }

    override fun nextUnsentPayload(): PayloadData? {
        return if (payloads.isEmpty()) null else payloads[0]
    }

    override fun deletePayload(payload: PayloadData) {
        val removed = payloads.remove(payload)
        if (!removed) {
            throw AssertionError("Payload was not in the queue")
        }
    }
}