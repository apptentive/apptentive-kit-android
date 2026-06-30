package apptentive.com.android.feedback;

import android.app.PendingIntent;

import apptentive.com.android.core.SdkAPI;

@SdkAPI
public interface PendingIntentCallback {
     void onPendingIntent(PendingIntent pendingIntent);
}
