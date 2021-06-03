package apptentive.com.android.feedback.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import static android.widget.Toast.LENGTH_SHORT;
import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;

public class MainActivity extends AppCompatActivity {
    public static final long ONE_DAY = 1000 * 60 * 60 * 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long currentTimeMillis = System.currentTimeMillis();
                long thirtyDays = currentTimeMillis + ONE_DAY * 30;
                String jwt = generateJWT("User", "ClientTeam", currentTimeMillis, thirtyDays, "38127017f4cfb4f84c8dfecd48ab98c6", null, null);
                Apptentive.login(jwt, new Apptentive.LoginCallback() {
                    @Override
                    public void onLoginFinish() {
                        ApptentiveLog.i(CONVERSATION, "Login finished");
                    }

                    @Override
                    public void onLoginFail(String errorMessage) {
                        ApptentiveLog.e(CONVERSATION, "Login failed: %s", errorMessage);
                    }
                });
            }
        });

        findViewById(R.id.engage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Apptentive.engage(MainActivity.this, "love_dialog_test");
                Apptentive.setPersonEmail("person@company.com");
                Apptentive.setPersonName("First Second");
                Apptentive.addCustomPersonData("person-int", 10);
                Apptentive.addCustomPersonData("person-bool", true);
                Apptentive.addCustomPersonData("person-str", "person");
                Apptentive.addCustomDeviceData("device-int", 20);
                Apptentive.addCustomDeviceData("device-bool", false);
                Apptentive.addCustomDeviceData("device-str", "device");
            }
        });

        findViewById(R.id.message_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Apptentive.showMessageCenter(MainActivity.this);
            }
        });
    }

    private String generateJWT(String subject, String issuer, long issuedAt, long expiration, String secret, Map<String, Object> headerParams, Map<String, Object> bodyParams) {
        if (secret == null || secret.length() == 0) {
            Toast.makeText(this, "Missing Secret", LENGTH_SHORT).show();
            return null;
        }
        SecretKeySpec secretKey;
        try {
            secretKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA512");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Error generating JWT: " + e.getMessage(), LENGTH_SHORT).show();
            return null;
        }

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("type", "user")
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date(issuedAt))
                .setExpiration(new Date(expiration))
                .setHeaderParams(headerParams)
                .signWith(SignatureAlgorithm.HS512, secretKey);
        if (bodyParams != null) {
            for (String key : bodyParams.keySet()) {
                builder.claim(key, bodyParams.get(key));
            }
        }

        return builder.compact();
    }
}