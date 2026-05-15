package apptentive.com.android.core.encryption

internal interface KeyResolver {
    fun resolveKey(): EncryptionKey
    fun resolveMultiUserWrapperKey(user: String): EncryptionKey
}
