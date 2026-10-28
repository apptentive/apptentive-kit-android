package apptentive.com.android.feedback.interactions.textmodal

import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.core.Callback
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.LogTags.INTERACTIONS
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.PrefetchManager
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.platform.ApptentiveKitSDKState.getEngagementContext
import apptentive.com.android.feedback.utils.HtmlWrapper.linkifiedHTMLString
import apptentive.com.android.feedback.utils.getInteractionBackup
import apptentive.com.android.feedback.utils.getWhereEventBackup
import apptentive.com.android.ui.DialogPosition
import apptentive.com.android.ui.core.LiveEvent
import apptentive.com.android.ui.toGravity
import apptentive.com.android.util.Log

internal class TextModalViewModel : ViewModel() {
    val dismissInteraction: LiveEvent<Unit> = LiveEvent()
    private val context: EngagementContext? = try {
        getEngagementContext()
    } catch (exception: Exception) {
        Log.e(INTERACTIONS, "Provider is not registered, could not create engagement context", exception)
        dismissInteraction.postValue(Unit)
        null
    }

    private val interaction: TextModalModel? = try {
        DependencyProvider.of<TextModalModelFactory>().getTextModalModel()
    } catch (exception: Exception) {
        getInteractionModelBackup(exception)
    }

    /**
     * Rebuilds the [TextModalModel] from the persisted backup after the SDK process was killed
     * while the note was in the background. The backup is deserialized with Gson, which can produce
     * an interaction with `null` fields (e.g. `actions`) despite their non-null Kotlin types when the
     * stored JSON is missing or malformed. Any failure here is swallowed and the interaction is
     * dismissed, mirroring [RatingDialogViewModel]/[EnjoymentDialogViewModel], so a corrupt backup can
     * never crash the host app on resume.
     */
    private fun getInteractionModelBackup(exception: Exception): TextModalModel? {
        Log.e(
            INTERACTIONS,
            "TextModalModelFactory is not registered, trying to build TextModalModel from backup...",
            exception
        )
        return try {
            val backup: TextModalInteraction = getInteractionBackup()
            TextModalModel(
                id = backup.id,
                title = backup.title,
                body = backup.body,
                richContent = backup.richContent,
                actions = backup.actions.map { action ->
                    DefaultTextModalActionConverter().convert(action)
                },
                position = backup.position,
                verticalMargins = backup.verticalMargins,
                whereEvent = getWhereEventBackup(),
            )
        } catch (e: Exception) {
            Log.e(INTERACTIONS, "Building TextModalModel from backup failed", e)
            dismissInteraction.postValue(Unit)
            null
        }
    }

    val maxHeight = interaction?.maxHeight ?: DEFAULT_MAX_HEIGHT
    private val scaleType = interaction?.richContent?.layout
    private val scale = interaction?.richContent?.scale
    private var isWiderImage: Boolean = false
    val title = linkifiedHTMLString(interaction?.title)
    val message = linkifiedHTMLString(interaction?.body)
    val alternateText = interaction?.richContent?.alternateText
    val actions = interaction?.actions?.mapIndexed { index, action ->
        if (action is TextModalModel.Action.Dismiss) {
            ActionModel.DismissActionModel(
                title = action.label,
                callback = {
                    // invoke action
                    context?.executors?.state?.execute(createActionCallback(action, index))

                    // dismiss UI
                    onDismiss?.invoke()
                }
            )
        } else {
            ActionModel.OtherActionModel(
                title = action.label,
                callback = {
                    // invoke action
                    context?.executors?.state?.execute(createActionCallback(action, index))

                    // dismiss UI
                    onDismiss?.invoke()
                }
            )
        }
    } ?: emptyList()

    var onDismiss: Callback? = null
    val verticalMargins = interaction?.verticalMargins
    private val noteHeaderEvent = LiveEvent<Bitmap>()
    val noteHeaderBitmapStream: LiveData<Bitmap> = noteHeaderEvent
    init {
        context?.executors?.state?.execute {
            interaction?.richContent?.url?.let { url ->
                PrefetchManager.getImage(url)?.let {
                    isWiderImage = it.width > MAX_IMAGE_WIDTH
                    noteHeaderEvent.postValue(it)
                }
            }
        }
    }

    fun getPromptsPosition(): Int = (interaction?.position ?: DialogPosition.CENTER).toGravity()

    fun onCancel() {
        context?.executors?.state?.execute {
            engageCodePoint(CODE_POINT_CANCEL)
        }
    }

    private fun engageCodePoint(
        codePoint: String,
        data: Map<String, Any?>? = null,
        actionId: String? = null,
        whereEvent: String? = null,
    ) {
        context?.engage(
            event = Event.internal(codePoint, interaction = "TextModal"),
            interactionId = interaction?.id,
            data = data,
            interactionResponses = actionId?.let { responseId ->
                interaction?.id?.let { id -> mapOf(id to setOf(InteractionResponse.IdResponse(responseId))) }
            },
            whereEvent = whereEvent
        )
    }

    private fun createActionCallback(action: TextModalModel.Action, index: Int): Callback = when (action) {
        is TextModalModel.Action.Dismiss -> {
            {
                context?.executors?.state?.execute {
                    Log.i(INTERACTIONS, "Note dismissed")
                    // engage event
                    val data = createEventData(action, index)
                    engageCodePoint(CODE_POINT_DISMISS, data, action.id, interaction?.whereEvent)
                }
            }
        }
        is TextModalModel.Action.Invoke -> {
            {
                context?.executors?.state?.execute {
                    Log.i(INTERACTIONS, "Note action invoked")

                    // run invocation
                    val result = context.engage(action.invocations, interaction?.whereEvent)

                    // engage event
                    val data = createEventData(action, index, result)
                    engageCodePoint(CODE_POINT_INTERACTION, data, action.id, interaction?.whereEvent)
                }
            }
        }
        is TextModalModel.Action.Event -> {
            {
                context?.executors?.state?.execute {
                    Log.i(INTERACTIONS, "Note event engaged")

                    // engage target event
                    val result = context.engage(
                        event = action.event,
                        interactionId = interaction?.id
                    )

                    // engage event
                    val data = createEventData(action, index, result)
                    engageCodePoint(CODE_POINT_EVENT, data, action.id)
                }
            }
        }
    }

    fun getImageScaleType(): ScaleType = getImageScaleTypeFromConfig(isWiderImage, scaleType ?: LayoutOptions.FULL_WIDTH)

    fun getLayoutParams(currentLayoutParams: LinearLayout.LayoutParams, imageHeight: Int): LayoutParams {
        return getLayoutParamsForTheImagePositioning(isWiderImage, currentLayoutParams, imageHeight, scaleType ?: LayoutOptions.FULL_WIDTH)
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
        const val MAX_IMAGE_WIDTH = 1000

        // Matches the default in TextModalModel/TextModalInteraction; used when the interaction
        // could not be restored from backup.
        private const val DEFAULT_MAX_HEIGHT = 100

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
