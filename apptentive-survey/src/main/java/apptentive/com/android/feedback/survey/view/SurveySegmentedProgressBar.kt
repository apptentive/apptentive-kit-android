package apptentive.com.android.feedback.survey.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.R as CoreR
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags

@SuppressLint("ResourceType")
internal class SurveySegmentedProgressBar(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private val progressBar: LinearLayout
    private var previousIcon: Drawable?
    private var currentIcon: Drawable?
    private var nextIcon: Drawable?

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.apptentive_survey_segmented_progress_bar, this, true)
        progressBar = findViewById(R.id.apptentive_progress_bar_pill_layout)

        val progressIcons: TypedArray = context.obtainStyledAttributes(
            intArrayOf(
                CoreR.attr.apptentiveProgressBarPreviousIcon,
                CoreR.attr.apptentiveProgressBarCurrentIcon,
                CoreR.attr.apptentiveProgressBarNextIcon
            )
        )

        try {
            previousIcon = progressIcons.getDrawable(0)
            currentIcon = progressIcons.getDrawable(1)
            nextIcon = progressIcons.getDrawable(2)
        } catch (e: Exception) {
            Log.e(LogTags.INTERACTIONS, "Error loading progress bar icons. Reverting to default", e)

            previousIcon = AppCompatResources.getDrawable(context, CoreR.drawable.apptentive_pill_previous)
            currentIcon = AppCompatResources.getDrawable(context, CoreR.drawable.apptentive_pill_current)
            nextIcon = AppCompatResources.getDrawable(context, CoreR.drawable.apptentive_pill_next)
        } finally {
            progressIcons.recycle()
        }
    }

    fun setSegmentCount(count: Int) {
        // Giving this a horizontal margin based on how many segments so the sizing of the
        // individual segments are more reasonable.
        val PROGRESS_BAR_HORIZONTAL_MARGIN = 350
        val params = progressBar.layoutParams as MarginLayoutParams
        params.marginStart = PROGRESS_BAR_HORIZONTAL_MARGIN / count
        params.marginEnd = PROGRESS_BAR_HORIZONTAL_MARGIN / count
        progressBar.layoutParams = params

        for (i in 0 until count) {
            val segment = LayoutInflater.from(context)
                .inflate(R.layout.apptentive_survey_segmented_progress_bar_item, progressBar, false)
            segment.background = nextIcon
            progressBar.addView(segment)
        }
    }

    fun updateProgress(currentQuestion: Int) {
        if (currentQuestion < 0) return

        val transition = ChangeBounds()
        transition.duration = 300
        TransitionManager.beginDelayedTransition(progressBar, transition)

        if (currentQuestion > 0) {
            for (i in 0 until currentQuestion) {
                val previousSegment = progressBar.getChildAt(i)
                previousSegment.background = previousIcon
            }
        }

        val currentSegment = progressBar.getChildAt(currentQuestion)
        currentSegment.background = currentIcon
    }
}
