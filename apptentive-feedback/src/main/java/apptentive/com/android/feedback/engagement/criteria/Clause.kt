package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.utils.IndentPrinter

interface Clause {
    fun evaluate(state: TargetingState, printer: IndentPrinter?): Boolean
}