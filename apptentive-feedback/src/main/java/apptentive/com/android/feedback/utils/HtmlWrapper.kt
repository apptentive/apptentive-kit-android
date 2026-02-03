package apptentive.com.android.feedback.utils

import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.util.Linkify
import apptentive.com.android.util.InternalUseOnly
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@InternalUseOnly
object HtmlWrapper {
    fun toHTMLString(source: String): Spanned =
        Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT)

    @OptIn(ExperimentalContracts::class)
    fun linkifiedHTMLString(source: String?): Spanned? {
        contract {
            returns() implies (source != null)
        }
        if (source.isNullOrEmpty()) return null
        val htmlSpanned = toHTMLString(source)
        val htmlURLSpans = htmlSpanned.getSpans(0, htmlSpanned.length, android.text.style.URLSpan::class.java)
        val linkifiedSpannable = SpannableString(htmlSpanned)
        Linkify.addLinks(linkifiedSpannable, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS)
        htmlURLSpans?.forEach { urlSpan ->
            val start = htmlSpanned.getSpanStart(urlSpan)
            val end = htmlSpanned.getSpanEnd(urlSpan)
            if (start >= 0 && end >= 0) {
                linkifiedSpannable.setSpan(urlSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return linkifiedSpannable
    }
}
