package apptentive.com.android.feedback.rating.reviewmanager

import android.app.Activity
import android.content.Context
import android.os.Build
import apptentive.com.android.feedback.Constants.MIN_ANDROID_API_VERSION_FOR_IN_APP_RATING
import apptentive.com.android.feedback.IN_APP_REVIEW
import apptentive.com.android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.ConnectionResult.SUCCESS
import com.google.android.gms.common.GoogleApiAvailability

class DefaultInAppReviewManagerFactory : InAppReviewManagerFactory {

    override fun createReviewManager(context: Context): InAppReviewManager {
        return try {
             when {
                 //FIXME verify if this check is still needed
                Build.VERSION.SDK_INT < MIN_ANDROID_API_VERSION_FOR_IN_APP_RATING -> {
                    Log.e(
                        IN_APP_REVIEW,
                        "Unable to create InAppReviewManager: Android version is too low ${Build.VERSION.SDK_INT}"
                    )
                    UnSupportedReviewManager()
                }
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
                    //FIXME [PBI-2056] Don't assume as activity context
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
