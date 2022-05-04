package apptentive.com.android.core

import androidx.lifecycle.MutableLiveData

internal fun <T> MutableLiveData<T>.postIfChanged(value: T?) {
    if (this.value != value) {
        this.postValue(value)
    }
}
