package apptentive.com.android.feedback.survey.viewmodel

import apptentive.com.android.ui.ListViewAdapter
import apptentive.com.android.ui.ListViewItem
import apptentive.com.android.ui.ViewHolderFactory

/**
 * Base class for representing survey list item
 */

abstract class SurveyListItem(id: String, type: Type) : ListViewItem(id, type.ordinal) {
    enum class Type {
        Header,
        Footer,
        SingleLineQuestion,
        RangeQuestion,
        MultiChoiceQuestion
    }
}

internal fun ListViewAdapter.register(type: SurveyListItem.Type, factory: ViewHolderFactory) =
    register(type.ordinal, factory)
