package apptentive.com.android.feedback.payload

internal class MockPayloadQueue : PayloadQueue {
    private val payloads = mutableListOf<Payload>()

    override fun enqueuePayload(payload: Payload) {
        payloads.add(payload)
    }

    override fun nextUnsentPayload(): Payload? {
        return if (payloads.isEmpty()) null else payloads[0]
    }

    override fun deletePayload(payload: Payload) {
        val removed = payloads.remove(payload)
        if (!removed) {
            throw AssertionError("Payload was not in the queue")
        }
    }
}