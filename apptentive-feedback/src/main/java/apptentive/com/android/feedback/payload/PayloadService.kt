package apptentive.com.android.feedback.payload

import androidx.annotation.VisibleForTesting
import apptentive.com.android.util.Result

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
interface PayloadService {
    fun sendPayload(payload: PayloadData, callback: (Result<PayloadData>) -> Unit)
}
