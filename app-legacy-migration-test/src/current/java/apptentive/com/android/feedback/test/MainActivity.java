package apptentive.com.android.feedback.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import apptentive.com.android.feedback.Apptentive;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notImplemented("Login not supported yet");
            }
        });

        findViewById(R.id.engage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Apptentive.engage(MainActivity.this, "love_dialog_test");
            }
        });

        findViewById(R.id.message_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notImplemented("Message center supported yet");
            }
        });
    }

    private void notImplemented(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
}