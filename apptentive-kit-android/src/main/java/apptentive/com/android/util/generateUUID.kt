@file:JvmName("UUIDUtils")

package apptentive.com.android.util

import java.util.UUID

internal fun generateUUID() = UUID.randomUUID().toString()
