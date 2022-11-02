package apptentive.com.android.feedback.payload

class MockPayloadQueue : PayloadQueue {
    private val payloads = mutableListOf<PayloadData>()

    override fun enqueuePayload(payload: PayloadData) {
        payloads.add(payload)
    }

    override fun nextUnsentPayload(): PayloadData? {
        return if (payloads.isEmpty()) null else payloads[0]
    }

    override fun deletePayloadAndAssociatedFiles(payload: PayloadData) {
        val removed = payloads.remove(payload)
        if (!removed) {
            throw AssertionError("Payload was not in the queue")
        }
    }
}
