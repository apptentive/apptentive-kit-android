package apptentive.com.android.feedback.redux

import apptentive.com.android.feedback.model.ApptentiveState
import org.rekotlin.Store

fun createStore() = Store(
    reducer = ::rootReducer,
    state = ApptentiveState.initialState(),
    middleware = listOf(conversationSaveMiddleWare)
)