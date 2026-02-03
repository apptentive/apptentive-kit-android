package apptentive.com.android.encryption

import apptentive.com.android.util.InternalUseOnly

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
