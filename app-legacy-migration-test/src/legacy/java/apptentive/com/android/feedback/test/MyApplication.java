package apptentive.com.android.feedback.test;

import android.app.Application;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveConfiguration;
import com.apptentive.android.sdk.ApptentiveLog;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApptentiveConfiguration configuration = new ApptentiveConfiguration("ANDROID-ANDROID-DEV-c9c0b324114f", "98f5539e9310dc290394c68b76664e98");
        configuration.setLogLevel(ApptentiveLog.Level.VERBOSE);
        configuration.setShouldEncryptStorage(true);
        Apptentive.register(this, configuration);
    }
}
