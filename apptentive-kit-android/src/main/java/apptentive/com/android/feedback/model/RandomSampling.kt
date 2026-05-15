package apptentive.com.android.feedback.model

import com.apptentive.apptentive_kit_android.BuildConfig
import kotlin.random.Random

internal data class RandomSampling(
    val percents: MutableMap<String, Double> = mutableMapOf(),
) {
    fun getOrPutRandomValue(id: String, debug: Boolean = BuildConfig.DEBUG): Double {
        return percents.getOrPut(id) {
            if (debug) 50.0 else Random.nextDouble(99.9)
        }
    }

    fun getRandomValue(): Double {
        return Random.nextDouble(99.9)
    }
}
