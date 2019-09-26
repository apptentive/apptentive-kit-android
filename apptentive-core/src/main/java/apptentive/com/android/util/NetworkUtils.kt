@file:Suppress("DEPRECATION")

package apptentive.com.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * Utility class for network related queries.
 */
object NetworkUtils {
    /**
     * Indicates whether network connectivity exists or is in the process
     * of being established.
     */
    fun isNetworkConnected(context: Context): Boolean {
        // FIXME: check ACCESS_NETWORK_STATE permission
        return getActiveNetwork(context)?.isConnectedOrConnecting == true
    }

    /**
     * Returns active network (if present)
     */
    private fun getActiveNetwork(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }
}