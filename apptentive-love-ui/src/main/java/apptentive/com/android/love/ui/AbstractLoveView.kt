package apptentive.com.android.love.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.love.LoveEntity
import apptentive.com.android.love.LoveSender

abstract class AbstractLoveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    var sentimentIdentifier: String? = null

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AbstractLoveView)
            val sentimentIdentifier = typedArray.getString(R.styleable.AbstractLoveView_sentiment)
            if (sentimentIdentifier == null || sentimentIdentifier.isEmpty()) {
                throw IllegalStateException("Missing sentiment identifier")
            }

            this.sentimentIdentifier = sentimentIdentifier

            typedArray.recycle()
        }
    }

    protected fun sendLoveEntity(
        entity: LoveEntity,
        onSend: ((entity: LoveEntity) -> Unit)? = null,
        onError: ((entity: LoveEntity, error: Exception) -> Unit)? = null
    ) {
        val sender = DependencyProvider.of<LoveSender>()
        sender.send(entity, onSend, onError)
    }
}