package apptentive.com.android.encryption

import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface KeyResolver {
    fun resolveKey(): EncryptionKey
    fun resolveMultiUserWrapperKey(user: String): EncryptionKey
}
