package apptentive.com.android.feedback.textmodal

internal data class RichContent(
    val url: String = "",
    val layout: LayoutOptions = LayoutOptions.FIT,
    val alternateText: String? = "",
    val scale: Int = 0,
)

internal enum class LayoutOptions {
    FIT,
    FILL,
    CENTER,
    ALIGN_LEFT,
    ALIGN_RIGHT
}
