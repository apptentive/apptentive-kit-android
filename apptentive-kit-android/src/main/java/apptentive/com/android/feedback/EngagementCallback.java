package apptentive.com.android.feedback;

import apptentive.com.android.core.SdkAPI;

@SdkAPI
public interface EngagementCallback {
     void onComplete(EngagementResult result);
}
