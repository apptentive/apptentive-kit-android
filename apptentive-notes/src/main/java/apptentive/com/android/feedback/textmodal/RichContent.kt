package apptentive.com.android.feedback.textmodal

internal data class RichContent(
    val url: String = "",
    val layout: LayoutOptions = LayoutOptions.FULL_WIDTH,
    val alternateText: String? = "",
    val scale: Int = 0,
)

internal enum class LayoutOptions {
    FULL_WIDTH,
    CENTER,
    ALIGN_LEFT,
    ALIGN_RIGHT
}
