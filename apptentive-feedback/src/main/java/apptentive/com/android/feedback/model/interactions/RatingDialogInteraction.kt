package apptentive.com.android.feedback.model.interactions

class RatingDialogInteraction(
    id: String,
    val title: String,
    val body: String,
    val rateText: String,
    val remindText: String,
    val declineText: String
) : Interaction(id)