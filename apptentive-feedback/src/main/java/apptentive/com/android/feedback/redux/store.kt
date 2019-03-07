package apptentive.com.android.feedback.redux

import apptentive.com.android.feedback.model.ApptentiveState
import org.rekotlin.Store

internal fun createStore(middleware: List<Middleware<ApptentiveState>>) = Store(
    reducer = ::rootReducer,
    state = ApptentiveState.initialState(),
    middleware = middleware.map { it::apply }
)