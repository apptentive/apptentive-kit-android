package apptentive.com.android.feedback.survey.viewmodel

import android.content.res.Resources
import android.widget.TextView
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.feedback.survey.view.SurveyQuestionContainerView
import com.google.android.material.slider.Slider

/**
 * Class which represents range question list item state
 * @param id question id
 * @param title question title
 * @param instructions optional instructions text (for example, "Required")
 * @param validationError contains validation error message in case if the question has an invalid
 *                        answer or <code>null</code> if the answer is valid.
 * @param selectedIndex selected value (or <code>null</code> if nothing is selected)
 */
class RangeQuestionListItem(
    id: String,
    title: String,
    val min: Int,
    val max: Int,
    instructions: String? = null,
    validationError: String? = null,
    val minLabel: String? = null,
    val maxLabel: String? = null,
    val selectedIndex: Int? = null
) : SurveyQuestionListItem(
    id = id,
    type = Type.RangeQuestion,
    title = title,
    instructions = instructions,
    validationError = validationError
) {
    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RangeQuestionListItem) return false
        if (!super.equals(other)) return false

        if (min != other.min) return false
        if (max != other.max) return false
        if (minLabel != other.minLabel) return false
        if (maxLabel != other.maxLabel) return false
        if (selectedIndex != other.selectedIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + min
        result = 31 * result + max
        result = 31 * result + (minLabel?.hashCode() ?: 0)
        result = 31 * result + (maxLabel?.hashCode() ?: 0)
        result = 31 * result + (selectedIndex ?: 0)
        return result
    }

    //endregion

    //region View Holder

    class ViewHolder(
        itemView: SurveyQuestionContainerView,
        onSelectionChanged: (id: String, selectedIndex: Int) -> Unit
    ) : SurveyQuestionListItem.ViewHolder<RangeQuestionListItem>(itemView) {

        private val rangeSlider = itemView.findViewById<Slider>(R.id.range_slider)
        private val minLabel = itemView.findViewById<TextView>(R.id.minLabel)
        private val maxLabel = itemView.findViewById<TextView>(R.id.maxLabel)


        init {
            rangeSlider.addOnChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    onSelectionChanged(questionId, value.toInt())
                }
            }
        }
        override fun bindView(
            item: RangeQuestionListItem,
            position: Int
        ) {
            super.bindView(item, position)
            rangeSlider.valueFrom = item.min.toFloat()
            rangeSlider.valueTo = item.max.toFloat()
            rangeSlider.stepSize = 1.0F
            if (item.selectedIndex != null) {
                rangeSlider.value = item.selectedIndex.toFloat()
            } else {
                rangeSlider.value = item.min.toFloat()
            }
            val res: Resources = itemView.resources
            minLabel.text = String.format(
                res.getString(R.string.range_min_label),
                item.min,
                item.minLabel
            )
            maxLabel.text = String.format(
                res.getString(R.string.range_max_label),
                item.max,
                item.maxLabel
            )
        }
    }

    //endregion
}