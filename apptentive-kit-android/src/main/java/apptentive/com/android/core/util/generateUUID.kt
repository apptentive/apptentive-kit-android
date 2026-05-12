@file:JvmName("UUIDUtils")

package apptentive.com.android.core.util

import java.util.UUID

@InternalUseOnly
fun generateUUID() = UUID.randomUUID().toString()
