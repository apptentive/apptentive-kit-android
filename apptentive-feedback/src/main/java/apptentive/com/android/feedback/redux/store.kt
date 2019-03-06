package apptentive.com.android.feedback.redux

import apptentive.com.android.feedback.model.AppState
import org.rekotlin.Store

fun createStore() = Store(
    reducer = ::appReducer,
    state = AppState.initialState(),
    middleware = listOf(conversationSaveMiddleWare)
)