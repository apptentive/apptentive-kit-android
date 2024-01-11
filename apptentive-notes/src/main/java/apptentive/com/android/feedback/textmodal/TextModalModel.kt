package apptentive.com.android.feedback.textmodal

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.engagement.interactions.InteractionType
import apptentive.com.android.feedback.model.InvocationData

internal class TextModalModel(
    id: InteractionId,
    val title: String?,
    val body: String?,
    val richContent: RichContent? = null,
    val actions: List<Action>
) : Interaction(id, InteractionType.TextModal) {

    override fun toString(): String {
        return "${javaClass.simpleName} (id=$id, title=\"$title\", body=\"$body\", richContent=$richContent, actions=$actions)"
    }

    sealed class Action(val id: String, val label: String) {
        class Invoke(
            id: String,
            label: String,
            val invocations: List<InvocationData>
        ) : Action(id, label) {
            override fun toString(): String {
                return "${javaClass.simpleName} (id=$id, label=\"$label\", invocations=$invocations)"
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Invoke) return false
                if (!super.equals(other)) return false

                if (invocations != other.invocations) return false

                return true
            }

            override fun hashCode(): Int {
                var result = super.hashCode()
                result = 31 * result + invocations.hashCode()
                return result
            }
        }

        class Dismiss(id: String, label: String) : Action(id, label)

        class Event(
            id: String,
            label: String,
            val event: apptentive.com.android.feedback.engagement.Event
        ) : Action(id, label)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Action) return false

            if (id != other.id) return false
            if (label != other.label) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + label.hashCode()
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TextModalModel) return false

        if (title != other.title) return false
        if (body != other.body) return false
        if (richContent != other.richContent) return false
        if (actions != other.actions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (body?.hashCode() ?: 0)
        result = 31 * result + (richContent?.hashCode() ?: 0)
        result = 31 * result + actions.hashCode()
        return result
    }
}
