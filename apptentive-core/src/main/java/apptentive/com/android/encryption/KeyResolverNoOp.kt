package apptentive.com.android.encryption

internal class KeyResolverNoOp : KeyResolver {
    override fun resolveKey(): EncryptionKey {
        return EncryptionKey.NO_OP
    }

    override fun resolveMultiUserWrapperKey(user: String): EncryptionKey {
        return EncryptionKey.NO_OP
    }
}
