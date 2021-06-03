package apptentive.com.android.feedback.test;

import android.app.Application;

import apptentive.com.android.feedback.Apptentive;
import apptentive.com.android.feedback.ApptentiveConfiguration;
import apptentive.com.android.util.LogLevel;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApptentiveConfiguration configuration = new ApptentiveConfiguration("ANDROID-ANDROID-DEV-c9c0b324114f", "98f5539e9310dc290394c68b76664e98");
        configuration.setLogLevel(LogLevel.Verbose);
        Apptentive.register(this, configuration);
    }
}
