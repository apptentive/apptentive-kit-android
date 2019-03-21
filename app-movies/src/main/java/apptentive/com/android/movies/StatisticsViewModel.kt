package apptentive.com.android.movies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import apptentive.com.android.love.ApptentiveLove
import apptentive.com.android.love.LoveEntitySnapshot

class StatisticsViewModel() : ViewModel() {
    val entries: LiveData<Array<LoveEntitySnapshot>> get() {
        val liveData = MutableLiveData<Array<LoveEntitySnapshot>>()
        ApptentiveLove.getEntities().then {
            liveData.postValue(it.data?.toTypedArray())
        }
        return liveData
    }
}