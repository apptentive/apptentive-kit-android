package apptentive.com.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetworkUtils {
    /**
     * Indicates whether network connectivity exists or is in the process
     * of being established.
     */
    fun isNetworkConnected(context: Context): Boolean {
        // TODO: check ACCESS_NETWORK_STATE permission
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}