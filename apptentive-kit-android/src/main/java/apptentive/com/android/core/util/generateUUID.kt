@file:JvmName("UUIDUtils")

package apptentive.com.android.core.util

import java.util.UUID

internal fun generateUUID() = UUID.randomUUID().toString()
