package apptentive.com.android.core.encryption

import apptentive.com.android.core.util.InternalUseOnly

@InternalUseOnly
class KeyResolverFactory {
    companion object {
        fun getKeyResolver(): KeyResolver =
            try {
                KeyResolver23()
            } catch (e: Exception) {
                throw EncryptionException("Failed to create KeyResolver", e)
            }
    }
}
