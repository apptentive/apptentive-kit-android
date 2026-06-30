package apptentive.com.android.util

import apptentive.com.android.TestCase

fun readAssetFile(path: String): String {
    return TestCase::class.java.classLoader!!.getResourceAsStream(path).bufferedReader().readText()
}
