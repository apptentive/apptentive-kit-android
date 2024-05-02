package apptentive.com.android.feedback.link

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType

internal class NavigateToLinkInteraction(
    id: InteractionId,
    val url: String,
    val target: Target,
    val appendVariables: List<String>
) : Interaction(id, InteractionType.NavigateToLink) {
    @Suppress("EnumEntryName")
    enum class Target {
        new,
        self;

        companion object {
            fun parse(value: String?) = try {
                if (value != null) valueOf(value) else new
            } catch (ignored: Exception) {
                new
            }
        }
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id, url=\"$url\", target=$target, appendVariables=$appendVariables)"
    }

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NavigateToLinkInteraction) return false

        if (url != other.url) return false
        if (target != other.target) return false
        if (appendVariables != other.appendVariables) return false
        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + appendVariables.hashCode()
        return result
    }

    //endregion
}
