package apptentive.com.android.feedback;

import android.app.Application;

/** Interface which represents callback for the method
 * {@link Apptentive#register(Application, ApptentiveConfiguration, RegisterCallback)}
 * on its completion.
 */
public interface RegisterCallback {
     void onComplete(RegisterResult result);
}