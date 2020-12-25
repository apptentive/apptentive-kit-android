package apptentive.com.android.feedback.ui

import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionId
import apptentive.com.android.feedback.model.InvocationData

class TextModalInteraction(
    id: InteractionId,
    title: String?,
    body: String?,
    actions: List<Action>
) : Interaction(id, "TextModal") {
    sealed class Action(val id: String, val label: String) {
        class Invoke(
            id: String,
            label: String,
            val invocations: List<InvocationData>
        ) : Action(id, label) {
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

        class Dismiss(id: String, label: String) : Action(id, label) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Dismiss) return false
                if (!super.equals(other)) return false
                return true
            }
        }

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
}