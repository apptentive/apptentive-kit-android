//package apptentive.com.app;
//
//import android.app.Activity;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import apptentive.com.android.feedback.Apptentive;
//import apptentive.com.android.feedback.ApptentiveActivityInfo;
//import apptentive.com.app.databinding.ActivityMainBinding;
//
//public class MainActivity extends AppCompatActivity implements ApptentiveActivityInfo {
//    ActivityMainBinding binding;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        binding.unreadMessagesText.setText(
//                getResources().getQuantityString(R.plurals.unread_messages, Apptentive.getUnreadMessageCount(), Apptentive.getUnreadMessageCount())
//        );
//        binding.messageCenterButton.setOnClickListener(v -> Apptentive.showMessageCenter());
//
//        // These are just for testing MC functions from Java
//        binding.loveDialogButton.setOnClickListener(v -> Apptentive.sendAttachmentText("abc 123"));
//        binding.noteButton.setOnClickListener(v -> {
//            String text = "Test text file";
//            byte[] bytes = text.getBytes();
//            Apptentive.sendAttachmentFile(bytes, "text/plain");
//        });
//    }
//
//    @NonNull
//    @Override
//    public Activity getApptentiveActivityInfo() {
//        return this;
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Apptentive.registerApptentiveActivityInfoCallback(this);
//        Apptentive.addUnreadMessagesListener(unreadMessages ->
//                binding.unreadMessagesText.setText(getResources().getQuantityString(R.plurals.unread_messages, unreadMessages, unreadMessages))
//        );
//    }
//
//}
