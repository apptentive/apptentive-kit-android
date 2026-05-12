package apptentive.com.android.feedback.survey.utils

import android.text.Spanned
import androidx.core.text.toSpanned
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal object SpannedUtils {
    fun emptySpanned(): Spanned = "".toSpanned()

    fun convertToSpanned(source: String): Spanned = source.toSpanned()

    @OptIn(ExperimentalContracts::class)
    fun isSpannedNotNullOrEmpty(spanned: Spanned?): Boolean {
        contract {
            returns(true) implies (spanned != null)
        }
        return !spanned.isNullOrEmpty()
    }

    fun convertToString(spanned: Spanned?): String = spanned.toString()
}
