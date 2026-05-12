package apptentive.com.android.core.encryption

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
interface KeyResolver {
    fun resolveKey(): EncryptionKey
    fun resolveMultiUserWrapperKey(user: String): EncryptionKey
}
