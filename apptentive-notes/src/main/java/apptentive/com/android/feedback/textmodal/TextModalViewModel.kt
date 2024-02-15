package apptentive.com.android.feedback.textmodal

import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.core.Callback
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LiveEvent
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.PrefetchManager
import apptentive.com.android.feedback.engagement.EngagementContextFactory
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.INTERACTIONS

internal class TextModalViewModel : ViewModel() {
    private val context = DependencyProvider.of<EngagementContextFactory>().engagementContext()

    private val interaction: TextModalModel = try {
        DependencyProvider.of<TextModalModelFactory>().getTextModalModel()
    } catch (exception: Exception) {
        val interaction: TextModalInteraction = getInteractionBackup()

        TextModalModel(
            id = interaction.id,
            title = interaction.title,
            body = interaction.body,
            richContent = interaction.richContent,
            actions = interaction.actions.map { action ->
                DefaultTextModalActionConverter().convert(action)
            }
        )
    }

    val maxHeight = interaction.maxHeight
    private val scaleType = interaction.richContent?.layout
    private val scale = interaction.richContent?.scale
    val title = interaction.title
    val message = interaction.body
    val alternateText = interaction.richContent?.alternateText
    val actions = interaction.actions.mapIndexed { index, action ->
        if (action is TextModalModel.Action.Dismiss) {
            ActionModel.DismissActionModel(
                title = action.label,
                callback = {
                    // invoke action
                    context.executors.state.execute(createActionCallback(action, index))

                    // dismiss UI
                    onDismiss?.invoke()
                }
            )
        } else {
            ActionModel.OtherActionModel(
                title = action.label,
                callback = {
                    // invoke action
                    context.executors.state.execute(createActionCallback(action, index))

                    // dismiss UI
                    onDismiss?.invoke()
                }
            )
        }
    }

    var onDismiss: Callback? = null

    private val noteHeaderEvent = LiveEvent<Bitmap>()
    val noteHeaderBitmapStream: LiveData<Bitmap> = noteHeaderEvent
    init {
        context.executors.state.execute {
            interaction.richContent?.url?.let { url ->
                PrefetchManager.getImage(url)?.let { noteHeaderEvent.postValue(it) }
            }
        }
    }

    fun onCancel() {
        context.executors.state.execute {
            engageCodePoint(CODE_POINT_CANCEL)
        }
    }

    private fun engageCodePoint(
        codePoint: String,
        data: Map<String, Any?>? = null,
        actionId: String? = null
    ) {
        context.engage(
            event = Event.internal(codePoint, interaction = "TextModal"),
            interactionId = interaction.id,
            data = data,
            interactionResponses = actionId?.let {
                mapOf(interaction.id to setOf(InteractionResponse.IdResponse(it)))
            }
        )
    }

    private fun createActionCallback(action: TextModalModel.Action, index: Int): Callback =
        when (action) {
            is TextModalModel.Action.Dismiss -> {
                {
                    context.executors.state.execute {
                        Log.i(INTERACTIONS, "Note dismissed")
                        // engage event
                        val data = createEventData(action, index)
                        engageCodePoint(CODE_POINT_DISMISS, data, action.id)
                    }
                }
            }
            is TextModalModel.Action.Invoke -> {
                {
                    context.executors.state.execute {
                        Log.i(INTERACTIONS, "Note action invoked")

                        // run invocation
                        val result = context.engage(action.invocations)

                        // engage event
                        val data = createEventData(action, index, result)
                        engageCodePoint(CODE_POINT_INTERACTION, data, action.id)
                    }
                }
            }
            is TextModalModel.Action.Event -> {
                {
                    context.executors.state.execute {
                        Log.i(INTERACTIONS, "Note event engaged")

                        // engage target event
                        val result = context.engage(
                            event = action.event,
                            interactionId = interaction.id
                        )

                        // engage event
                        val data = createEventData(action, index, result)
                        engageCodePoint(CODE_POINT_EVENT, data, action.id)
                    }
                }
            }
        }

    fun isRichNote(): Boolean {
        return interaction.richContent != null
    }

    fun getImageScaleType(): ScaleType = getImageScaleTypeFromConfig(scaleType ?: LayoutOptions.FULL_WIDTH)

    fun getLayoutParams(currentLayoutParams: LinearLayout.LayoutParams, imageHeight: Int): LayoutParams {
        return getLayoutParamsForTheImagePositioning(currentLayoutParams, imageHeight, scaleType ?: LayoutOptions.FULL_WIDTH)
    }

    fun getPadding(paddingFromDimen: Float): Int {
        return getPaddingForTheImagePositioning(paddingFromDimen, scaleType ?: LayoutOptions.FULL_WIDTH)
    }

    fun getModalHeight(maxModalHeight: Int, defaultModalHeight: Int): Int {
        return getAdjustedModalHeight(maxModalHeight, defaultModalHeight, maxHeight)
    }

    fun getScalingFactor(deviceDensity: Float): Float =
        if (scale != null && scale != 0) scale.toFloat() / getDeviceDensity(deviceDensity) else 1f

    private fun getDeviceDensity(deviceDensity: Float): Float = getAdjustedDeviceDensity(deviceDensity)

    fun getAlternateTextGravity(): Int {
        return getAlternateTextGravity(scaleType ?: LayoutOptions.FULL_WIDTH)
    }

    sealed class ActionModel(open val title: String, open val callback: Callback) {
        data class OtherActionModel(override val title: String, override val callback: Callback) :
            ActionModel(title, callback) {
            operator fun invoke() {
                callback.invoke()
            }
        }

        data class DismissActionModel(override val title: String, override val callback: Callback) :
            ActionModel(title, callback) {
            operator fun invoke() {
                callback.invoke()
            }
        }
    }

    companion object {
        const val CODE_POINT_INTERACTION = "interaction"
        const val CODE_POINT_EVENT = "event"
        const val CODE_POINT_DISMISS = "dismiss"
        const val CODE_POINT_CANCEL = "cancel"

        private const val DATA_ACTION_ID = "action_id"
        private const val DATA_ACTION_LABEL = "label"
        private const val DATA_ACTION_POSITION = "position"
        private const val DATA_ACTION_INTERACTION_ID = "invoked_interaction_id"

        private fun createEventData(
            action: TextModalModel.Action,
            actionPosition: Int,
            engagementResult: EngagementResult? = null
        ): Map<String, Any?> {
            // we need to include a target interaction id (if any)
            if (engagementResult != null) {
                val interactionId =
                    (engagementResult as? EngagementResult.InteractionShown)?.interactionId
                return mapOf(
                    DATA_ACTION_ID to action.id,
                    DATA_ACTION_LABEL to action.label,
                    DATA_ACTION_POSITION to actionPosition,
                    DATA_ACTION_INTERACTION_ID to interactionId
                )
            }

            return mapOf(
                DATA_ACTION_ID to action.id,
                DATA_ACTION_LABEL to action.label,
                DATA_ACTION_POSITION to actionPosition
            )
        }
    }
}
