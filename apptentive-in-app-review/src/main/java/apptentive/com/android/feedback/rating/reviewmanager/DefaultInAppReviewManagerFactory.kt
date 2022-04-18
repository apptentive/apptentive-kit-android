package apptentive.com.android.feedback.rating.reviewmanager

import android.app.Activity
import android.content.Context
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.IN_APP_REVIEW
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability

internal class DefaultInAppReviewManagerFactory : InAppReviewManagerFactory {

    override fun createReviewManager(context: Context): InAppReviewManager {
        return try {
             when {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != SUCCESS -> {
                    Log.e(
                        IN_APP_REVIEW,
                        "Unable to create InAppReviewManager: Google Play Services not available" +
                                " ${
                                    getStatusMessage(
                                        GoogleApiAvailability.getInstance()
                                            .isGooglePlayServicesAvailable(context)
                                    )
                                }"
                    )
                    UnSupportedReviewManager()
                }
                else -> {
                    if (context is Activity) {
                        Log.d(IN_APP_REVIEW, "Initialized Google Play in-App review manager")
                        GooglePlayReviewManager(context)
                    } else {
                        Log.d(IN_APP_REVIEW, "Failed to launch in-app review flow: make sure you pass Activity object into your Apptentive.engage() calls.")
                        UnSupportedReviewManager()
                    }

                }
            }
        } catch (exception: Exception) {
            Log.e(IN_APP_REVIEW,"Unable to create Google Play in-App review manager", exception)
            UnSupportedReviewManager()
        }

    }

    private fun getStatusMessage(result: Int): String? {
        return when (result) {
            ConnectionResult.SERVICE_MISSING -> "SERVICE_MISSING"
            ConnectionResult.SERVICE_UPDATING -> "SERVICE_UPDATING"
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "SERVICE_VERSION_UPDATE_REQUIRED"
            ConnectionResult.SERVICE_DISABLED -> "SERVICE_DISABLED"
            ConnectionResult.SERVICE_INVALID -> "SERVICE_INVALID"
            else -> "unknown result: $result"
        }
    }
}
