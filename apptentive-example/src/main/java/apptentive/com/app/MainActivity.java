//package apptentive.com.app;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import apptentive.com.android.feedback.Apptentive;
//import apptentive.com.android.feedback.ApptentiveActivityInfo;
//import apptentive.com.android.feedback.model.EventNotification;
//import apptentive.com.android.feedback.model.MessageCenterNotification;
//import apptentive.com.app.databinding.ActivityMainBinding;
//import kotlin.Unit;
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
//
//        //Button button = findViewById(R.id.survey_button);
//        //Boolean canShowSurvey = Apptentive.canShowInteraction("survey");
//        //button.setVisibility(canShowSurvey ? View.VISIBLE : View.GONE);
//        //button.setOnClickListener(v -> Apptentive.engage("survey"));
//
//        Apptentive.getEventNotificationObservable().observe(this::handleEventNotification);
//        Apptentive.getMessageCenterNotificationObservable().observe(this::handleMessageCenterNotification);
//    }
//
//    public Unit handleEventNotification(EventNotification notification) {
//        String name = notification.getName();
//        String vendor = notification.getVendor();
//        String interaction = notification.getInteraction();
//        String interactionId = notification.getInteractionId() != null ?
//                "\"" + notification.getInteractionId() + "\"" : "`null`";
//
//        String notificationText = "Name: \"" + name + "\". Vendor: \"" + vendor + "\". " +
//                "Interaction: \"" + interaction + "\". Interaction ID: " + interactionId;
//        Log.d("APPTENTIVE_EVENT", notificationText);
//
//        // Survey interaction handling
//        if (interaction.equals("Survey")) {
//            switch (name) {
//                case "launch":
//                    // Survey shown
//                    break;
//                case "submit":
//                    // Survey completed
//                    break;
//                case "cancel":
//                case "cancel_partial":
//                    // Survey closed without completing
//                    break;
//            }
//        }
//
//        return Unit.INSTANCE;
//    }
//
//    public Unit handleMessageCenterNotification(MessageCenterNotification notification) {
//        String notificationText = "Can show Message Center: " + notification.getCanShowMessageCenter() + ". " +
//                "Unread message count: " + notification.getUnreadMessageCount() + ". " +
//                "Person name: " + notification.getPersonName() + ". " +
//                "Person email: " + notification.getPersonEmail();
//
//        Log.d("APPTENTIVE_MC_EVENT", notificationText);
//
//        // Message Center data handling example
//        runOnUiThread (new Thread(() -> {
//            binding.messageCenterButton.setEnabled(notification.getCanShowMessageCenter());
//            binding.unreadMessagesText.setText("Unread Messages: " + notification.getUnreadMessageCount());
//        }));
//
//        return Unit.INSTANCE;
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
//    }
//
//}
