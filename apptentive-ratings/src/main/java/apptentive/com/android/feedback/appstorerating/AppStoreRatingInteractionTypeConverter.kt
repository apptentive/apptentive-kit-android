package apptentive.com.android.feedback.appstorerating

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionTypeConverter
import apptentive.com.android.util.optString

internal class AppStoreRatingInteractionTypeConverter : InteractionTypeConverter<AppStoreRatingInteraction> {
    @Suppress("UNCHECKED_CAST")
    override fun convert(data: InteractionData) = AppStoreRatingInteraction(
        id = data.id,
        storeID = data.configuration.optString("store_id"),
        method = data.configuration.optString("method"),
        url = data.configuration.optString("url")
    )
}
