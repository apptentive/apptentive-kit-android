package apptentive.com.android.core.encryption

internal class KeyResolverFactory {
    companion object {
        fun getKeyResolver(): KeyResolver =
            try {
                KeyResolver23()
            } catch (e: Exception) {
                throw EncryptionException("Failed to create KeyResolver", e)
            }
    }
}
