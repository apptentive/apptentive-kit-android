package apptentive.com.android.util

import apptentive.com.android.core.serialization.json.JsonConverter

inline fun <reified T> readJson(path: String): T {
    val json = readAssetFile(path)
    return JsonConverter.fromJson(json)
}
